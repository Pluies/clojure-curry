CREATE TABLE orders
(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_email VARCHAR(255),
	curry VARCHAR(255),
	hotness VARCHAR(10),
	garlic BOOLEAN,
	timestamp TIME
);
