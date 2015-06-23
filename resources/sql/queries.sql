-- name: create-user!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, pass)
VALUES (:first_name, :last_name, :email, :pass)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name
WHERE email = :email

-- name: get-user
-- retrieve a user given the email.
SELECT * FROM users
WHERE email = :email

-- name: set-password!
-- change a user's password
UPDATE users
SET pass = :password
WHERE email = :email

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
(user_email, curry, hotness, garlic, timestamp)
VALUES (:user_email, :curry, :hotness, :garlic, :timestamp)

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

