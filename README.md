# WebWatcher
Spring Boot app that periodically scrapes websites and notifies the owner via email if changes occur in textual content.
Any non-text content is _not_ tracked. Stores a history which allows you to see previous versions of websites (HTML only).
Uses SendGrid for sending emails.

See `Config.kt` for required/optional configuration properties.

# TODO
- Use a proper database instead of in-memory structures and JSON files (e.g. Mongo, Postgres, Redis; evaluate which fits the best)
