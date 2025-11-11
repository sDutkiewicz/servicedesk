# 📚 ServiceDesk API - Dokumentacja

## Przegląd

System zarządzania zgłoszeniami IT (Service Desk) - REST API w Spring Boot.

**Base URL:** `http://localhost:8080/api`

---

## 🎯 Funkcjonalności

| Kod | Opis | Endpoint |
|-----|------|----------|
| F1 | Rejestracja nowych zgłoszeń | `POST /tickets` |
| F2 | Przypisywanie zgłoszeń i zmiana statusu | `POST /tickets/{id}/assign`, `POST /tickets/{id}/status/{status}` |
| F3 | Przeglądanie i filtrowanie zgłoszeń | `GET /tickets?status=...&technicianId=...&departmentId=...` |
| F4 | Dodawanie komentarzy | `POST /comments` |
| F5 | Raport liczby zgłoszeń | `GET /reports/tickets-count?groupBy=department` |
| F6 | Raport średniego czasu rozwiązania | `GET /reports/avg-resolution-time` |
| F7 | Zarządzanie użytkownikami, technikami, działami, kategoriami | `CRUD na /users, /technicians, /departments, /categories` |

---

## 📋 Endpointy API

### 1. Zgłoszenia (Tickets)

#### 📝 Utwórz nowe zgłoszenie (F1)
```http
POST /api/tickets
Content-Type: application/json

{
  "title": "Nie działa drukarka",
  "description": "Drukarka w pokoju 204 nie drukuje",
  "reporterId": 1,
  "technicianId": 1,
  "categoryId": 1,
  "priority": "HIGH"
}
```

**Odpowiedź:**
```json
{
  "id": 1,
  "title": "Nie działa drukarka",
  "description": "Drukarka w pokoju 204 nie drukuje",
  "priority": "HIGH",
  "status": "OPEN",
  "reporter": { "id": 1, "firstName": "Jan", "lastName": "Kowalski" },
  "technician": { "id": 1, "firstName": "Tomasz", "lastName": "Biały" },
  "category": { "id": 1, "name": "Sprzęt" },
  "createdAt": "2025-11-10T10:00:00",
  "closedAt": null
}
```

#### 🔍 Pobierz wszystkie zgłoszenia (F3)
```http
GET /api/tickets
```

#### 🔍 Filtrowanie zgłoszeń (F3)
```http
GET /api/tickets?status=OPEN
GET /api/tickets?technicianId=1
GET /api/tickets?departmentId=3
GET /api/tickets?status=IN_PROGRESS&technicianId=1
```

**Statusy:** `OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`  
**Priorytety:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`

#### 📄 Pobierz szczegóły zgłoszenia
```http
GET /api/tickets/{id}
```

#### 👤 Przypisz zgłoszenie do technika (F2)
```http
POST /api/tickets/{id}/assign
Content-Type: application/json

{
  "technicianId": 2,
  "status": "IN_PROGRESS"
}
```

#### 🔄 Zmień status zgłoszenia (F2)
```http
POST /api/tickets/{id}/status/RESOLVED
```

---

### 2. Komentarze (Comments)

#### 💬 Dodaj komentarz do zgłoszenia (F4)
```http
POST /api/comments
Content-Type: application/json

{
  "ticketId": 1,
  "authorId": 2,
  "content": "Sprawdzam problem z drukarką"
}
```

#### 📋 Pobierz komentarze dla zgłoszenia
```http
GET /api/comments/ticket/{ticketId}
```

**Odpowiedź:**
```json
[
  {
    "id": 1,
    "content": "Problem pojawił się dziś rano",
    "author": { "id": 1, "firstName": "Jan", "lastName": "Kowalski" },
    "createdAt": "2025-11-10T10:15:00"
  }
]
```

---

### 3. Użytkownicy (Users) - F7

#### 📋 Lista użytkowników
```http
GET /api/users
```

#### 📄 Szczegóły użytkownika
```http
GET /api/users/{id}
```

#### ➕ Dodaj użytkownika
```http
POST /api/users
Content-Type: application/json

