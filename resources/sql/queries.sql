-- name: create-user!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, admin, is_active, pass)
VALUES (:first_name, :last_name, :email, :admin, true, :pass)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name
WHERE email = :email

-- name: get-users
-- retrieve all users
SELECT * FROM users

-- name: get-user
-- retrieve a user given the email.
SELECT * FROM users
WHERE email = :email

-- name: set-password!
-- change a user's password
UPDATE users
SET pass = :password
WHERE email = :email

-- name: get-curry
-- get a curry
SELECT * FROM curries
WHERE name = :name

-- name: get-curries
-- get a list of all available curries
SELECT * FROM curries

-- name: delete-order!
-- delete an existing curry order
DELETE FROM orders
WHERE id = :id

-- name: create-order!
-- insert a new curry order in the db
INSERT INTO orders
(user_email, curry, hotness, garlic, price, timestamp)
VALUES (:user_email, :curry, :hotness, :garlic, :price, :timestamp)

-- name: get-todays-orders
-- get today's orders
SELECT o.id, email, first_name, last_name, curry, hotness, garlic, timestamp
FROM orders o INNER JOIN users u
ON o.user_email = u.email
WHERE timestamp >= CURDATE()
ORDER BY timestamp DESC

-- name: get-users-orders
-- get all of a user's orders
SELECT o.id, email, first_name, last_name, curry, hotness, garlic, timestamp
FROM orders o INNER JOIN users u
ON o.user_email = u.email
WHERE u.email = :email
ORDER BY timestamp DESC

-- name: add-payment!
-- add a new payment
INSERT INTO payments
(user_email, amount, confirmed, timestamp)
VALUES (:user, :amount, :confirmed, :timestamp)

-- name: get-balance
-- get the current balance for a given user
SELECT
  (SELECT SUM(amount) FROM payments WHERE user_email = :email)
	- (SELECT SUM(price) FROM orders WHERE user_email = :email)
	AS balance


