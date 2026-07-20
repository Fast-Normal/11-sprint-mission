CREATE TABLE IF NOT EXISTS binary_contents
(
    id           UUID PRIMARY KEY,
    created_at   timestamp with time zone NOT NULL,
    updated_at   timestamp with time zone,
    status       VARCHAR(20)              NOT NULL DEFAULT 'PROCESSING',
    file_name    VARCHAR(255)             NOT NULL,
    size         BIGINT                   NOT NULL,
    content_type VARCHAR(100)             NOT NULL,
    bytes        BYTEA                    NOT NULL
);

CREATE TABLE iF NOT EXISTS users
(
    id         UUID PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    username   VARCHAR(50)              NOT NULL UNIQUE,
    email      VARCHAR(100)             NOT NULL UNIQUE,
    password   VARCHAR(60)              NOT NULL,
    profile_id UUID,
    role       VARCHAR(20)              NOT NULL,
    FOREIGN KEY (profile_id) REFERENCES binary_contents (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS channels
(
    id          UUID PRIMARY KEY,
    created_at  timestamp with time zone NOT NULL,
    updated_at  timestamp with time zone,
    name        VARCHAR(100),
    description VARCHAR(500),
    type        VARCHAR(10)              NOT NULL
        CONSTRAINT chk_channel_type CHECK ( type IN ('PUBLIC', 'PRIVATE') )
);

CREATE TABLE IF NOT EXISTS messages
(
    id         UUID PRIMARY KEY,
    created_at timestamp with time zone NOT NULL,
    updated_at timestamp with time zone,
    content    TEXT,
    channel_id UUID                     NOT NULL,
    author_id  UUID,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS message_attachments
(
    message_id    UUID NOT NULL,
    attachment_id UUID NOT NULL,
    PRIMARY KEY (message_id, attachment_id),
    FOREIGN KEY (message_id) REFERENCES messages (id) ON DELETE CASCADE,
    FOREIGN KEY (attachment_id) REFERENCES binary_contents (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS read_statuses
(
    id                   UUID PRIMARY KEY,
    created_at           timestamp with time zone NOT NULL,
    updated_at           timestamp with time zone,
    user_id              UUID                     NOT NULL,
    channel_id           UUID                     NOT NULL,
    last_read_at         timestamp with time zone NOT NULL,
    notification_enabled boolean                  NOT NULL DEFAULT false,
    UNIQUE (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES channels (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications
(
    id          UUID PRIMARY KEY,
    created_at  timestamp with time zone NOT NULL,
    receiver_id UUID                     NOT NULL REFERENCES users (id),
    title       VARCHAR(255)             NOT NULL,
    content     text                     NOT NULL
);

CREATE TABLE refresh_tokens
(
    user_id    UUID PRIMARY KEY,
    token      VARCHAR(1000) NOT NULL,
    expires_at TIMESTAMP     NOT NULL
);
