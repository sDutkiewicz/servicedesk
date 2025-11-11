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
        // Zwracam HTML bezpośrednio zamiast czytać z pliku
        return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Panel Technika</title>
    <style>
        body { font-family: Arial; margin: 0; background: #f5f7fa; }
        .navbar { background: linear-gradient(135deg, #667eea, #764ba2); color: white; padding: 1rem 2rem; }
        .container { max-width: 1400px; margin: 2rem auto; padding: 0 2rem; }
        .btn { padding: 0.6rem 1.2rem; border: none; border-radius: 5px; cursor: pointer; font-weight: 600; }
        .btn-primary { background: #667eea; color: white; }
    </style>
</head>
<body>
    <div class="navbar">
        <h1>🔧 Panel Technika IT</h1>
        <p>Zalogowany jako: <span id="tech-name"></span> | <a href="/" style="color: white;">Wyloguj</a></p>
    </div>
    <div class="container">
        <h2>Zgłoszenia przypisane do mnie</h2>
        <button class="btn btn-primary" onclick="location.reload()">Odśwież</button>
        <div id="tickets" style="margin-top: 2rem;"></div>
    </div>
    <script>
        const API = 'http://localhost:8080/api';
        let user = null;
        
        window.onload = () => {
            const u = sessionStorage.getItem('user');
            if (!u) { window.location.href = '/'; return; }
            user = JSON.parse(u);
            if (user.role !== 'technician') { alert('Brak dostępu!'); window.location.href = '/'; return; }
            document.getElementById('tech-name').textContent = user.login;
            loadTickets();
        };
        
        async function loadTickets() {
            try {
                const res = await fetch(`${API}/tickets/assigned?technicianId=${user.technicianId}`);
                const tickets = await res.json();
                const div = document.getElementById('tickets');
                if (tickets.length === 0) { div.innerHTML = '<p>Brak zgłoszeń</p>'; return; }
                div.innerHTML = tickets.map(t => `
                    <div style="background: white; padding: 1.5rem; margin-bottom: 1rem; border-radius: 8px; border-left: 4px solid #667eea;">
                        <h3>${t.title}</h3>
                        <p><strong>Status:</strong> ${t.status} | <strong>Priorytet:</strong> ${t.priority}</p>
                        <p><strong>Zgłaszający:</strong> ${t.reporter.firstName} ${t.reporter.lastName}</p>
                        <p>${t.description || ''}</p>
                        <button class="btn btn-primary" onclick="changeStatus(${t.id}, 'IN_PROGRESS')">Rozpocznij</button>
                        <button class="btn btn-primary" onclick="changeStatus(${t.id}, 'RESOLVED')" style="background: #4caf50;">Rozwiązane</button>
                    </div>
                `).join('');
            } catch(e) { document.getElementById('tickets').innerHTML = '<p>Błąd ładowania</p>'; }
        }
        
        async function changeStatus(id, status) {
            try {
                await fetch(`${API}/tickets/${id}/status/${status}`, { method: 'POST' });
                alert('Status zmieniony!');
                loadTickets();
            } catch(e) { alert('Błąd'); }
        }
    </script>
</body>
</html>
                """;
    }
}
