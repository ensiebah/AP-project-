-- ۱. تزریق شهرهای پیش‌فرض
INSERT INTO cities (name) VALUES ('Tehran');
INSERT INTO cities (name) VALUES ('Shiraz');
INSERT INTO cities (name) VALUES ('Isfahan');
INSERT INTO cities (name) VALUES ('Mashhad');

-- ۲. تزریق دسته‌بندی‌های پیش‌فرض
INSERT INTO categories (name) VALUES ('Digital Electronics');
INSERT INTO categories (name) VALUES ('Home Appliances');
INSERT INTO categories (name) VALUES ('Vehicles');
INSERT INTO categories (name) VALUES ('Books & Hobbies');

-- ۳. تزریق کاربران پیش‌فرض برای تست (پسورد رمزنگاری‌شده با BCrypt عبارت «password» است)
-- کاربر اول: ادمین سیستم
INSERT INTO users (user_name, pass_word, full_name, role, is_blocked)
VALUES ('admin', '$2a$10$X5.BvFzLhB7mO4WfVUXzeu1sV4iXN4r9tG3A5sE3hJ1kLmNoPqRSt', 'Project Admin', 'ADMIN', false);

-- کاربر دوم: کاربر عادی (خریدار یا فروشنده فرضی)
INSERT INTO users (user_name, pass_word, full_name, role, is_blocked)
VALUES ('user1', '$2a$10$X5.BvFzLhB7mO4WfVUXzeu1sV4iXN4r9tG3A5sE3hJ1kLmNoPqRSt', 'Reihaneh', 'USER', false);