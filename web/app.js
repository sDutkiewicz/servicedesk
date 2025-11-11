import { BASE_API_URL } from './config.js';

// Global state with user roles
let currentUser = {
    role: null, // 'admin', 'technician', 'user'
    userId: null,
    name: null
};
// Global state
let currentTicketId = null;

// Role-based menu configuration
const menuConfig = {
    admin: [
        { view: 'tickets', label: 'Zgłoszenia' },
        { view: 'new-ticket', label: 'Nowe zgłoszenie' },
        { view: 'reports', label: 'Raporty' },
        { view: 'manage', label: 'Zarządzanie' }
    ],
    technician: [
        { view: 'tickets', label: 'Moje zgłoszenia' },
        { view: 'reports', label: 'Raporty' }
    ],
    user: [
        { view: 'tickets', label: 'Moje zgłoszenia' },
        { view: 'new-ticket', label: 'Nowe zgłoszenie' }
    ]
};

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    // Just init tabs, wait for login
    initTabs();
});

// Navigation
function initNavigation() {
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const view = e.target.dataset.view;
            showView(view);
        });
    });
}

window.showView = function(viewName) {
    // Hide all views
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    // Show selected view
    document.getElementById(`view-${viewName}`).classList.add('active');

    // Update nav
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.toggle('active', link.dataset.view === viewName);
    });

    // Load data for specific views
    if (viewName === 'manage') {
        loadManageData();
    }
};

// Tabs
function initTabs() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(`tab-${tab}`).classList.add('active');
        });
    });
}

// Load tickets
window.loadTickets = async function() {
    const status = document.getElementById('filter-status').value;
    const technicianId = document.getElementById('filter-technician').value;
    const departmentId = document.getElementById('filter-department').value;

    let url = `${BASE_API_URL}/tickets?`;
    if (status) url += `status=${status}&`;
    if (technicianId) url += `technicianId=${technicianId}&`;
    if (departmentId) url += `departmentId=${departmentId}&`;

    try {
        const response = await fetch(url);
        const tickets = await response.json();
        displayTickets(tickets);
    } catch (error) {
        document.getElementById('tickets-list').innerHTML =
            '<div class="loading">Błąd ładowania zgłoszeń</div>';
    }
};

function displayTickets(tickets) {
    const container = document.getElementById('tickets-list');

    if (tickets.length === 0) {
        container.innerHTML = '<div class="loading">Brak zgłoszeń</div>';
        return;
    }

    container.innerHTML = tickets.map(ticket => `
        <div class="ticket-card" onclick="showTicketDetails(${ticket.id})">
            <div class="ticket-header">
                <div class="ticket-title">${ticket.title}</div>
                <div class="ticket-badges">
                    <span class="badge badge-${ticket.status.toLowerCase()}">${translateStatus(ticket.status)}</span>
                    <span class="badge badge-${ticket.priority.toLowerCase()}">${translatePriority(ticket.priority)}</span>
                </div>
            </div>
            <div class="ticket-info">
                <span>👤 ${ticket.reporter?.firstName} ${ticket.reporter?.lastName}</span>
                <span>🔧 ${ticket.technician ? `${ticket.technician.firstName} ${ticket.technician.lastName}` : 'Nieprzypisany'}</span>
                <span>📁 ${ticket.category?.name || 'Brak'}</span>
                <span>📅 ${formatDate(ticket.createdAt)}</span>
            </div>
        </div>
    `).join('');
}

// Show ticket details
window.showTicketDetails = async function(ticketId) {
    currentTicketId = ticketId;

    try {
        const response = await fetch(`${BASE_API_URL}/tickets/${ticketId}`);
        const ticket = await response.json();

        const commentsResponse = await fetch(`${BASE_API_URL}/comments/ticket/${ticketId}`);
        const comments = await commentsResponse.json();

        displayTicketDetails(ticket, comments);
        showView('ticket-details');
    } catch (error) {
        alert('Błąd ładowania szczegółów zgłoszenia');
    }
};

