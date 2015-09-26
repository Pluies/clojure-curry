-- Not really contrained, but sould be one of: always, never, smart
ALTER TABLE users ADD COLUMN email_preference VARCHAR(30) DEFAULT 'smart';
