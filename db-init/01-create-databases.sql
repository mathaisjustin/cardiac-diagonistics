-- Runs once, on first container start, against a fresh MySQL data volume.
-- One database + one scoped user per service, matching docs/ARCHITECTURE.md's
-- "no service reaches into another's database" rule.

CREATE DATABASE IF NOT EXISTS authentication_db;
CREATE DATABASE IF NOT EXISTS userprofile_db;
CREATE DATABASE IF NOT EXISTS bookmark_db;

CREATE USER IF NOT EXISTS 'authentication_user'@'%' IDENTIFIED BY 'authentication_pass';
GRANT ALL PRIVILEGES ON authentication_db.* TO 'authentication_user'@'%';

CREATE USER IF NOT EXISTS 'userprofile_user'@'%' IDENTIFIED BY 'userprofile_pass';
GRANT ALL PRIVILEGES ON userprofile_db.* TO 'userprofile_user'@'%';

CREATE USER IF NOT EXISTS 'bookmark_user'@'%' IDENTIFIED BY 'bookmark_pass';
GRANT ALL PRIVILEGES ON bookmark_db.* TO 'bookmark_user'@'%';

FLUSH PRIVILEGES;
