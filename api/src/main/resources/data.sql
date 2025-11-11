-- Działy
insert into departments (id, name) values
(1,'HR'), (2,'Maintenance'), (3,'IT'), (4,'Cargo');

-- Użytkownicy (zgłaszający problemy + technicy też jako users żeby mogli dodawać komentarze)
insert into users (id, first_name, last_name, email, department_id) values
(1,'Jan','Kowalski','jan.k@corp.pl',1),
(2,'Anna','Nowak','anna.n@corp.pl',3),
(3,'Piotr','Zieliński','piotr.z@corp.pl',2),
(4,'Tomasz','Biały','tomasz.b@corp.pl',3),
(5,'Katarzyna','Czarna','katarzyna.c@corp.pl',3);

-- Technicy IT (obsługujący zgłoszenia)
insert into technicians (id, first_name, last_name, email, department_id) values
(1,'Tomasz','Biały','tomasz.b@corp.pl',3),
(2,'Katarzyna','Czarna','katarzyna.c@corp.pl',3);

-- Konta logowania (powiązane z users i technicians)
-- admin nie jest powiązany z nikim (superuser)
-- jan.kowalski, anna.nowak, piotr.zielinski - zwykli użytkownicy
-- tomasz.bialy - technik (user_id=4 żeby mógł dodawać komentarze, technician_id=1 żeby mógł obsługiwać zgłoszenia)
-- katarzyna.czarna - technik (user_id=5, technician_id=2)
insert into accounts (id, login, password, role, user_id, technician_id) values
(1, 'admin', 'admin123', 'admin', NULL, NULL),
(2, 'jan.kowalski', 'pass123', 'user', 1, NULL),
(3, 'anna.nowak', 'pass123', 'user', 2, NULL),
(4, 'piotr.zielinski', 'pass123', 'user', 3, NULL),
(5, 'tomasz.bialy', 'tech123', 'technician', 4, 1),
(6, 'katarzyna.czarna', 'tech123', 'technician', 5, 2);

insert into categories (id, name) values
(1,'Sprzęt'), (2,'Oprogramowanie'), (3,'Konto');

-- Testowe zgłoszenia używają ID 100+ żeby nie kolidować z nowymi zgłoszeniami użytkowników (ID 1, 2, 3...)
insert into tickets
  (id, title, description, priority, status, reporter_id, technician_id, category_id, created_at, closed_at)
values
  (100,'Nie działa drukarka','Pokój 204','HIGH','OPEN',1,1,1, TIMESTAMP '2025-11-10 10:00:00', NULL),
  (101,'VPN rozłącza','Po aktualizacji','MEDIUM','IN_PROGRESS',2,1,2, TIMESTAMP '2025-11-10 09:30:00', NULL),
  (102,'Reset hasła','Brak dostępu do maila','LOW','CLOSED',3,2,3, TIMESTAMP '2025-11-09 14:00:00', TIMESTAMP '2025-11-09 16:30:00');

insert into comments (ticket_id, author_id, content, created_at) values
(100,1,'Problem pojawił się dziś rano', TIMESTAMP '2025-11-10 10:15:00'),
(100,4,'Sprawdzę sterowniki',          TIMESTAMP '2025-11-10 10:30:00'),
(102,3,'Proszę o weryfikację 2FA',     TIMESTAMP '2025-11-09 14:30:00');
