INSERT INTO users (username, password_hash, role) VALUES
('admin_demo', '240be518fabd2724ddb6f04eeb88493f6f0b2f7f179e8a6d1f2f8d59b3f0f8d2', 'ADMIN'),
('student_demo', '4387d14ea5713c2e681fc1f9f2f5a6f95eb4d92ab92f6f4b8b3eb4c5f3ab4c1c', 'STANDARD');

INSERT INTO credentials (user_id, site_name, site_username, encrypted_password, notes) VALUES
(2, 'Gmail', 'student@gmail.com', '2L6XtKzXgJ4aVRN0nEzMvS/itVUk9UA80ERwS2TXd5w=', 'Academic email account'),
(2, 'GitHub', 'student-demo', '0utKowdQdPfG4z7vKBg/WdE8vN8L0Gx6hv5b8uw2Y5g=', 'Source code repository account');
