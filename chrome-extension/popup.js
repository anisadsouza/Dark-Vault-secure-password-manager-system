const ACCOUNT_KEY = "darkVaultExtensionAccount";
const SESSION_KEY = "darkVaultExtensionActiveUser";
const CREDENTIALS_PREFIX = "darkVaultCredentials:";

const authView = document.getElementById("auth-view");
const appView = document.getElementById("app-view");
const loginTab = document.getElementById("login-tab");
const registerTab = document.getElementById("register-tab");
const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const loginUsernameInput = document.getElementById("login-username");
const loginPasswordInput = document.getElementById("login-password");
const registerUsernameInput = document.getElementById("register-username");
const registerPasswordInput = document.getElementById("register-password");
const registerConfirmPasswordInput = document.getElementById("register-confirm-password");
const toggleLoginPasswordButton = document.getElementById("toggle-login-password");
const toggleRegisterPasswordButton = document.getElementById("toggle-register-password");
const toggleConfirmPasswordButton = document.getElementById("toggle-confirm-password");
const authMessage = document.getElementById("auth-message");
const registerStrengthMeter = document.getElementById("register-strength-meter");
const registerStrengthFill = document.getElementById("register-strength-fill");
const registerStrengthText = document.getElementById("register-strength-text");
const logoutButton = document.getElementById("logout-button");
const welcomeText = document.getElementById("welcome-text");
const credentialForm = document.getElementById("credential-form");
const siteNameInput = document.getElementById("site-name");
const siteUsernameInput = document.getElementById("site-username");
const sitePasswordInput = document.getElementById("site-password");
const siteNotesInput = document.getElementById("site-notes");
const generateButton = document.getElementById("generate-button");
const togglePasswordButton = document.getElementById("toggle-password-button");
const saveButton = document.getElementById("save-button");
const formMessage = document.getElementById("form-message");
const searchInput = document.getElementById("search-input");
const clearButton = document.getElementById("clear-button");
const vaultMessage = document.getElementById("vault-message");
const credentialList = document.getElementById("credential-list");
const credentialCount = document.getElementById("credential-count");
const strengthMeter = document.getElementById("strength-meter");
const strengthFill = document.getElementById("strength-fill");
const strengthText = document.getElementById("strength-text");

let currentUser = null;
let allCredentials = [];
let visiblePasswords = new Set();

document.addEventListener("DOMContentLoaded", async () => {
    bindEvents();
    try {
        await initializeExtension();
    } catch (error) {
        console.error("Dark Vault extension failed to initialize:", error);
        showAuthView("register");
        showAuthMessage("Extension storage error. Reload the extension and try again.", true);
    }
});

function bindEvents() {
    loginTab.addEventListener("click", () => switchAuthTab("login"));
    registerTab.addEventListener("click", () => switchAuthTab("register"));
    loginForm.addEventListener("submit", handleLogin);
    registerForm.addEventListener("submit", handleRegister);
    logoutButton.addEventListener("click", handleLogout);
    registerPasswordInput.addEventListener("input", () => updateStrengthMeter(registerPasswordInput.value, registerStrengthMeter, registerStrengthFill, registerStrengthText));
    credentialForm.addEventListener("submit", handleSaveCredential);
    siteNameInput.addEventListener("input", updateSaveButtonState);
    siteUsernameInput.addEventListener("input", updateSaveButtonState);
    sitePasswordInput.addEventListener("input", () => {
        updateSaveButtonState();
        updateStrengthMeter(sitePasswordInput.value, strengthMeter, strengthFill, strengthText);
    });
    generateButton.addEventListener("click", handleGeneratePassword);
    togglePasswordButton.addEventListener("click", () => togglePasswordInput(sitePasswordInput, togglePasswordButton));
    toggleLoginPasswordButton.addEventListener("click", () => togglePasswordInput(loginPasswordInput, toggleLoginPasswordButton));
    toggleRegisterPasswordButton.addEventListener("click", () => togglePasswordInput(registerPasswordInput, toggleRegisterPasswordButton));
    toggleConfirmPasswordButton.addEventListener("click", () => togglePasswordInput(registerConfirmPasswordInput, toggleConfirmPasswordButton));
    searchInput.addEventListener("input", renderCredentials);
    clearButton.addEventListener("click", () => {
        searchInput.value = "";
        renderCredentials();
    });
}

async function initializeExtension() {
    const account = await getStoredAccount();
    const session = await getStoredSession();

    if (account && session?.username === account.username) {
        currentUser = account.username;
        welcomeText.textContent = `Welcome back, ${currentUser}.`;
        showAppView();
        await loadCredentials();
        updateSaveButtonState();
        return;
    }

    showAuthView(account ? "login" : "register");
}

