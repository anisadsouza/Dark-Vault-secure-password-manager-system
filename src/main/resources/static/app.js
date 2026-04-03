const authView = document.getElementById("auth-view");
const appView = document.getElementById("app-view");
const loginTab = document.getElementById("login-tab");
const registerTab = document.getElementById("register-tab");
const loginForm = document.getElementById("login-form");
const registerForm = document.getElementById("register-form");
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
const adminPanel = document.getElementById("admin-panel");
const userList = document.getElementById("user-list");
const credentialModal = document.getElementById("credential-modal");
const closeModalButton = document.getElementById("close-modal-button");
const credentialForm = document.getElementById("credential-form");
const credentialIdInput = document.getElementById("credential-id");
const modalTitle = document.getElementById("modal-title");
const siteNameInput = document.getElementById("site-name");
const siteUsernameInput = document.getElementById("site-username");
const sitePasswordInput = document.getElementById("site-password");
const siteNotesInput = document.getElementById("site-notes");

let currentSession = null;
let currentCredentials = [];

document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    checkSession();
});

function bindEvents() {
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
    credentialModal.addEventListener("click", event => {
        if (event.target === credentialModal) {
            closeCredentialModal();
        }
    });
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && !credentialModal.classList.contains("hidden")) {
            closeCredentialModal();
        }
    });
}

async function handleLogin(event) {
    event.preventDefault();
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
        loadUsers()
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
    const query = search ? `?search=${encodeURIComponent(search)}` : "";
    const response = await fetch(`/api/credentials${query}`);
    const result = await response.json();

    currentCredentials = result.items || [];
    renderCredentialGrid(currentCredentials);
    statCredentials.textContent = String(currentCredentials.length);
    statSites.textContent = String(new Set(currentCredentials.map(item => item.siteName)).size);

    if (search) {
        showDashboardMessage(`Showing ${currentCredentials.length} matching credential(s).`, false);
    } else {
        showDashboardMessage(currentCredentials.length ? "Vault synchronized." : "No credentials stored yet.", false);
    }
}

async function loadSummary() {
    const response = await fetch("/api/summary");
    const result = await response.json();
    renderSummary(result.items || []);
}

async function loadUsers() {
    if (!currentSession?.canManageUsers) {
        adminPanel.classList.add("hidden");
        userList.innerHTML = "";
        return;
    }

    const response = await fetch("/api/users");
    const result = await response.json();
    const users = result.items || [];
    adminPanel.classList.remove("hidden");
    userList.innerHTML = users.map(user => `
        <div class="user-row">
            <strong>${escapeHtml(user.username)}</strong>
            <div>${escapeHtml(user.displayRole)}</div>
        </div>
    `).join("");
}

function renderCredentialGrid(credentials) {
    if (!credentials.length) {
        credentialGrid.innerHTML = `
            <div class="preview-card">
                <p class="eyebrow">EMPTY VAULT</p>
                <h3>No credentials found</h3>
                <p>Add a credential to start filling the dashboard.</p>
            </div>
        `;
        return;
    }

    credentialGrid.innerHTML = credentials.map(credential => `
        <article class="credential-card">
            <p class="eyebrow">${escapeHtml(credential.siteName)}</p>
            <h4>${escapeHtml(credential.siteUsername)}</h4>
            <p>${escapeHtml(credential.notes || "No notes added.")}</p>
            <div class="password-line">
                <span id="password-${credential.credentialId}" data-password="${escapeHtml(credential.password)}">${maskPassword(credential.password)}</span>
                <button class="icon-button" type="button" onclick="togglePassword(${credential.credentialId})">Unlock</button>
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

function clearSearch() {
    searchInput.value = "";
    loadCredentials();
}

function openCredentialModal(credential = null) {
    credentialForm.reset();
    credentialIdInput.value = credential?.credentialId || "";
    modalTitle.textContent = credential ? "Edit Credential" : "Add Credential";
    siteNameInput.value = credential?.siteName || "";
    siteUsernameInput.value = credential?.siteUsername || "";
    sitePasswordInput.value = credential?.password || "";
    siteNotesInput.value = credential?.notes || "";
    credentialModal.classList.remove("hidden");
}

function closeCredentialModal() {
    credentialModal.classList.add("hidden");
}

async function handleCredentialSave(event) {
    event.preventDefault();
    const formData = new FormData(credentialForm);
    const body = new URLSearchParams(formData);
    const isEdit = Boolean(credentialIdInput.value);

    const response = await fetch("/api/credentials", {
        method: isEdit ? "PUT" : "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        },
        body
    });

    const result = await response.json();
    if (!response.ok) {
        showDashboardMessage(result.error || "Unable to save credential.", true);
        return;
    }

    closeCredentialModal();
    await Promise.all([loadCredentials(searchInput.value.trim()), loadSummary()]);
    showDashboardMessage(isEdit ? "Credential updated successfully." : "Credential added successfully.", false);
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
