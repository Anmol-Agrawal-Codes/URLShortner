# URL Shortner

A URL-shortening service with a Spring Boot API and a standalone static web interface. The frontend and backend are kept as separate deployable parts: the UI can be hosted on any static host, while the API owns link creation, persistence, caching, and redirects.

## Features

- Create short links for HTTP or HTTPS destinations.
- Set an optional expiration time; links default to 10 minutes when one is not supplied.
- Redirect short-link requests to their destination and return errors for expired or missing links.
- Persist links in PostgreSQL and cache redirect lookups with Caffeine.
- Use the included responsive frontend to create, copy, and open short links.

## Architecture

```text
frontend/                 Standalone HTML, CSS, and JavaScript UI
src/main/java/...         Spring Boot API, services, persistence, and cache
src/main/resources/       Application configuration
```

The frontend has no package manager or build step. It calls the backend over HTTP; the backend uses Spring Data JPA with PostgreSQL and a Caffeine cache for URL lookups.

## Requirements

- Java 21
- PostgreSQL
- Python 3 for the optional local static-file server

## Run locally

1. Create a PostgreSQL database named `urlshortener` and set credentials that can access it.
2. Configure the database environment variables if the defaults in `application.properties` do not match your setup:

	| Variable | Default |
	| --- | --- |
	| `DB_URL` | `jdbc:postgresql://127.0.0.1:5433/urlshortener` |
	| `DB_USERNAME` | `your_username` |
	| `DB_PASSWORD` | `your_passord` |
	| `FRONTEND_ORIGIN` | `http://localhost:5500` |

3. Start the API from the project root:

	```powershell
	.\mvnw.cmd spring-boot:run
	```

	On macOS or Linux, use `./mvnw spring-boot:run`. The API listens on port `8080` by default.

4. In a separate terminal, serve the frontend:

	```powershell
	cd frontend
	python -m http.server 5500
	```

	Open `http://localhost:5500`. The frontend defaults to the API at `http://localhost:8080`; its API origin can also be changed in the header and is remembered in the browser.

## API

### Create a short link

`POST /api/create`

```json
{
  "originalUrl": "https://example.com/articles/a-long-page",
  "expiresAt": "2026-11-01T10:30:00"
}
```

`expiresAt` is optional and uses a local date-time string (`yyyy-MM-dd'T'HH:mm:ss`). The response contains the original URL, generated short code, and expiration time:

```json
{
  "originalUrl": "https://example.com/articles/a-long-page",
  "shortLink": "generated-code",
  "expiresAt": "2026-11-01T10:30:00"
}
```

### Look up a short link

`POST /api/getUrl?url={shortLink}` returns the saved URL data for the supplied short code.

### Redirect

`GET /api/{shortLink}` redirects to the original destination. Expired links return `410 Gone`; unknown links return `404 Not Found`.

## Deploy separately

Host the `frontend/` directory as static files and deploy the Spring Boot application as the API. Set `apiBaseUrl` in `frontend/config.js` to the public API origin, and set `FRONTEND_ORIGIN` on the backend to the frontend's exact origin. The backend CORS policy allows that configured origin.

## Tests

Run the backend test suite with:

```powershell
.\mvnw.cmd test
```

The test application context requires a reachable PostgreSQL database.
