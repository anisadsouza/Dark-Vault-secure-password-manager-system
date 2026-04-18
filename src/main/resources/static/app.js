const authView = document.getElementById("auth-view");
const appView = document.getElementById("app-view");
const loginTab = document.getElementById("login-tab");
const registerTab = document.getElementById("register-tab");
const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
const registerUsernameInput = registerForm.querySelector('input[name="username"]');
const registerPasswordInput = document.getElementById("register-password");
const registerGenerateButton = document.getElementById("register-generate-button");
const registerSubmitButton = document.getElementById("register-submit-button");
const authMessage = document.getElementById("auth-message");
const welcomeName = document.getElementById("welcome-name");
const welcomeRole = document.getElementById("welcome-role");
const logoutButton = document.getElementById("logout-button");
const searchInput = document.getElementById("search-input");
const searchButton = document.getElementById("search-button");
const clearButton = document.getElementById("clear-button");
const addButton = document.getElementById("add-button");
const credentialGrid = document.getElementById("credential-grid");
const summaryStrip = document.getElementById("summary-strip");
const dashboardMessage = document.getElementById("dashboard-message");
const statCredentials = document.getElementById("stat-credentials");
const statSites = document.getElementById("stat-sites");
const statDocuments = document.getElementById("stat-documents");
const credentialModal = document.getElementById("credential-modal");
const closeModalButton = document.getElementById("close-modal-button");
const credentialForm = document.getElementById("credential-form");
const credentialIdInput = document.getElementById("credential-id");
const modalTitle = document.getElementById("modal-title");
const siteNameInput = document.getElementById("site-name");
const siteUsernameInput = document.getElementById("site-username");
const sitePasswordInput = document.getElementById("site-password");
const siteNotesInput = document.getElementById("site-notes");
const credentialGenerateButton = document.getElementById("credential-generate-button");
const credentialSaveButton = document.getElementById("credential-save-button");
const credentialFormMessage = document.getElementById("credential-form-message");
const registerStrength = document.getElementById("register-strength");
const registerStrengthFill = document.getElementById("register-strength-fill");
const registerStrengthText = document.getElementById("register-strength-text");
const credentialStrength = document.getElementById("credential-strength");
const credentialStrengthFill = document.getElementById("credential-strength-fill");
const credentialStrengthText = document.getElementById("credential-strength-text");
const documentForm = document.getElementById("document-form");
const documentTitleInput = document.getElementById("document-title");
const documentCategoryInput = document.getElementById("document-category");
const documentFileInput = document.getElementById("document-file");
const documentNotesInput = document.getElementById("document-notes");
const documentSaveButton = document.getElementById("document-save-button");
const documentFormMessage = document.getElementById("document-form-message");
const documentSearchInput = document.getElementById("document-search-input");
const documentSearchButton = document.getElementById("document-search-button");
const documentClearButton = document.getElementById("document-clear-button");
const documentMessage = document.getElementById("document-message");
const documentGrid = document.getElementById("document-grid");

const SUPPORTED_DOCUMENT_EXTENSIONS = [".pdf", ".doc", ".docx", ".csv", ".txt", ".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg"];
const SUPPORTED_DOCUMENT_TYPES = [
    "application/pdf",
    "application/msword",
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "text/csv",
    "application/csv",
    "text/plain",
    "image/jpeg",
    "image/png",
    "image/gif",
    "image/webp",
    "image/svg+xml"
];

let currentSession = null;
let currentCredentials = [];
let currentDocuments = [];

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    checkSession();
});