{
  "firstName": "Anna",
  "lastName": "Nowak",
  "email": "anna.nowak@corp.pl",
  "department": { "id": 3 }
}
```

#### ✏️ Aktualizuj użytkownika
```http
PUT /api/users/{id}
Content-Type: application/json

{
  "firstName": "Anna",
  "lastName": "Nowak-Kowalska",
  "email": "anna.nowak@corp.pl",
  "department": { "id": 2 }
}
```

#### ❌ Usuń użytkownika
```http
DELETE /api/users/{id}
```

---

### 4. Technicy (Technicians) - F7

#### 📋 Lista techników
```http
GET /api/technicians
```

#### 📄 Szczegóły technika
```http
GET /api/technicians/{id}
```

#### ➕ Dodaj technika
```http
POST /api/technicians
Content-Type: application/json

{
  "firstName": "Marek",
  "lastName": "Techniczny",
  "email": "marek.t@corp.pl",
  "department": { "id": 3 }
}
```

#### ✏️ Aktualizuj technika
```http
PUT /api/technicians/{id}
```

#### ❌ Usuń technika
```http
DELETE /api/technicians/{id}
```

---

### 5. Działy (Departments) - F7

#### 📋 Lista działów
```http
GET /api/departments
```

**Odpowiedź:**
```json
[
  { "id": 1, "name": "HR" },
  { "id": 2, "name": "Maintenance" },
  { "id": 3, "name": "IT" },
  { "id": 4, "name": "Cargo" }
]
```

#### 📄 Szczegóły działu
```http
GET /api/departments/{id}
```

#### ➕ Dodaj dział
```http
POST /api/departments
Content-Type: application/json

{
  "name": "Finance"
}
```

#### ✏️ Aktualizuj dział
```http
PUT /api/departments/{id}
Content-Type: application/json

{
  "name": "Finanse"
}
```

#### ❌ Usuń dział
```http
DELETE /api/departments/{id}
```

---

### 6. Kategorie (Categories) - F7

#### 📋 Lista kategorii
```http
GET /api/categories
```

**Odpowiedź:**
```json
[
  { "id": 1, "name": "Sprzęt" },
  { "id": 2, "name": "Oprogramowanie" },
  { "id": 3, "name": "Konto" }
]
```

#### 📄 Szczegóły kategorii
```http
GET /api/categories/{id}
```

#### ➕ Dodaj kategorię
```http
POST /api/categories
Content-Type: application/json

{
  "name": "Sieć"
}
```

#### ✏️ Aktualizuj kategorię
```http
PUT /api/categories/{id}
```

#### ❌ Usuń kategorię
```http
DELETE /api/categories/{id}
```

---

### 7. Raporty (Reports)

#### 📊 Raport liczby zgłoszeń wg działu (F5)
```http
GET /api/reports/tickets-count?groupBy=department
```

**Odpowiedź:**
```json
[
  { "group": "HR", "open": 2, "closed": 1 },
  { "group": "IT", "open": 1, "closed": 5 },
  { "group": "Maintenance", "open": 0, "closed": 2 }
]
```

#### 📊 Raport liczby zgłoszeń wg technika (F5)
```http
GET /api/reports/tickets-count?groupBy=technician
```

**Odpowiedź:**
```json
[
  { "group": "Tomasz Biały", "open": 3, "closed": 4 },
  { "group": "Katarzyna Czarna", "open": 0, "closed": 3 }
]
```

#### ⏱️ Raport średniego czasu rozwiązania (F6)
```http
GET /api/reports/avg-resolution-time
```

**Odpowiedź:**
```json
{
  "avgHours": 26.5,
  "count": 8
}
```

---

### 8. Status API

#### ✅ Sprawdź status
```http
GET /api/hello
```

**Odpowiedź:**
```json
{
  "status": "ok",
  "service": "ServiceDesk API"
}
```

---

## 🗄️ Model danych

### Ticket (Zgłoszenie)
```json
{
  "id": 1,
  "title": "string",
  "description": "string",
  "priority": "LOW | MEDIUM | HIGH | CRITICAL",
  "status": "OPEN | IN_PROGRESS | RESOLVED | CLOSED",
  "reporter": User,
  "technician": Technician,
  "category": Category,
  "createdAt": "2025-11-10T10:00:00",
  "closedAt": "2025-11-10T16:00:00"
}
```

### User (Użytkownik)
```json
{
  "id": 1,
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "department": Department
}
```

### Technician (Technik)
```json
{
  "id": 1,
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "department": Department
}
```

### Department (Dział)
```json
{
  "id": 1,
  "name": "string"
}
```

### Category (Kategoria)
```json
{
  "id": 1,
  "name": "string"
}
```

### Comment (Komentarz)
```json
{
  "id": 1,
  "ticket": Ticket,
  "author": User,
  "content": "string",
  "createdAt": "2025-11-10T10:15:00"
}
```

---

## 🧪 Testowanie API

### Postman
Import kolekcji Postman z przykładowymi requestami (dostępne w repo).

### cURL
```bash
# Pobierz wszystkie zgłoszenia
curl http://localhost:8080/api/tickets

