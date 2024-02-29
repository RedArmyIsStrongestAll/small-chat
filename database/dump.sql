--
-- PostgreSQL database dump
--

CREATE DATABASE small_chat;
SET search_path TO small_chat, public;

-- Dumped from database version 16.1
-- Dumped by pg_dump version 16.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: users_update_deleted_at_function(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.users_update_deleted_at_function() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

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

$$;


ALTER FUNCTION public.users_update_deleted_at_function() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: auth_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.auth_types (
    id integer NOT NULL,
    name character varying(100),
    description text
);


ALTER TABLE public.auth_types OWNER TO postgres;

--
-- Name: auth_types_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.auth_types_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.auth_types_id_seq OWNER TO postgres;

--
-- Name: auth_types_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.auth_types_id_seq OWNED BY public.auth_types.id;


--
-- Name: chat_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER SEQUENCE public.chat_id_seq OWNER TO postgres;

--
-- Name: chats; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chats (
    id bigint DEFAULT nextval('public.chat_id_seq'::regclass) NOT NULL,
    producer_user_id bigint NOT NULL,
    consumer_user_id bigint NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.chats OWNER TO postgres;

--
-- Name: personal_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.personal_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER SEQUENCE public.personal_messages_id_seq OWNER TO postgres;

--
-- Name: personal_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.personal_messages (
    id bigint DEFAULT nextval('public.personal_messages_id_seq'::regclass) NOT NULL,
    chat_id bigint NOT NULL,
    sender_is_producer boolean NOT NULL,
    message text NOT NULL,
    send_time timestamp without time zone NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.personal_messages OWNER TO postgres;

--
-- Name: personal_messages_chat_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.personal_messages_chat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.personal_messages_chat_id_seq OWNER TO postgres;

--
-- Name: personal_messages_chat_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.personal_messages_chat_id_seq OWNED BY public.personal_messages.chat_id;


--
-- Name: public_messages_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.public_messages_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER SEQUENCE public.public_messages_id_seq OWNER TO postgres;

--
-- Name: public_messages; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.public_messages (
    id bigint DEFAULT nextval('public.public_messages_id_seq'::regclass) NOT NULL,
    producer_user_id bigint NOT NULL,
    message text NOT NULL,
    send_time timestamp without time zone NOT NULL,
    deleted_at timestamp without time zone
);


ALTER TABLE public.public_messages OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
    CYCLE;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    id bigint DEFAULT nextval('public.users_id_seq'::regclass) NOT NULL,
    auth_id bigint NOT NULL,
    auth_type_id integer NOT NULL,
    name character varying(100),
    photo_path text,
    photo_type character varying(50),
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    deleted_at timestamp without time zone,
    last_login_at timestamp without time zone DEFAULT now()
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: auth_types id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auth_types ALTER COLUMN id SET DEFAULT nextval('public.auth_types_id_seq'::regclass);


--
-- Name: personal_messages chat_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.personal_messages ALTER COLUMN chat_id SET DEFAULT nextval('public.personal_messages_chat_id_seq'::regclass);


--
-- Data for Name: auth_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.auth_types (id, name, description) FROM stdin;
1	vk id	╨а╨╡╨│╨╕╤Б╤В╤А╨░╨╜╨╕╤Ж╨╕╤П ╤З╨╡╤А╨╡╨╖ ╨▓╨║, ╨╛╨┤╨╜╨╛╨║╨╗╨░╤Б╤Б╨╜╨╕╨║╨╕ ╨╕╨╗╨╕ mail.ru.╨Я╤А╨╡╨┤╨╛╤Б╤В╨░╨╗╨▓╤П╨╡╤В╤Б╤П ╨Ю╨Ю╨Ю "╨Т ╨Ъ╨Ю╨Э╨в╨Р╨Ъ╨в╨Х" ╨Ю╨У╨а╨Э 1079847035179
\.


--
-- Data for Name: chats; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chats (id, producer_user_id, consumer_user_id, deleted_at) FROM stdin;
\.


--
-- Data for Name: personal_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.personal_messages (id, chat_id, sender_is_producer, message, send_time, deleted_at) FROM stdin;
\.


--
-- Data for Name: public_messages; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.public_messages (id, producer_user_id, message, send_time, deleted_at) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (id, auth_id, auth_type_id, name, photo_path, photo_type, created_at, deleted_at, last_login_at) FROM stdin;
\.


--
-- Name: auth_types_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.auth_types_id_seq', 1, true);


--
-- Name: chat_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.chat_id_seq', 1, false);


--
-- Name: personal_messages_chat_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.personal_messages_chat_id_seq', 1, false);


--
-- Name: personal_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.personal_messages_id_seq', 1, false);


--
-- Name: public_messages_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.public_messages_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_id_seq', 1, false);


--
-- Name: auth_types auth_types_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.auth_types
    ADD CONSTRAINT auth_types_pk PRIMARY KEY (id);


--
-- Name: chats chats_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_pk PRIMARY KEY (id);


--
-- Name: personal_messages personal_messages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.personal_messages
    ADD CONSTRAINT personal_messages_pk PRIMARY KEY (id);


--
-- Name: public_messages public_messages_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.public_messages
    ADD CONSTRAINT public_messages_pk PRIMARY KEY (id);


--
-- Name: users users_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pk PRIMARY KEY (id);


--
-- Name: chats_ix1_producer_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX chats_ix1_producer_user_id ON public.chats USING btree (producer_user_id);


--
-- Name: chats_ix2_consumer_user_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX chats_ix2_consumer_user_id ON public.chats USING btree (consumer_user_id);


--
-- Name: personal_messages_ix_chat_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX personal_messages_ix_chat_id ON public.personal_messages USING btree (chat_id);


--
-- Name: users_ix_auth_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX users_ix_auth_id ON public.users USING btree (auth_id);


--
-- Name: users users_update_deleted_at_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER users_update_deleted_at_trigger AFTER UPDATE OF deleted_at ON public.users FOR EACH ROW EXECUTE FUNCTION public.users_update_deleted_at_function();


--
-- Name: chats chats_consumer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_consumer_id_fk FOREIGN KEY (consumer_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: chats chats_producer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chats
    ADD CONSTRAINT chats_producer_id_fk FOREIGN KEY (producer_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: personal_messages personal_messages_chat_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.personal_messages
    ADD CONSTRAINT personal_messages_chat_fk FOREIGN KEY (chat_id) REFERENCES public.chats(id) ON DELETE CASCADE;


--
-- Name: public_messages public_messages_producer_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.public_messages
    ADD CONSTRAINT public_messages_producer_id_fk FOREIGN KEY (producer_user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- Name: users users_auth_type_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_auth_type_id_fk FOREIGN KEY (auth_type_id) REFERENCES public.auth_types(id);


--
-- PostgreSQL database dump complete
--

