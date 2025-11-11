package com.servicedesk.controller;

import com.servicedesk.service.AuthService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class HomeController {

    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping(value = {"/", "/index.html"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String index() {
        return """
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Service Desk - Logowanie</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
        }
        .login-container {
            background: white;
            padding: 3rem;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.3);
            width: 400px;
        }
        h1 {
            text-align: center;
            color: #667eea;
            margin-bottom: 0.5rem;
            font-size: 2.5rem;
        }
        .subtitle {
            text-align: center;
            color: #999;
            margin-bottom: 2rem;
        }
        .form-group {
            margin-bottom: 1.5rem;
        }
        label {
            display: block;
            margin-bottom: 0.5rem;
            color: #555;
            font-weight: 600;
        }
        input {
            width: 100%;
            padding: 0.8rem;
            border: 2px solid #e0e0e0;
            border-radius: 8px;
            font-size: 1rem;
            transition: border-color 0.3s;
        }
        input:focus {
            outline: none;
            border-color: #667eea;
        }
        button {
            width: 100%;
            padding: 1rem;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 8px;
            font-size: 1.1rem;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.3s, box-shadow 0.3s;
        }
        button:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }
        .error {
            background: #ffebee;
            color: #c62828;
            padding: 0.8rem;
            border-radius: 8px;
            margin-bottom: 1rem;
            display: none;
        }
        .error.show {
            display: block;
        }
        .accounts-info {
            margin-top: 2rem;
            padding: 1rem;
            background: #f5f5f5;
            border-radius: 8px;
            font-size: 0.85rem;
        }
        .accounts-info h3 {
            color: #667eea;
            margin-bottom: 0.5rem;
            font-size: 0.9rem;
        }
        .accounts-info p {
            margin: 0.3rem 0;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h1>🔧 Service Desk</h1>
        <p class="subtitle">System zarządzania zgłoszeniami IT</p>
        
        <div id="error-message" class="error"></div>
        
        <form id="loginForm">
            <div class="form-group">
                <label for="login">Login</label>
                <input type="text" id="login" name="login" required autofocus>
            </div>
            
            <div class="form-group">
                <label for="password">Hasło</label>
                <input type="password" id="password" name="password" required>
            </div>
            
            <button type="submit">Zaloguj się</button>
        </form>
        
        <div class="accounts-info">
            <h3>📝 Konta testowe:</h3>
            <p><strong>Administrator:</strong> admin / admin123</p>
            <p><strong>Użytkownik (HR):</strong> jan.kowalski / pass123</p>
            <p><strong>Użytkownik (IT):</strong> anna.nowak / pass123</p>
            <p><strong>Technik IT:</strong> tomasz.bialy / tech123</p>
            <p><strong>Technik IT:</strong> katarzyna.czarna / tech123</p>
        </div>
    </div>

    <script>
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const login = document.getElementById('login').value;
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('error-message');
            
            try {
                const response = await fetch('/api/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ login, password })
                });
                
                const data = await response.json();
                
                if (data.success) {
                    sessionStorage.setItem('user', JSON.stringify({
                        login: data.login,
                        role: data.role,
                        userId: data.userId,
                        technicianId: data.technicianId
                    }));
                    
                    // Przekieruj w zależności od roli
                    if (data.role === 'technician') {
                        window.location.href = '/technician.html';
                    } else if (data.role === 'user') {
                        window.location.href = '/user.html';
                    } else {
                        window.location.href = '/api/tickets';
                    }
                } else {
                    errorDiv.textContent = data.message || 'Błędny login lub hasło';
                    errorDiv.classList.add('show');
                }
            } catch (error) {
                errorDiv.textContent = 'Błąd połączenia: ' + error.message;
                errorDiv.classList.add('show');
            }
        });
    </script>
</body>
</html>
                """;
    }

    @PostMapping("/api/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String login = credentials.get("login");
        String password = credentials.get("password");

        AuthService.UserAccount account = authService.authenticate(login, password);
        if (account != null) {
            return ResponseEntity.ok(Map.of(
                "success", true,
                "role", account.role,
                "login", account.login,
                "userId", account.userId != null ? account.userId : 0,
                "technicianId", account.technicianId != null ? account.technicianId : 0
            ));
        }

        return ResponseEntity.ok(Map.of(
            "success", false,
            "message", "Błędny login lub hasło"
        ));
    }

    @GetMapping(value = "/technician.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String technicianPanel() {
        return """
<!DOCTYPE html>
<html lang="pl">
<head>
    <meta charset="UTF-8">
    <title>Panel Technika - Service Desk</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f5f7fa; color: #333; }
        .navbar { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 1rem 2rem; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .navbar h1 { font-size: 1.5rem; margin-bottom: 0.5rem; }
        .user-info { font-size: 0.9rem; opacity: 0.9; }
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 1.5rem; margin-bottom: 2rem; }
        .stat-card { background: white; padding: 1.5rem; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); text-align: center; }
        .stat-card h3 { color: #999; font-size: 0.9rem; margin-bottom: 0.5rem; }
        .stat-card .number { font-size: 2.5rem; font-weight: bold; color: #667eea; }
        .filters { background: white; padding: 1.5rem; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); margin-bottom: 2rem; display: flex; gap: 1rem; flex-wrap: wrap; align-items: center; }
        .filters label { font-weight: 600; margin-right: 0.5rem; }
        .filters select { padding: 0.6rem; border: 2px solid #e0e0e0; border-radius: 5px; min-width: 150px; }
        .btn { padding: 0.6rem 1.2rem; border: none; border-radius: 5px; cursor: pointer; font-weight: 600; transition: all 0.3s; }
        .btn-primary { background: #667eea; color: white; }
        .btn-primary:hover { background: #5568d3; transform: translateY(-2px); }
        .btn-success { background: #4caf50; color: white; }
        .btn-warning { background: #ff9800; color: white; }
        .btn-danger { background: #f44336; color: white; }
        .btn-small { padding: 0.4rem 0.8rem; font-size: 0.85rem; }
        .tickets-grid { display: grid; gap: 1.5rem; }
        .ticket-card { background: white; padding: 1.5rem; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); border-left: 4px solid #667eea; transition: all 0.3s; }
        .ticket-card:hover { transform: translateY(-3px); box-shadow: 0 5px 20px rgba(0,0,0,0.1); }
        .ticket-header { display: flex; justify-content: space-between; align-items: start; margin-bottom: 1rem; }
        .ticket-title { font-size: 1.3rem; font-weight: 600; color: #333; }
        .ticket-badges { display: flex; gap: 0.5rem; }
        .badge { padding: 0.3rem 0.8rem; border-radius: 20px; font-size: 0.75rem; font-weight: 600; }
        .badge-open { background: #e3f2fd; color: #1976d2; }
        .badge-in-progress { background: #fff3e0; color: #f57c00; }
        .badge-resolved { background: #e8f5e9; color: #388e3c; }
        .badge-closed { background: #f3e5f5; color: #7b1fa2; }
        .badge-low { background: #e3f2fd; color: #1976d2; }
        .badge-medium { background: #fff3e0; color: #f57c00; }
        .badge-high { background: #ffebee; color: #c62828; }
        .badge-critical { background: #fce4ec; color: #880e4f; }
        .ticket-info { display: flex; gap: 1.5rem; font-size: 0.9rem; color: #666; margin: 0.5rem 0; }
        .ticket-actions { display: flex; gap: 0.5rem; margin-top: 1rem; flex-wrap: wrap; }
        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); justify-content: center; align-items: center; }
        .modal.active { display: flex; }
        .modal-content { background: white; padding: 2rem; border-radius: 10px; max-width: 700px; width: 90%; max-height: 85vh; overflow-y: auto; }
        .close { float: right; font-size: 1.5rem; cursor: pointer; color: #999; line-height: 1; }
        .close:hover { color: #333; }
        .form-group { margin-bottom: 1.5rem; }
        .form-group label { display: block; margin-bottom: 0.5rem; font-weight: 600; color: #555; }
        .form-group textarea { width: 100%; padding: 0.8rem; border: 2px solid #e0e0e0; border-radius: 5px; font-family: inherit; resize: vertical; min-height: 100px; }
        .comments-section { margin-top: 2rem; padding-top: 1.5rem; border-top: 2px solid #eee; }
        .comment { background: #f9f9f9; padding: 1rem; border-radius: 8px; margin-bottom: 1rem; border-left: 3px solid #667eea; }
        .comment-header { display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.9rem; }
        .comment-author { font-weight: 600; color: #667eea; }
        .comment-date { color: #999; }
        .loading, .empty { text-align: center; padding: 3rem; color: #999; }
        .detail-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 1rem; margin: 1.5rem 0; }
        .detail-item { padding: 0.5rem; }
        .detail-label { font-size: 0.85rem; color: #999; margin-bottom: 0.3rem; }
        .detail-value { font-weight: 500; color: #333; }
    </style>
</head>
<body>
    <nav class="navbar">
        <h1>🔧 Panel Technika IT</h1>
        <div class="user-info">
            Zalogowany jako: <strong id="tech-name">Technik</strong> | 
            <a href="/" style="color: white; text-decoration: underline;">Wyloguj</a>
        </div>
    </nav>

    <div class="container">
        <div class="stats">
            <div class="stat-card">
                <h3>Przypisane do mnie</h3>
                <div class="number" id="stat-assigned">0</div>
            </div>
            <div class="stat-card">
                <h3>W realizacji</h3>
                <div class="number" id="stat-in-progress">0</div>
            </div>
            <div class="stat-card">
                <h3>Rozwiązane</h3>
                <div class="number" id="stat-resolved">0</div>
            </div>
        </div>

        <div class="filters">
            <label>Status: <select id="filter-status">
                <option value="">Wszystkie</option>
                <option value="OPEN">Otwarte</option>
                <option value="IN_PROGRESS" selected>W realizacji</option>
                <option value="RESOLVED">Rozwiązane</option>
                <option value="CLOSED">Zamknięte</option>
            </select></label>
            <label>Priorytet: <select id="filter-priority">
                <option value="">Wszystkie</option>
                <option value="CRITICAL">Krytyczny</option>
                <option value="HIGH">Wysoki</option>
                <option value="MEDIUM">Średni</option>
                <option value="LOW">Niski</option>
            </select></label>
            <button class="btn btn-primary" onclick="applyFilters()">Filtruj</button>
            <button class="btn btn-primary" onclick="loadTickets()">Odśwież</button>
        </div>

        <div id="tickets-container" class="tickets-grid">
            <div class="loading">Ładowanie zgłoszeń...</div>
        </div>
    </div>

    <div id="modal-details" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeModal()">&times;</span>
            <div id="modal-body"></div>
        </div>
    </div>

    <div id="modal-resolve" class="modal">
        <div class="modal-content">
            <span class="close" onclick="closeResolveModal()">&times;</span>
            <h2>Rozwiązywanie zgłoszenia</h2>
            <p style="margin: 1rem 0;">Dodaj komentarz opisujący wykonane działania:</p>
            <div class="form-group">
                <label for="resolve-comment">Komentarz*</label>
                <textarea id="resolve-comment" placeholder="Np. Zaktualizowano sterowniki drukarki, problem rozwiązany..." required></textarea>
            </div>
            <div style="display: flex; gap: 0.5rem;">
                <button class="btn btn-success" onclick="confirmResolve()">✓ Rozwiąż zgłoszenie</button>
                <button class="btn" onclick="closeResolveModal()">Anuluj</button>
            </div>
        </div>
    </div>

    <script>
        const API = 'http://localhost:8080/api';
        let user = null;
        let allTickets = [];
        let currentTicketId = null;
        
        window.onload = () => {
            const u = sessionStorage.getItem('user');
            if (!u) { window.location.href = '/'; return; }
            user = JSON.parse(u);
            if (user.role !== 'technician') { alert('Brak dostępu!'); window.location.href = '/'; return; }
            document.getElementById('tech-name').textContent = user.login;
            loadTickets();
        };
        
        async function loadTickets() {
            const container = document.getElementById('tickets-container');
            container.innerHTML = '<div class="loading">Ładowanie...</div>';
            try {
                const res = await fetch(API + '/tickets/assigned?technicianId=' + user.technicianId);
                allTickets = await res.json();
                updateStats();
                applyFilters();
            } catch(e) { container.innerHTML = '<div class="empty">Błąd ładowania zgłoszeń</div>'; }
        }
        
        function updateStats() {
            document.getElementById('stat-assigned').textContent = allTickets.length;
            document.getElementById('stat-in-progress').textContent = allTickets.filter(t => t.status === 'IN_PROGRESS').length;
            document.getElementById('stat-resolved').textContent = allTickets.filter(t => t.status === 'RESOLVED' || t.status === 'CLOSED').length;
        }
        
        function applyFilters() {
            const status = document.getElementById('filter-status').value;
            const priority = document.getElementById('filter-priority').value;
            const filtered = allTickets.filter(t => {
                if (status && t.status !== status) return false;
                if (priority && t.priority !== priority) return false;
                return true;
            });
            displayTickets(filtered);
        }
        
        function displayTickets(tickets) {
            const container = document.getElementById('tickets-container');
            if (tickets.length === 0) {
                container.innerHTML = '<div class="empty">Brak zgłoszeń</div>';
                return;
            }
            container.innerHTML = tickets.map(t => {
                const statusMap = {OPEN:'Otwarte',IN_PROGRESS:'W realizacji',RESOLVED:'Rozwiązane',CLOSED:'Zamknięte'};
                const prioMap = {LOW:'Niski',MEDIUM:'Średni',HIGH:'Wysoki',CRITICAL:'Krytyczny'};
                return `
                    <div class="ticket-card">
                        <div class="ticket-header">
                            <div class="ticket-title">${t.title}</div>
                            <div class="ticket-badges">
                                <span class="badge badge-${t.status.toLowerCase()}">${statusMap[t.status]}</span>
                                <span class="badge badge-${t.priority.toLowerCase()}">${prioMap[t.priority]}</span>
                            </div>
                        </div>
                        <div class="ticket-info">
                            <span>👤 ${t.reporter.firstName} ${t.reporter.lastName}</span>
                            <span>🏢 ${t.reporter.department.name}</span>
                            <span>📁 ${t.category?.name || 'Brak'}</span>
                            <span>📅 ${new Date(t.createdAt).toLocaleString('pl-PL')}</span>
                        </div>
                        <p style="color: #666; margin-top: 0.5rem;">${t.description || ''}</p>
                        <div class="ticket-actions">
                            ${t.status === 'OPEN' ? '<button class="btn btn-small btn-warning" onclick="changeStatus('+t.id+', \\'IN_PROGRESS\\')">▶ Rozpocznij</button>' : ''}
                            ${t.status === 'IN_PROGRESS' ? '<button class="btn btn-small btn-success" onclick="openResolveModal('+t.id+')">✓ Rozwiąż z komentarzem</button>' : ''}
                            ${t.status === 'RESOLVED' ? '<button class="btn btn-small btn-danger" onclick="changeStatus('+t.id+', \\'CLOSED\\')">✕ Zamknij</button>' : ''}
                            <button class="btn btn-small btn-primary" onclick="showDetails('+t.id+')">💬 Szczegóły</button>
                        </div>
                    </div>
                `;
            }).join('');
        }
        
        function openResolveModal(ticketId) {
            currentTicketId = ticketId;
            document.getElementById('resolve-comment').value = '';
            document.getElementById('modal-resolve').classList.add('active');
        }
        
        function closeResolveModal() {
            document.getElementById('modal-resolve').classList.remove('active');
            currentTicketId = null;
        }
        
        async function confirmResolve() {
            const comment = document.getElementById('resolve-comment').value.trim();
            if (!comment) { alert('Wpisz komentarz!'); return; }
            try {
                await fetch(API + '/comments', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        ticketId: currentTicketId,
                        authorId: user.userId,
                        content: comment
                    })
                });
                await fetch(API + '/tickets/' + currentTicketId + '/status/RESOLVED', { method: 'POST' });
                alert('Zgłoszenie rozwiązane z komentarzem!');
                closeResolveModal();
                loadTickets();
            } catch(e) { alert('Błąd: ' + e.message); }
        }
        
        async function changeStatus(id, status) {
            try {
                await fetch(API + '/tickets/' + id + '/status/' + status, { method: 'POST' });
                alert('Status zmieniony!');
                loadTickets();
            } catch(e) { alert('Błąd zmiany statusu'); }
        }
        
        async function showDetails(ticketId) {
            const ticket = allTickets.find(t => t.id === ticketId);
            if (!ticket) return;
            try {
                const cRes = await fetch(API + '/comments/ticket/' + ticketId);
                const comments = await cRes.json();
                const statusMap = {OPEN:'Otwarte',IN_PROGRESS:'W realizacji',RESOLVED:'Rozwiązane',CLOSED:'Zamknięte'};
                const prioMap = {LOW:'Niski',MEDIUM:'Średni',HIGH:'Wysoki',CRITICAL:'Krytyczny'};
                document.getElementById('modal-body').innerHTML = `
                    <h2>${ticket.title}</h2>
                    <div style="margin: 1rem 0;">
                        <span class="badge badge-${ticket.status.toLowerCase()}">${statusMap[ticket.status]}</span>
                        <span class="badge badge-${ticket.priority.toLowerCase()}">${prioMap[ticket.priority]}</span>
                    </div>
                    <div class="detail-grid">
                        <div class="detail-item"><div class="detail-label">Zgłaszający</div><div class="detail-value">${ticket.reporter.firstName} ${ticket.reporter.lastName}</div></div>
                        <div class="detail-item"><div class="detail-label">Dział</div><div class="detail-value">${ticket.reporter.department.name}</div></div>
                        <div class="detail-item"><div class="detail-label">Kategoria</div><div class="detail-value">${ticket.category?.name || 'Brak'}</div></div>
                        <div class="detail-item"><div class="detail-label">Data zgłoszenia</div><div class="detail-value">${new Date(ticket.createdAt).toLocaleString('pl-PL')}</div></div>
                    </div>
                    <div style="margin: 1.5rem 0;"><strong>Opis:</strong><p style="color: #666; margin-top: 0.5rem;">${ticket.description || 'Brak opisu'}</p></div>
                    <div style="display: flex; gap: 0.5rem; margin: 1.5rem 0;">
                        ${ticket.status === 'OPEN' ? '<button class="btn btn-warning" onclick="changeStatus('+ticket.id+', \\'IN_PROGRESS\\'); closeModal();">▶ Rozpocznij</button>' : ''}
                        ${ticket.status === 'IN_PROGRESS' ? '<button class="btn btn-success" onclick="closeModal(); openResolveModal('+ticket.id+');">✓ Rozwiąż z komentarzem</button>' : ''}
                        ${ticket.status === 'RESOLVED' ? '<button class="btn btn-danger" onclick="changeStatus('+ticket.id+', \\'CLOSED\\'); closeModal();">✕ Zamknij</button>' : ''}
                    </div>
                    <div class="comments-section">
                        <h3>Historia działań (${comments.length})</h3>
                        ${comments.length === 0 ? '<p style="color: #999;">Brak komentarzy</p>' : comments.map(c => 
                            '<div class="comment"><div class="comment-header"><span class="comment-author">'+(c.author?.firstName||'')+' '+(c.author?.lastName||'')+'</span><span class="comment-date">'+new Date(c.createdAt).toLocaleString('pl-PL')+'</span></div><div>'+c.content+'</div></div>'
                        ).join('')}
                        <div class="form-group" style="margin-top: 1.5rem;">
                            <label>Dodaj komentarz:</label>
                            <textarea id="new-comment" placeholder="Opisz wykonane działania..."></textarea>
                            <button class="btn btn-primary" onclick="addComment(${ticketId})" style="margin-top: 0.5rem;">Dodaj komentarz</button>
                        </div>
                    </div>
                `;
                document.getElementById('modal-details').classList.add('active');
            } catch(e) { alert('Błąd ładowania szczegółów'); }
        }
        
        async function addComment(ticketId) {
            const content = document.getElementById('new-comment').value.trim();
            if (!content) { alert('Wpisz treść komentarza'); return; }
            try {
                await fetch(API + '/comments', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ticketId: ticketId, authorId: user.userId, content: content })
                });
                alert('Komentarz dodany!');
                closeModal();
                loadTickets();
            } catch(e) { alert('Błąd dodawania komentarza'); }
        }
        
        function closeModal() {
            document.getElementById('modal-details').classList.remove('active');
        }
    </script>
</body>
</html>
                """;
    }
}
