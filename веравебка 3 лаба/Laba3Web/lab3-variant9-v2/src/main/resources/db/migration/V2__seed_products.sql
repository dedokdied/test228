-- Тестовый набор данных: каталог магазина электроники.
-- Скрипт выполняется Flyway один раз при создании/обновлении БД.

INSERT INTO products (name, brand, price, count_in_stock) VALUES
    ('Монитор Dell UltraSharp U2723QE', 'Dell', 15999.99, 5),
    ('Ноутбук Apple MacBook Air 13 M3', 'Apple', 112990.00, 3),
    ('Смартфон Samsung Galaxy S24', 'Samsung', 79990.00, 8),
    ('Наушники Sony WH-1000XM5', 'Sony', 29990.00, 12),
    ('Клавиатура Logitech MX Keys', 'Logitech', 8990.00, 20),
    ('Мышь Logitech MX Master 3S', 'Logitech', 7490.00, 15),
    ('SSD Samsung 990 Pro 1TB', 'Samsung', 12490.00, 25),
    ('Видеокарта NVIDIA GeForce RTX 4070', 'NVIDIA', 64990.00, 4),
    ('Процессор AMD Ryzen 7 7800X3D', 'AMD', 34990.00, 6),
    ('Материнская плата ASUS ROG Strix B650E-F', 'ASUS', 23990.00, 7),
    ('Блок питания be quiet! Straight Power 850W', 'be quiet!', 11990.00, 10),
    ('Корпус Fractal Design North', 'Fractal Design', 13990.00, 0);
