-- Default cities. The larger city list is completed by DataInitializer when needed.
INSERT INTO cities (name) VALUES ('Tehran');
INSERT INTO cities (name) VALUES ('Shiraz');
INSERT INTO cities (name) VALUES ('Isfahan');
INSERT INTO cities (name) VALUES ('Mashhad');

-- Categories deliberately are not inserted here. DataInitializer is the one
-- source of category data and creates the parent/subcategory tree.

-- Default users for development.
INSERT INTO users (user_name, pass_word, full_name, role, is_blocked)
VALUES ('admin', '$2a$10$X5.BvFzLhB7mO4WfVUXzeu1sV4iXN4r9tG3A5sE3hJ1kLmNoPqRSt', 'Project Admin', 'ADMIN', false);

INSERT INTO users (user_name, pass_word, full_name, role, is_blocked)
VALUES ('user1', '$2a$10$X5.BvFzLhB7mO4WfVUXzeu1sV4iXN4r9tG3A5sE3hJ1kLmNoPqRSt', 'Reihaneh', 'USER', false);
