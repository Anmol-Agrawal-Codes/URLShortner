const apiOriginInput = document.querySelector("#api-origin");
const themeToggle = document.querySelector("#theme-toggle");
const form = document.querySelector("#shorten-form");
const originalUrlInput = document.querySelector("#original-url");
const expiresAtInput = document.querySelector("#expires-at");
const formMessage = document.querySelector("#form-message");
const resultState = document.querySelector("#result-state");
const emptyResult = document.querySelector("#empty-result");
const createdResult = document.querySelector("#created-result");
const shortLink = document.querySelector("#short-link");
const openButton = document.querySelector("#open-button");
const copyButton = document.querySelector("#copy-button");

const themePreference = localStorage.getItem("url-shortner-theme");
const prefersDarkTheme = window.matchMedia("(prefers-color-scheme: dark)").matches;

function setTheme(isDark) {
  const theme = isDark ? "dark" : "light";
  document.documentElement.dataset.theme = theme;
  themeToggle.checked = isDark;
  document.querySelector('meta[name="theme-color"]').content = isDark ? "#1b1d1b" : "#f3f1e9";
}

setTheme(themePreference ? themePreference === "dark" : prefersDarkTheme);
themeToggle.addEventListener("change", () => {
  setTheme(themeToggle.checked);
  localStorage.setItem("url-shortner-theme", themeToggle.checked ? "dark" : "light");
});

const configuredOrigin = window.URL_SHORTNER_CONFIG?.apiBaseUrl || window.location.origin;
apiOriginInput.value = localStorage.getItem("url-shortner-api-origin") || configuredOrigin;

const localDateTimeValue = (date) => {
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 16);
};

expiresAtInput.min = localDateTimeValue(new Date());

function getApiOrigin() {
  const value = apiOriginInput.value.trim();
  const parsed = new URL(value);
  if (!parsed.origin || !["http:", "https:"].includes(parsed.protocol)) {
    throw new Error("Enter a valid HTTP or HTTPS API origin.");
  }
  return parsed.origin;
}

function showMessage(message, isError = false) {
  formMessage.textContent = message;
  formMessage.classList.toggle("is-error", isError);
}

function formatExpiry(value) {
  if (!value) return "Not provided";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(date);
}

async function readError(response) {
  try {
    const body = await response.json();
    return body.message || body.error || `Request failed (${response.status}).`;
  } catch {
    return `Request failed (${response.status}). Check the API connection and try again.`;
  }
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  showMessage("");

  if (!originalUrlInput.validity.valid || !originalUrlInput.value.trim()) {
    showMessage("Enter a complete destination URL, including its protocol.", true);
    originalUrlInput.focus();
    return;
  }

  if (expiresAtInput.value && new Date(expiresAtInput.value) <= new Date()) {
    showMessage("Choose an expiration time in the future.", true);
    expiresAtInput.focus();
    return;
  }

  let apiOrigin;
  try {
    apiOrigin = getApiOrigin();
  } catch {
    showMessage("Enter a valid backend API origin, such as http://localhost:8080.", true);
    apiOriginInput.focus();
    return;
  }

  const submitButton = form.querySelector(".submit-button");
  submitButton.disabled = true;
  submitButton.querySelector("span").textContent = "Creating link...";
  resultState.textContent = "WORKING";

  const payload = {
    originalUrl: originalUrlInput.value.trim(),
    expiresAt: expiresAtInput.value ? `${expiresAtInput.value}:00` : null
  };

  try {
    const response = await fetch(`${apiOrigin}/api/create`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!response.ok) throw new Error(await readError(response));

    const result = await response.json();
    if (!result.shortLink) throw new Error("The API response did not include a short link.");

    const generatedUrl = `${apiOrigin}/api/${encodeURIComponent(result.shortLink)}`;
    shortLink.href = generatedUrl;
    shortLink.textContent = generatedUrl.replace(/^https?:\/\//, "");
    openButton.href = generatedUrl;
    document.querySelector("#destination-value").textContent = result.originalUrl;
    document.querySelector("#expiry-value").textContent = formatExpiry(result.expiresAt);
    emptyResult.hidden = true;
    createdResult.hidden = false;
    resultState.textContent = "CREATED";
    localStorage.setItem("url-shortner-api-origin", apiOrigin);
    showMessage("Your short link is ready.");
  } catch (error) {
    resultState.textContent = "ERROR";
    showMessage(error.message || "Could not reach the API. Check the backend and try again.", true);
  } finally {
    submitButton.disabled = false;
    submitButton.querySelector("span").textContent = "Shorten URL";
  }
});

copyButton.addEventListener("click", async () => {
  try {
    await navigator.clipboard.writeText(shortLink.href);
    copyButton.textContent = "Copied";
    window.setTimeout(() => { copyButton.textContent = "Copy link"; }, 1600);
  } catch {
    showMessage("Clipboard access is unavailable in this browser.", true);
  }
});

apiOriginInput.addEventListener("change", () => {
  try {
    apiOriginInput.value = getApiOrigin();
    localStorage.setItem("url-shortner-api-origin", apiOriginInput.value);
  } catch {
    showMessage("Enter a valid backend API origin, such as http://localhost:8080.", true);
  }
});