function switchAuthTab(tabName) {
    const showLogin = tabName === "login";
    loginTab.classList.toggle("active", showLogin);
    registerTab.classList.toggle("active", !showLogin);
    loginForm.classList.toggle("hidden", !showLogin);
    registerForm.classList.toggle("hidden", showLogin);
    showAuthMessage("", false);
}

async function handleRegister(event) {
    event.preventDefault();

    const username = registerUsernameInput.value.trim();
    const password = registerPasswordInput.value;
    const confirmPassword = registerConfirmPasswordInput.value;
    const existingAccount = await getStoredAccount();

    if (existingAccount) {
        showAuthMessage("An extension account already exists. Please log in.", true);
        switchAuthTab("login");
        return;
    }

    const validationError = validateAuthInput(username, password, true);
    if (validationError) {
        showAuthMessage(validationError, true);
        return;
    }

    if (password !== confirmPassword) {
        showAuthMessage("Passwords do not match", true);
        return;
    }

    const account = {
        username,
        passwordHash: await hashValue(password)
    };

    await setStoredValue("local", ACCOUNT_KEY, account);
    await setStoredValue("local", SESSION_KEY, {
        username
    });

    currentUser = username;
    registerForm.reset();
    updateStrengthMeter("", registerStrengthMeter, registerStrengthFill, registerStrengthText);
    welcomeText.textContent = `Welcome back, ${currentUser}.`;
    showAuthMessage("Account created successfully.", false);
    showAppView();
    await loadCredentials();
    updateSaveButtonState();
}

async function handleLogin(event) {
    event.preventDefault();

    const username = loginUsernameInput.value.trim();
    const password = loginPasswordInput.value;
    const validationError = validateAuthInput(username, password, false);
    if (validationError) {
        showAuthMessage(validationError, true);
        return;
    }

    const account = await getStoredAccount();
    if (!account) {
        showAuthMessage("No account found. Please register first.", true);
        switchAuthTab("register");
        return;
    }

    const passwordHash = await hashValue(password);
    if (account.username !== username || account.passwordHash !== passwordHash) {
        showAuthMessage("Invalid username or password", true);
        return;
    }

    await setStoredValue("local", SESSION_KEY, {
        username
    });

    currentUser = username;
    loginForm.reset();
    welcomeText.textContent = `Welcome back, ${currentUser}.`;
    showAppView();
    await loadCredentials();
    updateSaveButtonState();
}

async function handleLogout() {
    await removeStoredValue("local", SESSION_KEY);
    currentUser = null;
    allCredentials = [];
    visiblePasswords = new Set();
    credentialList.innerHTML = "";
    searchInput.value = "";
    loginForm.reset();
    registerForm.reset();
    credentialForm.reset();
    updateStrengthMeter("", registerStrengthMeter, registerStrengthFill, registerStrengthText);
    updateStrengthMeter("", strengthMeter, strengthFill, strengthText);
    showAuthView("login");
}

function showAuthView(defaultTab) {
    authView.classList.remove("hidden");
    appView.classList.add("hidden");
    switchAuthTab(defaultTab);
}

function showAppView() {
    authView.classList.add("hidden");
    appView.classList.remove("hidden");
    showAuthMessage("", false);
}

async function handleSaveCredential(event) {
    event.preventDefault();

    const siteName = siteNameInput.value.trim();
    const siteUsername = siteUsernameInput.value.trim();
    const password = sitePasswordInput.value;
    const notes = siteNotesInput.value.trim();
    const validationError = validateCredential(siteName, siteUsername, password);
    if (validationError) {
        showFormMessage(validationError, true);
        return;
    }

    const credential = {
        id: crypto.randomUUID(),
        siteName,
        siteUsername,
        password,
        notes,
        createdAt: new Date().toISOString()
    };

    allCredentials.unshift(credential);
    await saveCredentials();
    credentialForm.reset();
    updateStrengthMeter("", strengthMeter, strengthFill, strengthText);
    updateSaveButtonState();
    togglePasswordButton.textContent = "Show";
    sitePasswordInput.type = "password";
    showFormMessage("Credential saved successfully.", false);
    showVaultMessage("Vault updated successfully.", false);
    renderCredentials();
}

function handleGeneratePassword() {
    sitePasswordInput.value = generateStrongPassword();
    sitePasswordInput.type = "text";
    togglePasswordButton.textContent = "Hide";
    updateStrengthMeter(sitePasswordInput.value, strengthMeter, strengthFill, strengthText);
    updateSaveButtonState();
    showFormMessage("Strong password generated.", false);
}

function togglePasswordInput(input, button) {
    const shouldReveal = input.type === "password";
    input.type = shouldReveal ? "text" : "password";
    button.textContent = shouldReveal ? "Hide" : "Show";
}

