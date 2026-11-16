package com.mepmep.auth;

import com.mepmep.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.Set;
import java.util.regex.Pattern;

/** Decides whether an address looks real: proper shape, not a throwaway inbox, and a domain that can receive mail. */
@Component
public class EmailPolicy {
    private static final Pattern SHAPE = Pattern.compile(
        "^[A-Za-z0-9._%+-]{1,64}@([A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*\\.[A-Za-z]{2,24})$");

    /** Common throwaway-mail services. Not complete: the emailed code is what really proves the inbox is real. */
    private static final Set<String> DISPOSABLE = Set.of(
        "mailinator.com", "guerrillamail.com", "guerrillamail.net", "guerrillamail.org", "sharklasers.com", "grr.la",
        "10minutemail.com", "10minutemail.net", "tempmail.com", "temp-mail.org", "temp-mail.io", "tempmail.net",
        "throwawaymail.com", "yopmail.com", "yopmail.net", "trashmail.com", "trashmail.net", "getnada.com", "nada.email",
        "maildrop.cc", "dispostable.com", "fakeinbox.com", "mintemail.com", "mohmal.com", "emailondeck.com",
        "mytemp.email", "tempinbox.com", "spamgourmet.com", "mailnesia.com", "moakt.com", "burnermail.io",
        "discard.email", "tmpmail.org", "tmpmail.net", "dropmail.me", "minuteinbox.com", "tempail.com",
        "inboxkitten.com", "harakirimail.com", "mailcatch.com", "spambox.us", "33mail.com", "anonaddy.me");

    /** Made-up domains people type when they have no real address in mind. */
    private static final Set<String> PLACEHOLDER_DOMAINS = Set.of(
        "email.com", "example.com", "example.org", "example.net", "test.com", "test.org", "test.net", "sample.com",
        "domain.com", "mail.test", "email.test", "yourdomain.com", "yourmail.com", "youremail.com", "abc.com", "xyz.com",
        "asdf.com", "fake.com", "fakemail.com", "noemail.com", "none.com", "nomail.com", "invalid.com", "localhost.com");

    /** Made-up names before the @: test, sample, demo, asdf, user1 ... A real inbox is rarely called any of these. */
    private static final Pattern PLACEHOLDER_NAME = Pattern.compile(
        "^(test|tester|testing|testuser|test[._-]?user|sample|example|demo|fake|dummy|temp|tmp|abc|abcd|abcde|asdf|asdfg|qwerty|qwe|xyz|"
        + "user|username|name|yourname|your[._-]?name|email|mail|noreply|no-reply|null|none|nobody|nothing|me|you|foo|bar|baz|hello|hi|admin|root)[._-]?\\d*$");

    private static final Pattern REPEATED = Pattern.compile("^(.)\\1{3,}$");

    private final boolean checkMx;

    public EmailPolicy(AppProperties props) {
        this.checkMx = props.auth() != null && props.auth().checkMx();
    }

    /** Returns an error message, or null when the address is acceptable. `email` is already trimmed and lower case. */
    public String problem(String email) {
        var m = SHAPE.matcher(email);
        if (!m.matches() || email.contains("..")) return "Enter a valid email address";
        String domain = m.group(1);
        String local = email.substring(0, email.indexOf('@'));
        if (PLACEHOLDER_NAME.matcher(local).matches() || REPEATED.matcher(local).matches()) {
            return "That looks like a sample email. Use your real address";
        }
        if (PLACEHOLDER_DOMAINS.contains(domain)) return "That looks like a sample email domain. Use your real address";
        if (DISPOSABLE.contains(domain)) return "Disposable email addresses are not allowed";
        if (checkMx && !domainCanReceiveMail(domain)) return "That email domain cannot receive mail";
        return null;
    }

    /** True when the domain has a mail server (MX) or at least an address (A). A slow or broken DNS never blocks a sign-up. */
    boolean domainCanReceiveMail(String domain) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");
        try {
            var ctx = new InitialDirContext(env);
            try {
                Attributes attrs = ctx.getAttributes(domain, new String[] {"MX", "A"});
                return attrs.get("MX") != null || attrs.get("A") != null;
            } finally {
                ctx.close();
            }
        } catch (NameNotFoundException e) {
            return false; // the domain does not exist
        } catch (NamingException e) {
            return true;  // DNS trouble: let the emailed code decide
        }
    }
}
