# URL Shortner UI

Standalone static frontend for the Spring Boot URL Shortner API. It supports link creation, optional expiration, copying the resulting link, and opening it through the API redirect endpoint.

## Run locally

1. Start the Spring Boot app with its PostgreSQL database available.
2. From this directory, run `python -m http.server 5500`.
3. Open `http://localhost:5500`.

The default API origin is `http://localhost:8080`. It can be changed in the UI and is remembered in the browser. The backend permits the local frontend origin by default.

## Deploy separately

Host this directory as a static site and deploy the Spring Boot API independently. Set `apiBaseUrl` in `config.js` to the public API origin, then set the backend environment variable `FRONTEND_ORIGIN` to the deployed frontend's exact origin. The UI calls `POST /api/create` and builds redirect links at `/api/{shortLink}`.