function updateSaveButtonState() {
    saveButton.disabled = !siteNameInput.value.trim() || !siteUsernameInput.value.trim() || !sitePasswordInput.value;
}

function validateAuthInput(username, password, requireStrongPassword) {
    if (!username) {
        return "Username cannot be empty";
    }

    if (!password) {
        return "Password cannot be empty";
    }

    if (!isValidInput(username)) {
        return "Invalid input";
    }

    if (requireStrongPassword) {
        const passwordValidation = validatePassword(password);
        if (passwordValidation) {
            return passwordValidation;
        }
    }

    return "";
}

function validateCredential(siteName, siteUsername, password) {
    if (!siteName) {
        return "Site name cannot be empty";
    }

    if (!siteUsername) {
        return "Username cannot be empty";
    }

    if (!password) {
        return "Password cannot be empty";
    }

    if (!isValidInput(siteName) || !isValidInput(siteUsername)) {
        return "Invalid input";
    }

    return validatePassword(password);
}

function validatePassword(password) {
    if (password.length < 8) {
        return "Password must be at least 8 characters";
    }

    return "";
}

function updateStrengthMeter(password, wrapper, fill, text) {
    if (!password) {
        wrapper.classList.add("hidden");
        fill.style.width = "0%";
        text.textContent = "";
        return;
    }

    wrapper.classList.remove("hidden");
    const checks = getPasswordChecks(password);
    const passedChecks = Object.values(checks).filter(Boolean).length;

    let state = { width: "34%", color: "#ff6b6b", label: "Weak" };
    if (passedChecks === 4) {
        state = { width: "100%", color: "#00f5d4", label: "Strong" };
    } else if (passedChecks >= 2) {
        state = { width: "67%", color: "#ffd166", label: "Medium" };
    }

    fill.style.width = state.width;
    fill.style.background = state.color;
    text.textContent = `Password Strength: ${state.label}`;
}

function getPasswordChecks(password) {
    return {
        length: password.length >= 8,
        uppercase: /[A-Z]/.test(password),
        number: /[0-9]/.test(password),
        special: /[^A-Za-z0-9]/.test(password)
    };
}

function generateStrongPassword() {
    const uppercase = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    const lowercase = "abcdefghijkmnopqrstuvwxyz";
    const numbers = "23456789";
    const special = "!@#$%^&*?";
    const allCharacters = uppercase + lowercase + numbers + special;
    const characters = [
        pickRandom(uppercase),
        pickRandom(lowercase),
        pickRandom(numbers),
        pickRandom(special)
    ];

    while (characters.length < 12) {
        characters.push(pickRandom(allCharacters));
    }

    return shuffle(characters).join("");
}

function pickRandom(characters) {
    const index = Math.floor(Math.random() * characters.length);
    return characters[index];
}

function shuffle(characters) {
    const copy = [...characters];

    for (let index = copy.length - 1; index > 0; index -= 1) {
        const swapIndex = Math.floor(Math.random() * (index + 1));
        const current = copy[index];
        copy[index] = copy[swapIndex];
        copy[swapIndex] = current;
    }

    return copy;
}

async function getStoredAccount() {
    return getStoredValue("local", ACCOUNT_KEY);
}

async function getStoredSession() {
    return getStoredValue("local", SESSION_KEY);
}

function currentCredentialsKey() {
    return `${CREDENTIALS_PREFIX}${currentUser || "guest"}`;
}

async function loadCredentials() {
    const credentials = await getStoredValue("local", currentCredentialsKey());
    allCredentials = Array.isArray(credentials) ? credentials : [];
    renderCredentials();
}

async function saveCredentials() {
    await setStoredValue("local", currentCredentialsKey(), allCredentials);
}