function displayTicketDetails(ticket, comments) {
    const container = document.getElementById('ticket-details-content');

    container.innerHTML = `
        <div class="ticket-details">
            <div class="details-header">
                <h2>${ticket.title}</h2>
                <div class="ticket-badges">
                    <span class="badge badge-${ticket.status.toLowerCase()}">${translateStatus(ticket.status)}</span>
                    <span class="badge badge-${ticket.priority.toLowerCase()}">${translatePriority(ticket.priority)}</span>
                </div>
            </div>

            <div class="details-grid">
                <div class="detail-item">
                    <div class="detail-label">Zgłaszający</div>
                    <div class="detail-value">${ticket.reporter?.firstName} ${ticket.reporter?.lastName}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Technik</div>
                    <div class="detail-value">${ticket.technician ? `${ticket.technician.firstName} ${ticket.technician.lastName}` : 'Nieprzypisany'}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Kategoria</div>
                    <div class="detail-value">${ticket.category?.name || 'Brak'}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Data utworzenia</div>
                    <div class="detail-value">${formatDate(ticket.createdAt)}</div>
                </div>
                ${ticket.closedAt ? `
                <div class="detail-item">
                    <div class="detail-label">Data zamknięcia</div>
                    <div class="detail-value">${formatDate(ticket.closedAt)}</div>
                </div>` : ''}
            </div>

            <div class="detail-item">
                <div class="detail-label">Opis</div>
                <div class="detail-value">${ticket.description || 'Brak opisu'}</div>
            </div>

            <div style="margin-top: 2rem; display: flex; gap: 1rem;">
                <button class="btn btn-primary" onclick="changeStatus('${ticket.id}', 'IN_PROGRESS')">▶ W realizacji</button>
                <button class="btn btn-success" onclick="changeStatus('${ticket.id}', 'RESOLVED')">✓ Rozwiązane</button>
                <button class="btn btn-danger" onclick="changeStatus('${ticket.id}', 'CLOSED')">✕ Zamknij</button>
                <button class="btn" onclick="openCommentModal(${ticket.id})">💬 Dodaj komentarz</button>
            </div>

            <div class="comments-section">
                <h3>Komentarze (${comments.length})</h3>
                ${comments.map(c => `
                    <div class="comment-card">
                        <div class="comment-header">
                            <span class="comment-author">${c.author?.firstName} ${c.author?.lastName}</span>
                            <span class="comment-date">${formatDate(c.createdAt)}</span>
                        </div>
                        <div>${c.content}</div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

// Change ticket status
window.changeStatus = async function(ticketId, newStatus) {
    try {
        await fetch(`${BASE_API_URL}/tickets/${ticketId}/status/${newStatus}`, {
            method: 'POST'
        });
        alert('Status zmieniony!');
        showTicketDetails(ticketId);
    } catch (error) {
        alert('Błąd zmiany statusu');
    }
};

// Load filters
async function loadFilters() {
    try {
        // Load technicians
        const techResponse = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await techResponse.json();
        const techSelect = document.getElementById('filter-technician');
        technicians.forEach(t => {
            const option = document.createElement('option');
            option.value = t.id;
            option.textContent = `${t.firstName} ${t.lastName}`;
            techSelect.appendChild(option);
        });

        // Load departments
        const deptResponse = await fetch(`${BASE_API_URL}/departments`);
        const departments = await deptResponse.json();
        const deptSelect = document.getElementById('filter-department');
        departments.forEach(d => {
            const option = document.createElement('option');
            option.value = d.id;
            option.textContent = d.name;
            deptSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading filters:', error);
    }
}

// Initialize forms
function initForms() {
    // New ticket form
    document.getElementById('form-new-ticket').addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            title: document.getElementById('ticket-title').value,
            description: document.getElementById('ticket-description').value,
            priority: document.getElementById('ticket-priority').value,
            reporterId: parseInt(document.getElementById('ticket-reporter').value),
            categoryId: parseInt(document.getElementById('ticket-category').value) || null,
            technicianId: parseInt(document.getElementById('ticket-technician').value) || null
        };

        try {
            await fetch(`${BASE_API_URL}/tickets`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            alert('Zgłoszenie utworzone!');
            e.target.reset();
            showView('tickets');
            loadTickets();
        } catch (error) {
            alert('Błąd tworzenia zgłoszenia');
        }
    });

    // Load form options
    loadFormOptions();

    // Comment form
    document.getElementById('form-comment').addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            ticketId: parseInt(document.getElementById('comment-ticket-id').value),
            authorId: parseInt(document.getElementById('comment-author').value),
            content: document.getElementById('comment-content').value
        };

        try {
            await fetch(`${BASE_API_URL}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            alert('Komentarz dodany!');
            closeModal('comment');
            showTicketDetails(data.ticketId);
        } catch (error) {
            alert('Błąd dodawania komentarza');
        }
    });
}

async function loadFormOptions() {
    try {
        // Users
        const usersResponse = await fetch(`${BASE_API_URL}/users`);
        const users = await usersResponse.json();
        const userSelects = [
            document.getElementById('ticket-reporter'),
            document.getElementById('comment-author')
        ];
        userSelects.forEach(select => {
            users.forEach(u => {
                const option = document.createElement('option');
                option.value = u.id;
                option.textContent = `${u.firstName} ${u.lastName}`;
                select.appendChild(option);
            });
        });

        // Technicians
        const techResponse = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await techResponse.json();
        const techSelect = document.getElementById('ticket-technician');
        technicians.forEach(t => {
            const option = document.createElement('option');
            option.value = t.id;
            option.textContent = `${t.firstName} ${t.lastName}`;
            techSelect.appendChild(option);
        });

        // Categories
        const catResponse = await fetch(`${BASE_API_URL}/categories`);
        const categories = await catResponse.json();
        const catSelect = document.getElementById('ticket-category');
        categories.forEach(c => {
            const option = document.createElement('option');
            option.value = c.id;
            option.textContent = c.name;
            catSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading form options:', error);
    }
}

// Reports
window.loadReport = async function(groupBy) {
    try {
        const response = await fetch(`${BASE_API_URL}/reports/tickets-count?groupBy=${groupBy}`);
        const data = await response.json();

        const container = document.getElementById(`report-${groupBy}`);
        container.innerHTML = `
            <table class="report-table">
                <thead>
                    <tr>
                        <th>${groupBy === 'department' ? 'Dział' : 'Technik'}</th>
                        <th>Otwarte</th>
                        <th>Zamknięte</th>
                        <th>Razem</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.map(row => `
                        <tr>
                            <td>${row.group}</td>
                            <td>${row.open}</td>
                            <td>${row.closed}</td>
                            <td><strong>${row.open + row.closed}</strong></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        alert('Błąd ładowania raportu');
    }
};

window.loadAvgTime = async function() {
    try {
        const response = await fetch(`${BASE_API_URL}/reports/avg-resolution-time`);
        const data = await response.json();

        const container = document.getElementById('report-avgtime');
        const hours = Math.round(data.avgHours);
        const days = Math.floor(hours / 24);
        const remainingHours = hours % 24;

        container.innerHTML = `
            <div style="font-size: 2rem; color: #667eea; margin: 1rem 0;">
                ${days}d ${remainingHours}h
            </div>
            <p>Średni czas rozwiązania: <strong>${hours} godzin</strong></p>
            <p>Liczba zamkniętych zgłoszeń: <strong>${data.count}</strong></p>
        `;
    } catch (error) {
        alert('Błąd ładowania raportu');
    }
};

// Management
async function loadManageData() {
    loadUsers();
    loadTechnicians();
    loadDepartments();
    loadCategories();
}

async function loadUsers() {
    try {
        const response = await fetch(`${BASE_API_URL}/users`);
        const users = await response.json();

        const container = document.getElementById('users-list');
        container.innerHTML = users.map(u => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${u.firstName} ${u.lastName}</h4>
                    <p>${u.email} | ${u.department?.name || 'Brak działu'}</p>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteUser(${u.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

async function loadTechnicians() {
    try {
        const response = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await response.json();

        const container = document.getElementById('technicians-list');
        container.innerHTML = technicians.map(t => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${t.firstName} ${t.lastName}</h4>
                    <p>${t.email} | ${t.department?.name || 'Brak działu'}</p>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteTechnician(${t.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading technicians:', error);
    }
}

async function loadDepartments() {
    try {
        const response = await fetch(`${BASE_API_URL}/departments`);
        const departments = await response.json();

        const container = document.getElementById('departments-list');
        container.innerHTML = departments.map(d => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${d.name}</h4>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteDepartment(${d.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

async function loadCategories() {
    try {
        const response = await fetch(`${BASE_API_URL}/categories`);
        const categories = await response.json();

        const container = document.getElementById('categories-list');
        container.innerHTML = categories.map(c => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${c.name}</h4>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteCategory(${c.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Modal
window.openCommentModal = function(ticketId) {
    document.getElementById('comment-ticket-id').value = ticketId;
    document.getElementById('modal-comment').classList.add('active');
};

window.closeModal = function(modalName) {
    document.getElementById(`modal-${modalName}`).classList.remove('active');
};

// Utility functions
function translateStatus(status) {
    const map = {
        'OPEN': 'Otwarte',
        'IN_PROGRESS': 'W realizacji',
        'RESOLVED': 'Rozwiązane',
        'CLOSED': 'Zamknięte'
    };
    return map[status] || status;
}

function translatePriority(priority) {
    const map = {
        'LOW': 'Niski',
        'MEDIUM': 'Średni',
        'HIGH': 'Wysoki',
        'CRITICAL': 'Krytyczny'
    };
    return map[priority] || priority;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Delete functions (simple implementation)
window.deleteUser = async function(id) {
    if (!confirm('Czy na pewno usunąć użytkownika?')) return;
    try {
        await fetch(`${BASE_API_URL}/users/${id}`, { method: 'DELETE' });
        loadUsers();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteTechnician = async function(id) {
    if (!confirm('Czy na pewno usunąć technika?')) return;
    try {
        await fetch(`${BASE_API_URL}/technicians/${id}`, { method: 'DELETE' });
        loadTechnicians();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteDepartment = async function(id) {
    if (!confirm('Czy na pewno usunąć dział?')) return;
    try {
        await fetch(`${BASE_API_URL}/departments/${id}`, { method: 'DELETE' });
        loadDepartments();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteCategory = async function(id) {
    if (!confirm('Czy na pewno usunąć kategorię?')) return;
    try {
        await fetch(`${BASE_API_URL}/categories/${id}`, { method: 'DELETE' });
        loadCategories();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.showAddForm = function(type) {
    alert(`Formularz dodawania ${type} - do zaimplementowania`);
};

// Login function
window.login = function(role) {
    currentUser.role = role;
    currentUser.name = getRoleName(role);

    // Hide login screen, show app
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('app').classList.remove('hidden');

    // Setup UI based on role
    setupUI();
    loadTickets();
    loadFilters();
    initForms();
};

// Logout function
window.logout = function() {
    currentUser = { role: null, userId: null, name: null };
    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('app').classList.add('hidden');
    showView('tickets');
};

function getRoleName(role) {
    const names = {
        admin: 'Administrator',
        technician: 'Technik IT',
        user: 'Użytkownik'
    };
    return names[role];
}
function initNavigation() {
function setupUI() {
    // Set user role display
    document.getElementById('current-user-role').textContent = currentUser.name;

    // Build navigation menu
    const navLinks = document.getElementById('nav-links');
    navLinks.innerHTML = menuConfig[currentUser.role].map(item => `
        <a href="#" class="nav-link ${item.view === 'tickets' ? 'active' : ''}" data-view="${item.view}">${item.label}</a>
    `).join('');

    // Re-init navigation after building menu
    initNavigation();

    // Init tabs
    initTabs();

    // Show first view
    showView('tickets');
}
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const view = e.target.dataset.view;
            showView(view);
        });
    });
}

window.showView = function(viewName) {
    // Hide all views
    document.querySelectorAll('.view').forEach(v => v.classList.remove('active'));
    // Show selected view
    document.getElementById(`view-${viewName}`).classList.add('active');

    // Update nav
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.toggle('active', link.dataset.view === viewName);
    });

    // Load data for specific views
    if (viewName === 'manage') {
        loadManageData();
    }
};

// Tabs
function initTabs() {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const tab = btn.dataset.tab;
            document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
            btn.classList.add('active');
            document.getElementById(`tab-${tab}`).classList.add('active');
        });
    });
}

// Load tickets
window.loadTickets = async function() {
    const status = document.getElementById('filter-status').value;
    const technicianId = document.getElementById('filter-technician').value;
    const departmentId = document.getElementById('filter-department').value;

    let url = `${BASE_API_URL}/tickets?`;
    if (status) url += `status=${status}&`;
    if (technicianId) url += `technicianId=${technicianId}&`;
    if (departmentId) url += `departmentId=${departmentId}&`;

    try {
        const response = await fetch(url);
        const tickets = await response.json();
        displayTickets(tickets);
    } catch (error) {
        document.getElementById('tickets-list').innerHTML =
            '<div class="loading">Błąd ładowania zgłoszeń</div>';
    }
};

function displayTickets(tickets) {
    const container = document.getElementById('tickets-list');

    if (tickets.length === 0) {
        container.innerHTML = '<div class="loading">Brak zgłoszeń</div>';
        return;
    }

    container.innerHTML = tickets.map(ticket => `
        <div class="ticket-card" onclick="showTicketDetails(${ticket.id})">
            <div class="ticket-header">
                <div class="ticket-title">${ticket.title}</div>
                <div class="ticket-badges">
                    <span class="badge badge-${ticket.status.toLowerCase()}">${translateStatus(ticket.status)}</span>
                    <span class="badge badge-${ticket.priority.toLowerCase()}">${translatePriority(ticket.priority)}</span>
                </div>
            </div>
            <div class="ticket-info">
                <span>👤 ${ticket.reporter?.firstName} ${ticket.reporter?.lastName}</span>
                <span>🔧 ${ticket.technician ? `${ticket.technician.firstName} ${ticket.technician.lastName}` : 'Nieprzypisany'}</span>
                <span>📁 ${ticket.category?.name || 'Brak'}</span>
                <span>📅 ${formatDate(ticket.createdAt)}</span>
            </div>
        </div>
    `).join('');
}

// Show ticket details
window.showTicketDetails = async function(ticketId) {
    currentTicketId = ticketId;

    try {
        const response = await fetch(`${BASE_API_URL}/tickets/${ticketId}`);
        const ticket = await response.json();

        const commentsResponse = await fetch(`${BASE_API_URL}/comments/ticket/${ticketId}`);
        const comments = await commentsResponse.json();

        displayTicketDetails(ticket, comments);
        showView('ticket-details');
    } catch (error) {
        alert('Błąd ładowania szczegółów zgłoszenia');
    }
};

function displayTicketDetails(ticket, comments) {
    const container = document.getElementById('ticket-details-content');

    container.innerHTML = `
        <div class="ticket-details">
            <div class="details-header">
                <h2>${ticket.title}</h2>
                <div class="ticket-badges">
                    <span class="badge badge-${ticket.status.toLowerCase()}">${translateStatus(ticket.status)}</span>
                    <span class="badge badge-${ticket.priority.toLowerCase()}">${translatePriority(ticket.priority)}</span>
                </div>
            </div>

            <div class="details-grid">
                <div class="detail-item">
                    <div class="detail-label">Zgłaszający</div>
                    <div class="detail-value">${ticket.reporter?.firstName} ${ticket.reporter?.lastName}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Technik</div>
                    <div class="detail-value">${ticket.technician ? `${ticket.technician.firstName} ${ticket.technician.lastName}` : 'Nieprzypisany'}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Kategoria</div>
                    <div class="detail-value">${ticket.category?.name || 'Brak'}</div>
                </div>
                <div class="detail-item">
                    <div class="detail-label">Data utworzenia</div>
                    <div class="detail-value">${formatDate(ticket.createdAt)}</div>
                </div>
                ${ticket.closedAt ? `
                <div class="detail-item">
                    <div class="detail-label">Data zamknięcia</div>
                    <div class="detail-value">${formatDate(ticket.closedAt)}</div>
                </div>` : ''}
            </div>

            <div class="detail-item">
                <div class="detail-label">Opis</div>
                <div class="detail-value">${ticket.description || 'Brak opisu'}</div>
            </div>

            <div style="margin-top: 2rem; display: flex; gap: 1rem;">
                <button class="btn btn-primary" onclick="changeStatus('${ticket.id}', 'IN_PROGRESS')">▶ W realizacji</button>
                <button class="btn btn-success" onclick="changeStatus('${ticket.id}', 'RESOLVED')">✓ Rozwiązane</button>
                <button class="btn btn-danger" onclick="changeStatus('${ticket.id}', 'CLOSED')">✕ Zamknij</button>
                <button class="btn" onclick="openCommentModal(${ticket.id})">💬 Dodaj komentarz</button>
            </div>

            <div class="comments-section">
                <h3>Komentarze (${comments.length})</h3>
                ${comments.map(c => `
                    <div class="comment-card">
                        <div class="comment-header">
                            <span class="comment-author">${c.author?.firstName} ${c.author?.lastName}</span>
                            <span class="comment-date">${formatDate(c.createdAt)}</span>
                        </div>
                        <div>${c.content}</div>
                    </div>
                `).join('')}
            </div>
        </div>
    `;
}

// Change ticket status
window.changeStatus = async function(ticketId, newStatus) {
    try {
        await fetch(`${BASE_API_URL}/tickets/${ticketId}/status/${newStatus}`, {
            method: 'POST'
        });
        alert('Status zmieniony!');
        showTicketDetails(ticketId);
    } catch (error) {
        alert('Błąd zmiany statusu');
    }
};

// Load filters
async function loadFilters() {
    try {
        // Load technicians
        const techResponse = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await techResponse.json();
        const techSelect = document.getElementById('filter-technician');
        technicians.forEach(t => {
            const option = document.createElement('option');
            option.value = t.id;
            option.textContent = `${t.firstName} ${t.lastName}`;
            techSelect.appendChild(option);
        });

        // Load departments
        const deptResponse = await fetch(`${BASE_API_URL}/departments`);
        const departments = await deptResponse.json();
        const deptSelect = document.getElementById('filter-department');
        departments.forEach(d => {
            const option = document.createElement('option');
            option.value = d.id;
            option.textContent = d.name;
            deptSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading filters:', error);
    }
}

// Initialize forms
function initForms() {
    // New ticket form
    document.getElementById('form-new-ticket').addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            title: document.getElementById('ticket-title').value,
            description: document.getElementById('ticket-description').value,
            priority: document.getElementById('ticket-priority').value,
            reporterId: parseInt(document.getElementById('ticket-reporter').value),
            categoryId: parseInt(document.getElementById('ticket-category').value) || null,
            technicianId: parseInt(document.getElementById('ticket-technician').value) || null
        };

        try {
            await fetch(`${BASE_API_URL}/tickets`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            alert('Zgłoszenie utworzone!');
            e.target.reset();
            showView('tickets');
            loadTickets();
        } catch (error) {
            alert('Błąd tworzenia zgłoszenia');
        }
    });

    // Load form options
    loadFormOptions();

    // Comment form
    document.getElementById('form-comment').addEventListener('submit', async (e) => {
        e.preventDefault();

        const data = {
            ticketId: parseInt(document.getElementById('comment-ticket-id').value),
            authorId: parseInt(document.getElementById('comment-author').value),
            content: document.getElementById('comment-content').value
        };

        try {
            await fetch(`${BASE_API_URL}/comments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
            alert('Komentarz dodany!');
            closeModal('comment');
            showTicketDetails(data.ticketId);
        } catch (error) {
            alert('Błąd dodawania komentarza');
        }
    });
}

