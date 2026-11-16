package com.mepmep.auth;

import com.mepmep.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank @Size(max = 60) String displayName,
                                  @NotBlank @Size(max = 100) String password) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record UserDto(Long id, String email, String displayName) {}

    public record AuthResponse(String token, UserDto user) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailPolicy emailPolicy;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt, EmailPolicy emailPolicy) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.emailPolicy = emailPolicy;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        String emailProblem = emailPolicy.problem(email);
        if (emailProblem != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, emailProblem);
        var passwordProblems = PasswordPolicy.problems(req.password());
        if (!passwordProblems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, PasswordPolicy.message(passwordProblems));
        }
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = users.save(new User(email, req.displayName().trim(), encoder.encode(req.password())));
        return respond(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = users.findByEmail(req.email().trim().toLowerCase())
            .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        return respond(user);
    }

    @GetMapping("/me")
    public UserDto me(Authentication auth) {
        User user = users.findById((Long) auth.getPrincipal())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return toDto(user);
    }

    private AuthResponse respond(User user) {
        return new AuthResponse(jwt.create(user.getId()), toDto(user));
    }

    private UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName());
    }
}
