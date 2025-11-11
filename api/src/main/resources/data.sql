-- Działy
insert into departments (id, name) values
(1,'HR'), (2,'Maintenance'), (3,'IT'), (4,'Cargo');

-- Użytkownicy (zgłaszający problemy)
insert into users (id, first_name, last_name, email, department_id) values
(1,'Jan','Kowalski','jan.k@corp.pl',1),
(2,'Anna','Nowak','anna.n@corp.pl',3),
(3,'Piotr','Zieliński','piotr.z@corp.pl',2);

-- Technicy IT (obsługujący zgłoszenia)
insert into technicians (id, first_name, last_name, email, department_id) values
(1,'Tomasz','Biały','tomasz.b@corp.pl',3),
(2,'Katarzyna','Czarna','katarzyna.c@corp.pl',3);

-- Konta logowania (powiązane z users i technicians)
-- admin nie jest powiązany z nikim (superuser) - user_id i technician_id = NULL
-- jan.kowalski powiązany z user id 1
-- anna.nowak powiązana z user id 2
-- piotr.zielinski powiązany z user id 3
-- tomasz.bialy powiązany z technician id 1
-- katarzyna.czarna powiązana z technician id 2
insert into accounts (id, login, password, role, user_id, technician_id) values
(1, 'admin', 'admin123', 'admin', NULL, NULL),
(2, 'jan.kowalski', 'pass123', 'user', 1, NULL),
(3, 'anna.nowak', 'pass123', 'user', 2, NULL),
(4, 'piotr.zielinski', 'pass123', 'user', 3, NULL),
(5, 'tomasz.bialy', 'tech123', 'technician', NULL, 1),
(6, 'katarzyna.czarna', 'tech123', 'technician', NULL, 2);

insert into categories (id, name) values
(1,'Sprzęt'), (2,'Oprogramowanie'), (3,'Konto');

insert into tickets
  (id, title, description, priority, status, reporter_id, technician_id, category_id, created_at, closed_at)
values
  (1,'Nie działa drukarka','Pokój 204','HIGH','OPEN',1,1,1, TIMESTAMP '2025-11-10 10:00:00', NULL),
  (2,'VPN rozłącza','Po aktualizacji','MEDIUM','IN_PROGRESS',2,1,2, TIMESTAMP '2025-11-10 09:30:00', NULL),
  (3,'Reset hasła','Brak dostępu do maila','LOW','CLOSED',3,2,3, TIMESTAMP '2025-11-09 14:00:00', TIMESTAMP '2025-11-09 16:30:00');

insert into comments (ticket_id, author_id, content, created_at) values
(1,1,'Problem pojawił się dziś rano', TIMESTAMP '2025-11-10 10:15:00'),
(1,2,'Sprawdzę sterowniki',          TIMESTAMP '2025-11-10 10:30:00'),
(3,3,'Proszę o weryfikację 2FA',     TIMESTAMP '2025-11-09 14:30:00');
