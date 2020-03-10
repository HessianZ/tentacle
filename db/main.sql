DROP TABLE IF EXISTS public.auth_users;

CREATE TABLE public.auth_users
(
    id serial NOT NULL,
    username varchar(32) NOT NULL,
    email varchar(64) NOT NULL,
    status smallint NOT NULL DEFAULT 1,
    mi_token varchar(64) NOT NULL,
    password varchar(64) NOT NULL,
    created_at timestamp without time zone NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS public.actions;

CREATE TABLE public.actions
(
    id serial NOT NULL,
    user_id integer NOT NULL,
    name varchar(30) NOT NULL,
    memo varchar(200),
    type smallint,
    status smallint,
    command jsonb,
    created_at timestamp without time zone NOT NULL,
    modified_at timestamp without time zone NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user_id FOREIGN KEY (user_id)  REFERENCES public.auth_users (id)
);

COMMENT ON COLUMN actions.type IS '1=http, 2=ssh';


INSERT INTO public.auth_users (id, created_at, modified_at, password, mi_token, status, email, username)
VALUES ('1'::integer, '2020-03-09 23:44:02'::timestamp without time zone,
        '2020-03-09 23:44:02'::timestamp without time zone, '123123'::character varying(64),
        '123123'::character varying(64), '1'::smallint, 'mail@hessian.cn'::character varying(64),
        'mail@hessian.cn'::character varying(32)) returning id;