# Utwórz nowe zgłoszenie
curl -X POST http://localhost:8080/api/tickets \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","description":"Opis","reporterId":1,"priority":"MEDIUM"}'

# Pobierz raport
curl http://localhost:8080/api/reports/tickets-count?groupBy=department
```

### Przeglądarka
- Lista działów: http://localhost:8080/api/departments
- Lista zgłoszeń: http://localhost:8080/api/tickets
- Status API: http://localhost:8080/api/hello

---

## 🔒 CORS

API pozwala na żądania z:
- `http://localhost:3000`
- `http://localhost:8080`
- file:// (local development)

---

## 🚀 Uruchomienie

1. **Uruchom aplikację:**
   ```bash
   cd api
   mvn spring-boot:run
   ```
   Lub uruchom `Main.java` w IntelliJ IDEA.

2. **API dostępne pod:**
   ```
   http://localhost:8080/api
   ```

3. **Konsola bazy danych H2:**
   ```
   http://localhost:8080/h2-console
   ```
   - JDBC URL: `jdbc:h2:mem:servicedesk`
   - User: `sa`
   - Password: (puste)

---

## 📊 Struktura projektu

```
ServiceDesk/
├── api/
│   ├── src/main/java/com/servicedesk/
│   │   ├── Main.java
│   │   ├── config/
│   │   │   └── CorsConfig.java
│   │   ├── controller/
│   │   │   ├── TicketController.java      (F1, F2, F3)
│   │   │   ├── CommentController.java     (F4)
│   │   │   ├── ReportController.java      (F5, F6)
│   │   │   ├── UserController.java        (F7)
│   │   │   ├── TechnicianController.java  (F7)
│   │   │   ├── DepartmentController.java  (F7)
│   │   │   └── CategoryController.java    (F7)
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   └── service/
│   └── src/main/resources/
│       ├── application.yml
│       └── data.sql
└── web/
    ├── index.html
    ├── app.js
    └── styles.css
```

---

## 💡 Przykłady użycia

### Scenariusz 1: Zgłoszenie problemu
1. Użytkownik tworzy zgłoszenie: `POST /api/tickets`
2. System przypisuje technikom: `POST /api/tickets/1/assign`
3. Technik dodaje komentarz: `POST /api/comments`
4. Technik zamyka zgłoszenie: `POST /api/tickets/1/status/CLOSED`

### Scenariusz 2: Przeglądanie zgłoszeń
1. Manager filtruje zgłoszenia: `GET /api/tickets?status=OPEN&departmentId=3`
2. Sprawdza szczegóły: `GET /api/tickets/5`
3. Przegląda komentarze: `GET /api/comments/ticket/5`

### Scenariusz 3: Generowanie raportów
1. Raport wg działu: `GET /api/reports/tickets-count?groupBy=department`
2. Średni czas: `GET /api/reports/avg-resolution-time`

---

## 📞 Kontakt

Projekt: System Service Desk  
Autor: Stanisław Dutkiewicz 329076  
Kurs: Metody tworzenia aplikacji bazodanowych  
Data: Listopad 2025

