DROP TABLE IF EXISTS ano_output;
DROP TABLE IF EXISTS ano_note;
DROP TABLE IF EXISTS ano_task;

CREATE TABLE ano_task (
    id BIGINT NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description CLOB,
    state INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    parent_id BIGINT,
    iteration_note VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    archived_time TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_user_state ON ano_task (user_id, state);
CREATE INDEX idx_parent_id ON ano_task (parent_id);

CREATE TABLE ano_note (
    id BIGINT NOT NULL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    content CLOB,
    state INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_task_id ON ano_note (task_id);

CREATE TABLE ano_output (
    id BIGINT NOT NULL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    url VARCHAR(1024) NOT NULL,
    platform VARCHAR(50),
    state INT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_id ON ano_output (task_id);
