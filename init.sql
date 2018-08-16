CREATE TABLE servers ( server_id BIGINT PRIMARY KEY, prefix CHAR(16), log_channel BIGINT, star_channel BIGINT );

CREATE TABLE servers_roles( server_id BIGINT, role_spec VARCHAR(255), role_id BIGINT, UNIQUE(server_id, role_spec));
CREATE INDEX servers_roles_id ON servers_roles(server_id);

CREATE TABLE servers_self_roles(server_id BIGINT, role_spec VARCHAR(255), role_id BIGINT, UNIQUE(server_id, role_id));
CREATE INDEX servers_self_roles_id ON servers_self_roles(server_id);

CREATE TABLE custom_commands( server_id BIGINT, command VARCHAR(255), response VARCHAR(3000), UNIQUE(server_id, command));
CREATE INDEX custom_commands_id ON custom_commands(server_id, command);

CREATE TABLE servers_logs( server_id BIGINT, event_name VARCHAR(255), channel_id BIGINT );
CREATE INDEX servers_logs_id_name ON servers_logs(server_id, event_name);