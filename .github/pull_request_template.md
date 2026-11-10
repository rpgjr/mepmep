## What changed

<!-- One or two sentences. Why, not how. -->

## How I tested it

- [ ] `mvn -B verify` passes
- [ ] Ran it locally with `mvn spring-boot:run` and tried the endpoint I changed
- [ ] Added or updated a test for the change (if it changes behavior)

## Database

- [ ] No database change
- [ ] Entity or column changed (`ddl-auto: update` applies it on start, so I checked it on a copy of the data first)

## Config and secrets

- [ ] No new environment variable
- [ ] New environment variable added to `.env.example` and set on Render

## Risk and rollback

<!-- What could break? How do we undo it? -->