function bindEvents() {
    bindPasswordToggleButtons();
    loginTab.addEventListener("click", () => switchTab("login"));
    registerTab.addEventListener("click", () => switchTab("register"));
    loginForm.addEventListener("submit", handleLogin);
    registerForm.addEventListener("submit", handleRegister);
    logoutButton.addEventListener("click", handleLogout);
    searchButton.addEventListener("click", () => loadCredentials(searchInput.value.trim()));
    clearButton.addEventListener("click", clearSearch);
    searchInput.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadCredentials(searchInput.value.trim());
        }
    });
    addButton.addEventListener("click", () => openCredentialModal());
    closeModalButton.addEventListener("click", closeCredentialModal);
    credentialForm.addEventListener("submit", handleCredentialSave);
    registerUsernameInput.addEventListener("input", updateRegisterButtonState);
    credentialModal.addEventListener("click", event => {
        if (event.target === credentialModal) {
            closeCredentialModal();
        }
    });
    registerPasswordInput.addEventListener("input", () => {
        updateStrengthMeter(registerPasswordInput.value, registerStrength, registerStrengthFill, registerStrengthText);
        updateRegisterButtonState();
    });
    registerGenerateButton.addEventListener("click", generateRegisterPassword);
    siteNameInput.addEventListener("input", updateCredentialSaveButtonState);
    siteUsernameInput.addEventListener("input", updateCredentialSaveButtonState);
    sitePasswordInput.addEventListener("input", () => {
        updateStrengthMeter(sitePasswordInput.value, credentialStrength, credentialStrengthFill, credentialStrengthText);
        updateCredentialSaveButtonState();
    });
    credentialGenerateButton.addEventListener("click", generateCredentialPassword);
    documentForm.addEventListener("submit", handleDocumentSave);
    documentTitleInput.addEventListener("input", updateDocumentSaveButtonState);
    documentFileInput.addEventListener("change", updateDocumentSaveButtonState);
    documentSearchButton.addEventListener("click", () => loadDocuments(documentSearchInput.value.trim()));
    documentClearButton.addEventListener("click", clearDocumentSearch);
    documentSearchInput.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            event.preventDefault();
            loadDocuments(documentSearchInput.value.trim());
        }
    });
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && !credentialModal.classList.contains("hidden")) {
            closeCredentialModal();
        }
    });
    updateRegisterButtonState();
    updateCredentialSaveButtonState();
    updateDocumentSaveButtonState();
}

