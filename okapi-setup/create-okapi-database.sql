DROP DATABASE okapi;
DROP USER okapi;

CREATE USER okapi PASSWORD 'okapi25' NOSUPERUSER NOCREATEDB INHERIT LOGIN;
CREATE DATABASE okapi OWNER okapi;