function renderCredentials() {
    const keyword = searchInput.value.trim().toLowerCase();
    const filteredCredentials = allCredentials.filter(credential => {
        if (!keyword) {
            return true;
        }

        return credential.siteName.toLowerCase().includes(keyword) ||
            credential.siteUsername.toLowerCase().includes(keyword);
    });

    credentialCount.textContent = `${filteredCredentials.length} item${filteredCredentials.length === 1 ? "" : "s"}`;

    if (!allCredentials.length) {
        credentialList.innerHTML = `
            <div class="empty-state">
                <h3>Your vault is empty</h3>
                <p>Add your first credential to start using Dark Vault in Chrome.</p>
            </div>
        `;
        showVaultMessage("No credentials saved yet.", false);
        return;
    }

    if (!filteredCredentials.length) {
        credentialList.innerHTML = `
            <div class="empty-state">
                <h3>No matching credentials</h3>
                <p>Try another site name or username in the search box.</p>
            </div>
        `;
        showVaultMessage("No matching credentials found.", true);
        return;
    }

    credentialList.innerHTML = filteredCredentials.map(credential => {
        const isVisible = visiblePasswords.has(credential.id);
        const passwordText = isVisible ? escapeHtml(credential.password) : maskPassword(credential.password);

        return `
            <article class="credential-card">
                <div class="credential-card-head">
                    <div>
                        <h3 class="credential-title">${escapeHtml(credential.siteName)}</h3>
                        <p class="credential-subtitle">${escapeHtml(credential.siteUsername)}</p>
                    </div>
                </div>
                <div class="credential-password-row">
                    <span class="password-badge">${passwordText}</span>
                    <button type="button" class="mini-button" data-toggle-id="${credential.id}">${isVisible ? "Hide" : "Show"}</button>
                </div>
                <p class="credential-notes">${escapeHtml(credential.notes || "No notes added.")}</p>
                <div class="credential-actions">
                    <button type="button" class="mini-button" data-copy-id="${credential.id}">Copy</button>
                    <button type="button" class="mini-button" data-delete-id="${credential.id}">Delete</button>
                </div>
            </article>
        `;
    }).join("");

    bindCardButtons();
    showVaultMessage(`Showing ${filteredCredentials.length} credential(s).`, false);
}

function bindCardButtons() {
    document.querySelectorAll("[data-toggle-id]").forEach(button => {
        button.addEventListener("click", () => {
            const credentialId = button.dataset.toggleId;
            if (visiblePasswords.has(credentialId)) {
                visiblePasswords.delete(credentialId);
            } else {
                visiblePasswords.add(credentialId);
            }
            renderCredentials();
        });
    });

    document.querySelectorAll("[data-copy-id]").forEach(button => {
        button.addEventListener("click", async () => {
            const credential = allCredentials.find(item => item.id === button.dataset.copyId);
            if (!credential) {
                return;
            }

            await navigator.clipboard.writeText(credential.password);
            showVaultMessage(`Password copied for ${credential.siteName}.`, false);
        });
    });

    document.querySelectorAll("[data-delete-id]").forEach(button => {
        button.addEventListener("click", async () => {
            const credentialId = button.dataset.deleteId;
            allCredentials = allCredentials.filter(item => item.id !== credentialId);
            visiblePasswords.delete(credentialId);
            await saveCredentials();
            renderCredentials();
            showVaultMessage("Credential deleted successfully.", false);
        });
    });
}

function maskPassword(password) {
    return "*".repeat(Math.max(8, (password || "").length));
}

function showAuthMessage(message, isError) {
    authMessage.textContent = message;
    authMessage.classList.toggle("error", isError);
    authMessage.classList.toggle("success", Boolean(message) && !isError);
}

function showFormMessage(message, isError) {
    formMessage.textContent = message;
    formMessage.classList.toggle("error", isError);
    formMessage.classList.toggle("success", Boolean(message) && !isError);
}

function showVaultMessage(message, isError) {
    vaultMessage.textContent = message;
    vaultMessage.classList.toggle("error", isError);
    vaultMessage.classList.toggle("success", Boolean(message) && !isError);
}

async function hashValue(value) {
    const data = new TextEncoder().encode(value);
    const hashBuffer = await crypto.subtle.digest("SHA-256", data);
    return Array.from(new Uint8Array(hashBuffer))
        .map(byte => byte.toString(16).padStart(2, "0"))
        .join("");
}

function getStorageArea(areaName) {
    if (typeof chrome === "undefined" || !chrome.storage) {
        return null;
    }

    if (areaName === "session" && chrome.storage.session) {
        return chrome.storage.session;
    }

    return chrome.storage.local || null;
}

function getStoredValue(areaName, key) {
    const storageArea = getStorageArea(areaName);
    if (!storageArea) {
        return Promise.resolve(readBrowserFallback(key));
    }

    return new Promise(resolve => {
        storageArea.get([key], result => {
            resolve(result ? result[key] || null : null);
        });
    });
}

function setStoredValue(areaName, key, value) {
    const storageArea = getStorageArea(areaName);
    if (!storageArea) {
        writeBrowserFallback(key, value);
        return Promise.resolve();
    }

    return new Promise(resolve => {
        storageArea.set({ [key]: value }, resolve);
    });
}

function removeStoredValue(areaName, key) {
    const storageArea = getStorageArea(areaName);
    if (!storageArea) {
        localStorage.removeItem(key);
        return Promise.resolve();
    }

    return new Promise(resolve => {
        storageArea.remove(key, resolve);
    });
}

function readBrowserFallback(key) {
    try {
        const rawValue = localStorage.getItem(key);
        return rawValue ? JSON.parse(rawValue) : null;
    } catch (error) {
        return null;
    }
}

function writeBrowserFallback(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

function isValidInput(value) {
    return !/[<>]/.test(value);
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