async function loadFormOptions() {
    try {
        // Users
        const usersResponse = await fetch(`${BASE_API_URL}/users`);
        const users = await usersResponse.json();
        const userSelects = [
            document.getElementById('ticket-reporter'),
            document.getElementById('comment-author')
        ];
        userSelects.forEach(select => {
            users.forEach(u => {
                const option = document.createElement('option');
                option.value = u.id;
                option.textContent = `${u.firstName} ${u.lastName}`;
                select.appendChild(option);
            });
        });

        // Technicians
        const techResponse = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await techResponse.json();
        const techSelect = document.getElementById('ticket-technician');
        technicians.forEach(t => {
            const option = document.createElement('option');
            option.value = t.id;
            option.textContent = `${t.firstName} ${t.lastName}`;
            techSelect.appendChild(option);
        });

        // Categories
        const catResponse = await fetch(`${BASE_API_URL}/categories`);
        const categories = await catResponse.json();
        const catSelect = document.getElementById('ticket-category');
        categories.forEach(c => {
            const option = document.createElement('option');
            option.value = c.id;
            option.textContent = c.name;
            catSelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading form options:', error);
    }
}

// Reports
window.loadReport = async function(groupBy) {
    try {
        const response = await fetch(`${BASE_API_URL}/reports/tickets-count?groupBy=${groupBy}`);
        const data = await response.json();

        const container = document.getElementById(`report-${groupBy}`);
        container.innerHTML = `
            <table class="report-table">
                <thead>
                    <tr>
                        <th>${groupBy === 'department' ? 'Dział' : 'Technik'}</th>
                        <th>Otwarte</th>
                        <th>Zamknięte</th>
                        <th>Razem</th>
                    </tr>
                </thead>
                <tbody>
                    ${data.map(row => `
                        <tr>
                            <td>${row.group}</td>
                            <td>${row.open}</td>
                            <td>${row.closed}</td>
                            <td><strong>${row.open + row.closed}</strong></td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;
    } catch (error) {
        alert('Błąd ładowania raportu');
    }
};

window.loadAvgTime = async function() {
    try {
        const response = await fetch(`${BASE_API_URL}/reports/avg-resolution-time`);
        const data = await response.json();

        const container = document.getElementById('report-avgtime');
        const hours = Math.round(data.avgHours);
        const days = Math.floor(hours / 24);
        const remainingHours = hours % 24;

        container.innerHTML = `
            <div style="font-size: 2rem; color: #667eea; margin: 1rem 0;">
                ${days}d ${remainingHours}h
            </div>
            <p>Średni czas rozwiązania: <strong>${hours} godzin</strong></p>
            <p>Liczba zamkniętych zgłoszeń: <strong>${data.count}</strong></p>
        `;
    } catch (error) {
        alert('Błąd ładowania raportu');
    }
};

// Management
async function loadManageData() {
    loadUsers();
    loadTechnicians();
    loadDepartments();
    loadCategories();
}

async function loadUsers() {
    try {
        const response = await fetch(`${BASE_API_URL}/users`);
        const users = await response.json();

        const container = document.getElementById('users-list');
        container.innerHTML = users.map(u => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${u.firstName} ${u.lastName}</h4>
                    <p>${u.email} | ${u.department?.name || 'Brak działu'}</p>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteUser(${u.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

async function loadTechnicians() {
    try {
        const response = await fetch(`${BASE_API_URL}/technicians`);
        const technicians = await response.json();

        const container = document.getElementById('technicians-list');
        container.innerHTML = technicians.map(t => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${t.firstName} ${t.lastName}</h4>
                    <p>${t.email} | ${t.department?.name || 'Brak działu'}</p>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteTechnician(${t.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading technicians:', error);
    }
}

async function loadDepartments() {
    try {
        const response = await fetch(`${BASE_API_URL}/departments`);
        const departments = await response.json();

        const container = document.getElementById('departments-list');
        container.innerHTML = departments.map(d => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${d.name}</h4>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteDepartment(${d.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading departments:', error);
    }
}

async function loadCategories() {
    try {
        const response = await fetch(`${BASE_API_URL}/categories`);
        const categories = await response.json();

        const container = document.getElementById('categories-list');
        container.innerHTML = categories.map(c => `
            <div class="data-item">
                <div class="data-item-info">
                    <h4>${c.name}</h4>
                </div>
                <div class="data-item-actions">
                    <button class="btn btn-small btn-danger" onclick="deleteCategory(${c.id})">Usuń</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Modal
window.openCommentModal = function(ticketId) {
    document.getElementById('comment-ticket-id').value = ticketId;
    document.getElementById('modal-comment').classList.add('active');
};

window.closeModal = function(modalName) {
    document.getElementById(`modal-${modalName}`).classList.remove('active');
};

// Utility functions
function translateStatus(status) {
    const map = {
        'OPEN': 'Otwarte',
        'IN_PROGRESS': 'W realizacji',
        'RESOLVED': 'Rozwiązane',
        'CLOSED': 'Zamknięte'
    };
    return map[status] || status;
}

function translatePriority(priority) {
    const map = {
        'LOW': 'Niski',
        'MEDIUM': 'Średni',
        'HIGH': 'Wysoki',
        'CRITICAL': 'Krytyczny'
    };
    return map[priority] || priority;
}

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleString('pl-PL', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Delete functions (simple implementation)
window.deleteUser = async function(id) {
    if (!confirm('Czy na pewno usunąć użytkownika?')) return;
    try {
        await fetch(`${BASE_API_URL}/users/${id}`, { method: 'DELETE' });
        loadUsers();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteTechnician = async function(id) {
    if (!confirm('Czy na pewno usunąć technika?')) return;
    try {
        await fetch(`${BASE_API_URL}/technicians/${id}`, { method: 'DELETE' });
        loadTechnicians();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteDepartment = async function(id) {
    if (!confirm('Czy na pewno usunąć dział?')) return;
    try {
        await fetch(`${BASE_API_URL}/departments/${id}`, { method: 'DELETE' });
        loadDepartments();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.deleteCategory = async function(id) {
    if (!confirm('Czy na pewno usunąć kategorię?')) return;
    try {
        await fetch(`${BASE_API_URL}/categories/${id}`, { method: 'DELETE' });
        loadCategories();
    } catch (error) {
        alert('Błąd usuwania');
    }
};

window.showAddForm = function(type) {
    alert(`Formularz dodawania ${type} - do zaimplementowania`);
};

