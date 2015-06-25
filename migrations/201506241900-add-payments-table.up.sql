CREATE TABLE payments
(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_email VARCHAR(512),
	amount FLOAT,
	confirmed BOOLEAN,
	timestamp TIME
);
