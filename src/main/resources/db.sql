-- All Discord IDs are 64bit unsigned integers, unfortunately SQLite doesn't properly support them.
-- So we use TEXT instead. BLOB would work here too, but not needed

CREATE TABLE servers (
  server_id    TEXT PRIMARY KEY,
  prefix       TEXT,
  log_channel  TEXT,
  star_channel TEXT
);

CREATE TABLE servers_roles (
  server_id TEXT,
  role_spec TEXT,
  role_id   TEXT,
  UNIQUE (server_id, role_spec)
    ON CONFLICT REPLACE
);

CREATE INDEX servers_roles_id
  ON servers_roles (server_id);

CREATE TABLE servers_self_roles (
  server_id TEXT,
  role_spec TEXT,
  role_id   TEXT,
  UNIQUE (server_id, role_id)
    ON CONFLICT REPLACE
);

CREATE INDEX servers_self_roles_id
  ON servers_self_roles (server_id);

CREATE TABLE custom_commands (
  server_id TEXT,
  command   TEXT,
  response  TEXT,
  UNIQUE (server_id, command)
    ON CONFLICT REPLACE
);

CREATE INDEX custom_commands_id
  ON custom_commands (server_id);

CREATE TABLE servers_logs (
  server_id  TEXT,
  event_name TEXT,
  channel_id TEXT
);

CREATE INDEX servers_logs_id_name
  ON servers_logs (server_id, event_name);