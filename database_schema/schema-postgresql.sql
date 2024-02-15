CREATE TABLE auth_types
(
    id          serial NOT NULL,
    name        varchar(100),
    description text,

    CONSTRAINT auth_types_pk PRIMARY KEY (id)
);

/*------------------------------------------------------------------------------------------------------------------*/

CREATE SEQUENCE users_id_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CYCLE;

CREATE TABLE users
(
    id           bigint    DEFAULT nextval('users_id_seq') NOT NULL,
    auth_id      bigint                                    NOT NULL,
    auth_type_id int                                       NOT NULL,
    name         varchar(100),
    photo_path   text,
    photo_type   varchar(50),
    created_at   timestamp DEFAULT now()                   NOT NULL,
    deleted_at   timestamp,

    CONSTRAINT users_pk PRIMARY KEY (id),
    CONSTRAINT users_auth_type_id_fk FOREIGN KEY (auth_type_id) REFERENCES auth_types (id)
);

CREATE INDEX users_ix_auth_id ON users (auth_id);

/*------------------------------------------------------------------------------------------------------------------*/

CREATE SEQUENCE chat_id_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CYCLE;

CREATE TABLE chats
(
    id               bigint DEFAULT nextval('chat_id_seq') NOT NULL,
    producer_user_id bigint                                NOT NULL,
    consumer_user_id bigint                                NOT NULL,
    deleted_at       timestamp,


    CONSTRAINT chats_pk PRIMARY KEY (id),
    CONSTRAINT chats_producer_id_fk FOREIGN KEY (producer_user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chats_consumer_id_fk FOREIGN KEY (consumer_user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX chats_ix1_producer_user_id ON chats (producer_user_id);
CREATE INDEX chats_ix2_consumer_user_id ON chats (consumer_user_id);

/*------------------------------------------------------------------------------------------------------------------*/

CREATE SEQUENCE personal_messages_id_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CYCLE;

CREATE TABLE personal_messages
(
    id                 bigint DEFAULT nextval('personal_messages_id_seq') NOT NULL,
    chat_id            bigserial                                          NOT NULL,
    sender_is_producer boolean                                            NOT NULL,
    message            text                                               NOT NULL,
    send_time          timestamp                                          NOT NULL,
    deleted_at         timestamp,

    CONSTRAINT personal_messages_pk PRIMARY KEY (id),
    CONSTRAINT personal_messages_chat_fk FOREIGN KEY (chat_id) REFERENCES chats (id) ON DELETE CASCADE
);

CREATE INDEX personal_messages_ix_chat_id ON personal_messages (chat_id);

/*------------------------------------------------------------------------------------------------------------------*/

CREATE SEQUENCE public_messages_id_seq
    START 1
    INCREMENT 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CYCLE;

CREATE TABLE public_messages
(
    id               bigint DEFAULT nextval('public_messages_id_seq') NOT NULL,
    producer_user_id bigint                                           NOT NULL,
    message          text                                             NOT NULL,
    send_time        timestamp                                        NOT NULL,
    deleted_at       timestamp,

    CONSTRAINT public_messages_pk PRIMARY KEY (id),
    CONSTRAINT public_messages_producer_id_fk FOREIGN KEY (producer_user_id) REFERENCES users (id) ON DELETE CASCADE
);

/*------------------------------------------------------------------------------------------------------------------*/

CREATE OR REPLACE FUNCTION users_update_deleted_at_function()
    RETURNS TRIGGER AS
$$
BEGIN
    IF TG_OP = 'UPDATE' AND NEW.deleted_at IS NOT DISTINCT FROM OLD.deleted_at THEN
        RETURN NEW;
    END IF;

    UPDATE chats
    SET deleted_at = NEW.deleted_at
    WHERE producer_user_id = NEW.id
       OR consumer_user_id = NEW.id;

    UPDATE personal_messages
    SET deleted_at = NEW.deleted_at
    FROM chats
    WHERE personal_messages.chat_id = chats.id
      AND (chats.producer_user_id = NEW.id OR chats.consumer_user_id = NEW.id);

    UPDATE public_messages
    SET deleted_at = NEW.deleted_at
    WHERE producer_user_id = NEW.id;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER users_update_deleted_at_trigger
    AFTER UPDATE OF deleted_at
    ON users
    FOR EACH ROW
EXECUTE FUNCTION users_update_deleted_at_function();