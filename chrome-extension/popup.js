const STORAGE_KEY = "darkVaultCredentials";

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

let allCredentials = [];
let visiblePasswords = new Set();

document.addEventListener("DOMContentLoaded", async () => {
    bindEvents();
    await loadCredentials();
    updateSaveButtonState();
});

function bindEvents() {
    credentialForm.addEventListener("submit", handleSaveCredential);
    siteNameInput.addEventListener("input", updateSaveButtonState);
    siteUsernameInput.addEventListener("input", updateSaveButtonState);
    sitePasswordInput.addEventListener("input", () => {
        updateSaveButtonState();
        updateStrengthMeter(sitePasswordInput.value);
    });
    generateButton.addEventListener("click", handleGeneratePassword);
    togglePasswordButton.addEventListener("click", toggleFormPassword);
    searchInput.addEventListener("input", renderCredentials);
    clearButton.addEventListener("click", () => {
        searchInput.value = "";
        renderCredentials();
    });
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
    updateStrengthMeter("");
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
    updateStrengthMeter(sitePasswordInput.value);
    updateSaveButtonState();
    showFormMessage("Strong password generated.", false);
}

function toggleFormPassword() {
    const shouldReveal = sitePasswordInput.type === "password";
    sitePasswordInput.type = shouldReveal ? "text" : "password";
    togglePasswordButton.textContent = shouldReveal ? "Hide" : "Show";
}

function updateSaveButtonState() {
    saveButton.disabled = !siteNameInput.value.trim() || !siteUsernameInput.value.trim() || !sitePasswordInput.value;
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

    if (/[<>]/.test(siteName) || /[<>]/.test(siteUsername)) {
        return "Invalid input";
    }

    if (password.length < 8) {
        return "Password must be at least 8 characters";
    }

    return "";
}

function updateStrengthMeter(password) {
    if (!password) {
        strengthMeter.classList.add("hidden");
        strengthFill.style.width = "0%";
        strengthText.textContent = "";
        return;
    }

    strengthMeter.classList.remove("hidden");
    const checks = getPasswordChecks(password);
    const passedChecks = Object.values(checks).filter(Boolean).length;

    let state = { width: "34%", color: "#ff6b6b", label: "Weak" };
    if (passedChecks === 4) {
        state = { width: "100%", color: "#00f5d4", label: "Strong" };
    } else if (passedChecks >= 2) {
        state = { width: "67%", color: "#ffd166", label: "Medium" };
    }

    strengthFill.style.width = state.width;
    strengthFill.style.background = state.color;
    strengthText.textContent = `Password Strength: ${state.label}`;
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

async function loadCredentials() {
    const result = await chrome.storage.local.get([STORAGE_KEY]);
    allCredentials = Array.isArray(result[STORAGE_KEY]) ? result[STORAGE_KEY] : [];
    renderCredentials();
}

async function saveCredentials() {
    await chrome.storage.local.set({ [STORAGE_KEY]: allCredentials });
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

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