async function handleLogin(event) {
    event.preventDefault();
    const username = loginForm.username.value.trim();
    const password = loginForm.password.value;

    const loginValidationError = validateAuthInput(username, password, false);
    if (loginValidationError) {
        showAuthMessage(loginValidationError, true);
        return;
    }

    const formData = new FormData(loginForm);
    const body = new URLSearchParams(formData);

    try {
        const response = await fetch("/api/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        const result = await response.json();
        if (!response.ok) {
            showAuthMessage(result.error || "Login failed.", true);
            return;
        }

        loginForm.reset();
        await showDashboard(result);
        showAuthMessage("", false);
    } catch (error) {
        showAuthMessage("Unable to reach the local web server.", true);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const username = registerForm.username.value.trim();
    const password = registerForm.password.value;

    const registerValidationError = validateAuthInput(username, password, true);
    if (registerValidationError) {
        showAuthMessage(registerValidationError, true);
        return;
    }

    const formData = new FormData(registerForm);
    const body = new URLSearchParams(formData);

    try {
        const response = await fetch("/api/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        const result = await response.json();
        if (!response.ok) {
            showAuthMessage(result.error || "Registration failed.", true);
            return;
        }

        registerForm.reset();
        updateStrengthMeter("", registerStrength, registerStrengthFill, registerStrengthText);
        updateRegisterButtonState();
        switchTab("login");
        showAuthMessage(result.message || "User registered successfully.", false);
    } catch (error) {
        showAuthMessage("Unable to reach the local web server.", true);
    }
}

async function handleLogout() {
    await fetch("/api/logout", { method: "POST" });
    authView.classList.remove("hidden");
    appView.classList.add("hidden");
    currentSession = null;
    currentCredentials = [];
    currentDocuments = [];
}

async function checkSession() {
    try {
        const response = await fetch("/api/session");
        const result = await response.json();

        if (!result.authenticated) {
            authView.classList.remove("hidden");
            appView.classList.add("hidden");
            return;
        }

        await showDashboard(result);
    } catch (error) {
        showAuthMessage("Unable to load session.", true);
    }
}

async function showDashboard(session) {
    currentSession = session;
    authView.classList.add("hidden");
    appView.classList.remove("hidden");
    welcomeName.textContent = session.username || "Vault User";
    welcomeRole.textContent = session.displayRole || "Secure session";
    await Promise.all([
        loadCredentials(),
        loadSummary(),
        loadDocuments()
    ]);
}

function switchTab(tabName) {
    const isLogin = tabName === "login";
    loginTab.classList.toggle("active", isLogin);
    registerTab.classList.toggle("active", !isLogin);
    loginForm.classList.toggle("hidden", !isLogin);
    registerForm.classList.toggle("hidden", isLogin);
    showAuthMessage("", false);
}

function showAuthMessage(message, isError) {
    authMessage.textContent = message;
    authMessage.classList.toggle("error", isError);
    authMessage.classList.toggle("success", Boolean(message) && !isError);
}

async function loadCredentials(search = "") {
    try {
        const query = search ? `?search=${encodeURIComponent(search)}` : "";
        const response = await fetch(`/api/credentials${query}`);
        const result = await response.json();

        if (!response.ok) {
            showDashboardMessage(result.error || "Database error occurred.", true);
            return;
        }

        currentCredentials = result.items || [];
        renderCredentialGrid(currentCredentials);
        statCredentials.textContent = String(currentCredentials.length);
        statSites.textContent = String(new Set(currentCredentials.map(item => item.siteName)).size);
        showDashboardMessage(currentCredentials.length ? "Vault synchronized." : "Your vault is empty. Add your first credential.", false);
    } catch (error) {
        showDashboardMessage("Unable to load credentials.", true);
    }
}

async function loadSummary() {
    try {
        const response = await fetch("/api/summary");
        const result = await response.json();

        if (!response.ok) {
            summaryStrip.innerHTML = "";
            return;
        }

        renderSummary(result.items || []);
    } catch (error) {
        summaryStrip.innerHTML = "";
    }
}

async function loadDocuments(search = "") {
    try {
        const query = search ? `?search=${encodeURIComponent(search)}` : "";
        const response = await fetch(`/api/documents${query}`);
        const result = await response.json();

        if (!response.ok) {
            showDocumentMessage(result.error || "Unable to load documents.", true);
            return;
        }

        currentDocuments = result.items || [];
        renderDocumentGrid(currentDocuments);
        statDocuments.textContent = String(currentDocuments.length);

        if (currentDocuments.length) {
            showDocumentMessage(`Showing ${currentDocuments.length} document(s).`, false);
        } else {
            showDocumentMessage("No documents stored yet. Passwords remain the main vault above.", false);
        }
    } catch (error) {
        showDocumentMessage("Unable to load documents.", true);
    }
}

function renderCredentialGrid(credentials) {
    if (!credentials.length) {
        credentialGrid.innerHTML = `
            <div class="preview-card">
                <p class="eyebrow">EMPTY VAULT</p>
                <h3>Your vault is empty. Add your first credential.</h3>
                <p>Store website credentials here and keep them protected in one place.</p>
            </div>
        `;
        return;
    }

    credentialGrid.innerHTML = credentials.map(credential => `
        <article class="credential-card">
            <div>
                <p class="eyebrow">${escapeHtml(credential.siteName)}</p>
                <p>${escapeHtml(credential.notes || "No notes added.")}</p>
            </div>
            <h4>${escapeHtml(credential.siteUsername)}</h4>
            <div class="password-line">
                <span class="password-value" id="password-${credential.credentialId}" data-password="${escapeHtml(credential.password)}">${maskPassword(credential.password)}</span>
                <button class="icon-button eye-button" type="button" onclick="togglePassword(${credential.credentialId})" title="Show or hide password">👁</button>
            </div>
            <div class="card-actions">
                <button type="button" onclick="copyPassword(${credential.credentialId})">Copy</button>
                <button type="button" onclick="editCredential(${credential.credentialId})">Edit</button>
                <button type="button" onclick="deleteCredential(${credential.credentialId})">Delete</button>
            </div>
        </article>
    `).join("");
}

function renderSummary(items) {
    if (!items.length) {
        summaryStrip.innerHTML = "";
        return;
    }

    summaryStrip.innerHTML = items.map(item => `
        <div class="summary-pill" onclick="filterBySite('${escapeHtml(item.siteName)}')">${escapeHtml(item.siteName)} · ${item.count}</div>
    `).join("");
}

function renderDocumentGrid(documents) {
    if (!documents.length) {
        documentGrid.innerHTML = `
            <div class="preview-card">
                <p class="eyebrow">DOCUMENT VAULT</p>
                <h3>No documents stored yet.</h3>
                <p>Add PDFs, Word files, CSVs, TXT files, or images here after managing your passwords above.</p>
            </div>
        `;
        return;
    }

    documentGrid.innerHTML = documents.map(documentItem => `
        <article class="document-card">
            <div>
                <h4>${escapeHtml(documentItem.title)}</h4>
                <p>${escapeHtml(documentItem.notes || "No notes added.")}</p>
            </div>
            <div>
                <p>${escapeHtml(documentItem.originalFileName)}</p>
                <p>${formatFileSize(documentItem.fileSizeBytes)} · ${escapeHtml(documentItem.mimeType || "Unknown type")}</p>
            </div>
            <p>${escapeHtml(documentItem.category || "Uncategorized")}</p>
            <div class="document-actions">
                <a href="/api/documents/download?id=${documentItem.documentId}" target="_blank" rel="noopener">Download</a>
                <button type="button" onclick="deleteDocument(${documentItem.documentId})">Delete</button>
            </div>
        </article>
    `).join("");
}

function clearSearch() {
    searchInput.value = "";
    loadCredentials();
}

function clearDocumentSearch() {
    documentSearchInput.value = "";
    loadDocuments();
}

function openCredentialModal(credential = null) {
    credentialForm.reset();
    credentialIdInput.value = credential?.credentialId || "";
    modalTitle.textContent = credential ? "Edit Credential" : "Add Credential";
    siteNameInput.value = credential?.siteName || "";
    siteUsernameInput.value = credential?.siteUsername || "";
    sitePasswordInput.value = credential?.password || "";
    siteNotesInput.value = credential?.notes || "";
    showCredentialFormMessage("", false);
    updateStrengthMeter(sitePasswordInput.value, credentialStrength, credentialStrengthFill, credentialStrengthText);
    updateCredentialSaveButtonState();
    credentialModal.classList.remove("hidden");
}

function closeCredentialModal() {
    credentialModal.classList.add("hidden");
}

async function handleCredentialSave(event) {
    event.preventDefault();
    const validationError = validateCredentialInput(
        siteNameInput.value.trim(),
        siteUsernameInput.value.trim(),
        sitePasswordInput.value
    );

    if (validationError) {
        showCredentialFormMessage(validationError, true);
        return;
    }

    const formData = new FormData(credentialForm);
    const body = new URLSearchParams(formData);
    const isEdit = Boolean(credentialIdInput.value);
    credentialSaveButton.disabled = true;

    try {
        const response = await fetch("/api/credentials", {
            method: isEdit ? "PUT" : "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });

        const result = await response.json();
        if (!response.ok) {
            showCredentialFormMessage(result.error || "Invalid input", true);
            updateCredentialSaveButtonState();
            return;
        }

        closeCredentialModal();
        await Promise.all([loadCredentials(searchInput.value.trim()), loadSummary()]);
        showDashboardMessage(isEdit ? "Credential updated successfully." : "Credential saved successfully.", false);
    } catch (error) {
        showCredentialFormMessage("Unable to save credential.", true);
    } finally {
        updateCredentialSaveButtonState();
    }
}

async function deleteCredential(credentialId) {
    const confirmed = window.confirm("Delete this credential?");
    if (!confirmed) {
        return;
    }

    const response = await fetch(`/api/credentials?id=${credentialId}`, { method: "DELETE" });
    const result = await response.json();
    if (!response.ok) {
        showDashboardMessage(result.error || "Delete failed.", true);
        return;
    }

    await Promise.all([loadCredentials(searchInput.value.trim()), loadSummary()]);
    showDashboardMessage("Credential deleted successfully.", false);
}

async function handleDocumentSave(event) {
    event.preventDefault();
    event.stopPropagation();

    const selectedFile = documentFileInput.files[0];
    const validationError = validateDocumentInput(documentTitleInput.value.trim(), selectedFile);
    if (validationError) {
        showDocumentFormMessage(validationError, true);
        return;
    }

    documentSaveButton.disabled = true;

    try {
        const fileData = await readFileAsDataUrl(selectedFile);
        const body = new URLSearchParams();
        body.set("title", documentTitleInput.value.trim());
        body.set("fileName", selectedFile.name);
        body.set("mimeType", selectedFile.type || "application/octet-stream");
        body.set("category", documentCategoryInput.value.trim());
        body.set("notes", documentNotesInput.value.trim());
        body.set("fileData", fileData);

        const response = await fetch("/api/documents", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body
        });
        const result = await response.json();

        if (!response.ok) {
            showDocumentFormMessage(result.error || "Unable to save document.", true);
            updateDocumentSaveButtonState();
            return;
        }

        documentForm.reset();
        updateDocumentSaveButtonState();
        await loadDocuments(documentSearchInput.value.trim());
        showDocumentFormMessage("Document saved successfully.", false);
    } catch (error) {
        showDocumentFormMessage("Unable to save document.", true);
    } finally {
        updateDocumentSaveButtonState();
    }
}

async function deleteDocument(documentId) {
    const confirmed = window.confirm("Delete this document?");
    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`/api/documents?id=${documentId}`, { method: "DELETE" });
        const result = await response.json();
        if (!response.ok) {
            showDocumentMessage(result.error || "Delete failed.", true);
            return;
        }

        await loadDocuments(documentSearchInput.value.trim());
        showDocumentMessage("Document deleted successfully.", false);
    } catch (error) {
        showDocumentMessage("Unable to delete document.", true);
    }
}

function editCredential(credentialId) {
    const credential = currentCredentials.find(item => item.credentialId === credentialId);
    if (!credential) {
        return;
    }

    openCredentialModal(credential);
}

function togglePassword(credentialId) {
    const element = document.getElementById(`password-${credentialId}`);
    if (!element) {
        return;
    }

    const actualPassword = element.dataset.password || "";
    if (element.dataset.visible === "true") {
        element.textContent = maskPassword(actualPassword);
        element.dataset.visible = "false";
        return;
    }

    element.textContent = actualPassword;
    element.dataset.visible = "true";
}

async function copyPassword(credentialId) {
    const credential = currentCredentials.find(item => item.credentialId === credentialId);
    if (!credential) {
        return;
    }

    try {
        await navigator.clipboard.writeText(credential.password);
        showDashboardMessage(`Password copied for ${credential.siteName}.`, false);
    } catch (error) {
        showDashboardMessage("Clipboard copy failed.", true);
    }
}

function filterBySite(siteName) {
    searchInput.value = siteName;
    loadCredentials(siteName);
}

function maskPassword(password) {
    return "*".repeat(Math.max(8, (password || "").length));
}

function showDashboardMessage(message, isError) {
    dashboardMessage.textContent = message;
    dashboardMessage.classList.toggle("error", isError);
    dashboardMessage.classList.toggle("success", Boolean(message) && !isError);
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

    let config = { width: "34%", label: "Weak", color: "#ff6b6b" };
    if (passedChecks === 4) {
        config = { width: "100%", label: "Strong", color: "#00f5d4" };
    } else if (passedChecks >= 2) {
        config = { width: "67%", label: "Medium", color: "#ffd166" };
    }

    fill.style.width = config.width;
    fill.style.background = config.color;
    text.textContent = `Password Strength: ${config.label}`;
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
        const passwordValidationMessage = validatePassword(password);
        if (passwordValidationMessage) {
            return passwordValidationMessage;
        }
    }

    return "";
}

function validateCredentialInput(siteName, siteUsername, password) {
    if (!siteName) {
        return "Site name cannot be empty";
    }

    if (!siteUsername) {
        return "Account username cannot be empty";
    }

    if (!password) {
        return "Password cannot be empty";
    }

    if (!isValidInput(siteName) || !isValidInput(siteUsername)) {
        return "Invalid input";
    }

    return validatePassword(password);
}

function validateDocumentInput(title, selectedFile) {
    if (!title) {
        return "Document title cannot be empty";
    }

    if (!selectedFile) {
        return "Please select a file";
    }

    if (!isValidInput(title)) {
        return "Invalid input";
    }

    if (!isSupportedDocumentFile(selectedFile)) {
        return "Unsupported file type. Use PDF, Word, CSV, TXT, JPG, PNG, GIF, WebP, or SVG.";
    }

    if (selectedFile.size > 10 * 1024 * 1024) {
        return "File must be 10 MB or smaller";
    }

    return "";
}

function isSupportedDocumentFile(file) {
    const fileName = file.name.toLowerCase();
    const fileType = (file.type || "").toLowerCase();
    const hasSupportedExtension = SUPPORTED_DOCUMENT_EXTENSIONS.some(extension => fileName.endsWith(extension));
    const hasSupportedMimeType = SUPPORTED_DOCUMENT_TYPES.includes(fileType);
    return hasSupportedExtension || hasSupportedMimeType;
}

function validatePassword(password) {
    if (password.length < 8) {
        return "Password must be at least 8 characters";
    }

    return "";
}

function getPasswordChecks(password) {
    return {
        length: password.length >= 8,
        uppercase: /[A-Z]/.test(password),
        number: /[0-9]/.test(password),
        special: /[^A-Za-z0-9]/.test(password)
    };
}

function isValidInput(value) {
    return !/[<>]/.test(value);
}

function showCredentialFormMessage(message, isError) {
    credentialFormMessage.textContent = message;
    credentialFormMessage.classList.toggle("error", isError);
    credentialFormMessage.classList.toggle("success", Boolean(message) && !isError);
}

function showDocumentFormMessage(message, isError) {
    documentFormMessage.textContent = message;
    documentFormMessage.classList.toggle("error", isError);
    documentFormMessage.classList.toggle("success", Boolean(message) && !isError);
}

function showDocumentMessage(message, isError) {
    documentMessage.textContent = message;
    documentMessage.classList.toggle("error", isError);
    documentMessage.classList.toggle("success", Boolean(message) && !isError);
}

function bindPasswordToggleButtons() {
    const toggleButtons = document.querySelectorAll("[data-password-toggle]");

    for (const button of toggleButtons) {
        button.addEventListener("click", () => {
            const targetId = button.dataset.target;
            const input = document.getElementById(targetId);
            if (!input) {
                return;
            }

            const shouldReveal = input.type === "password";
            input.type = shouldReveal ? "text" : "password";
            button.textContent = shouldReveal ? "Hide" : "Show";
        });
    }
}

function updateRegisterButtonState() {
    registerSubmitButton.disabled = !registerUsernameInput.value.trim() || !registerPasswordInput.value;
}

function updateCredentialSaveButtonState() {
    credentialSaveButton.disabled = !siteNameInput.value.trim() || !siteUsernameInput.value.trim() || !sitePasswordInput.value;
}

function updateDocumentSaveButtonState() {
    documentSaveButton.disabled = !documentTitleInput.value.trim() || !documentFileInput.files.length;
}

function generateRegisterPassword() {
    registerPasswordInput.value = generateStrongPassword();
    updateStrengthMeter(registerPasswordInput.value, registerStrength, registerStrengthFill, registerStrengthText);
    updateRegisterButtonState();
    showAuthMessage("Strong password generated.", false);
}

function generateCredentialPassword() {
    sitePasswordInput.value = generateStrongPassword();
    updateStrengthMeter(sitePasswordInput.value, credentialStrength, credentialStrengthFill, credentialStrengthText);
    updateCredentialSaveButtonState();
    showCredentialFormMessage("Strong password generated.", false);
}

function generateStrongPassword() {
    const uppercase = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    const lowercase = "abcdefghijkmnopqrstuvwxyz";
    const numbers = "23456789";
    const special = "!@#$%^&*?";
    const allCharacters = uppercase + lowercase + numbers + special;
    const characters = [
        randomCharacter(uppercase),
        randomCharacter(lowercase),
        randomCharacter(numbers),
        randomCharacter(special)
    ];

    while (characters.length < 12) {
        characters.push(randomCharacter(allCharacters));
    }

    return shuffleCharacters(characters).join("");
}

function randomCharacter(characters) {
    const index = Math.floor(Math.random() * characters.length);
    return characters[index];
}

function shuffleCharacters(characters) {
    const copy = [...characters];

    for (let index = copy.length - 1; index > 0; index -= 1) {
        const swapIndex = Math.floor(Math.random() * (index + 1));
        const current = copy[index];
        copy[index] = copy[swapIndex];
        copy[swapIndex] = current;
    }

    return copy;
}

function readFileAsDataUrl(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(String(reader.result || ""));
        reader.onerror = () => reject(reader.error);
        reader.readAsDataURL(file);
    });
}

function formatFileSize(sizeInBytes) {
    const size = Number(sizeInBytes) || 0;
    if (size < 1024) {
        return `${size} B`;
    }
    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
    }
    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}

function escapeHtml(value) {
    return String(value || "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}

window.togglePassword = togglePassword;
window.copyPassword = copyPassword;
window.editCredential = editCredential;
window.deleteCredential = deleteCredential;
window.filterBySite = filterBySite;
window.deleteDocument = deleteDocument;
