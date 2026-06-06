-- Seed default users
-- Admin User: admin / secret
INSERT INTO users (username, email, full_name, password, role, created_at, version)
VALUES ('admin', 'admin@issueflow.com', 'System Admin', '$2a$10$X5p1A4A/yqWbS6yL5Cj3P.GvqjRzV4.yD.kK034bZ0Q/t2/Y6dDk.', 'ADMIN', CURRENT_TIMESTAMP, 0)
ON CONFLICT (username) DO NOTHING;

-- Developer User: jdoe / secret
INSERT INTO users (username, email, full_name, password, role, created_at, version)
VALUES ('jdoe', 'jdoe@issueflow.com', 'John Doe', '$2a$10$X5p1A4A/yqWbS6yL5Cj3P.GvqjRzV4.yD.kK034bZ0Q/t2/Y6dDk.', 'DEVELOPER', CURRENT_TIMESTAMP, 0)
ON CONFLICT (username) DO NOTHING;