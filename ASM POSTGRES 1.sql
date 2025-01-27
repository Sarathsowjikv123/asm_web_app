--
-- PostgreSQL database dump
--

-- Dumped from database version 17.2
-- Dumped by pg_dump version 17.2

-- Started on 2025-01-08 21:46:33 IST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'LATIN8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 868 (class 1247 OID 16591)
-- Name: asset_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.asset_type AS ENUM (
    'HARDWARE',
    'SOFTWARE'
);


ALTER TYPE public.asset_type OWNER TO postgres;

--
-- TOC entry 874 (class 1247 OID 16604)
-- Name: operation_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.operation_type AS ENUM (
    'ASSIGN',
    'RETAIN'
);


ALTER TYPE public.operation_type OWNER TO postgres;

--
-- TOC entry 871 (class 1247 OID 16596)
-- Name: user_type; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE public.user_type AS ENUM (
    'MANAGER',
    'EMPLOYEE',
    'TRAINEE'
);


ALTER TYPE public.user_type OWNER TO postgres;

--
-- TOC entry 245 (class 1255 OID 16863)
-- Name: add_new_asset(character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.add_new_asset(IN asset_name character varying, IN asset_type character varying, IN asset_count integer, OUT new_asset_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
    INSERT INTO assets (asset_name, asset_type) VALUES (asset_name, asset_type::asset_type) 
    RETURNING asset_id INTO new_asset_id;

    INSERT INTO asset_count (asset_id, asset_count) VALUES (new_asset_id, asset_count);

    INSERT INTO retained_assets (asset_id, retained_asset_count) VALUES (new_asset_id, 0);
END;
$$;


ALTER PROCEDURE public.add_new_asset(IN asset_name character varying, IN asset_type character varying, IN asset_count integer, OUT new_asset_id integer) OWNER TO postgres;

--
-- TOC entry 243 (class 1255 OID 16813)
-- Name: add_new_user(character varying, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.add_new_user(IN user_name character varying, IN user_type_id integer, OUT new_user_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
	INSERT INTO users (user_name, user_type_id) VALUES (user_name, user_type_id)
	RETURNING user_id INTO new_user_id;
END;
$$;


ALTER PROCEDURE public.add_new_user(IN user_name character varying, IN user_type_id integer, OUT new_user_id integer) OWNER TO postgres;

--
-- TOC entry 244 (class 1255 OID 16828)
-- Name: allocate_asset(integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.allocate_asset(IN a_user_id integer, IN a_asset_id integer)
    LANGUAGE plpgsql
    AS $$
DECLARE 
	req_assignment_id INT;
BEGIN

	--Check for availability in the inventory
	IF EXISTS (SELECT 1 FROM assets WHERE asset_id = a_asset_id AND
		(SELECT asset_count FROM asset_count WHERE asset_id = a_asset_id) < 1 AND is_giving = TRUE) THEN
		RAISE EXCEPTION 'No Stock Available !!!';
	END IF;

	--Check for valid asset
	IF not EXISTS (SELECT 1 FROM assets WHERE asset_id = a_asset_id AND is_giving = TRUE) THEN
		RAISE EXCEPTION 'INVALID ASSET !!!';
	END IF;

	--Check for Mapping
	IF NOT EXISTS (SELECT 1 FROM user_asset_mapping WHERE asset_id = a_asset_id 
		AND user_type_id = (SELECT user_type_id FROM users WHERE user_id = a_user_id)) THEN
			RAISE EXCEPTION 'This Asset ID = % is Not Available For Your Role', a_asset_id;
	END IF;
	
	--Check for already allocated Asset
	IF EXISTS (SELECT 1 FROM asset_assignments_summary
			WHERE user_id = a_user_id
			AND asset_id = a_asset_id
			AND operation = 'ASSIGN'::operation_type 
			AND is_active = TRUE
			AND completed_date_time IS NOT NULL) THEN
		RAISE EXCEPTION 'Asset ID = % Already Allocated !!!', a_asset_id;
		RETURN;
	END IF;

	--Check for request was already raised THEN storing the req_id in a variable
	SELECT assignment_id INTO req_assignment_id FROM asset_assignments_summary WHERE user_id = a_user_id AND
			asset_id = a_asset_id AND
			operation = 'ASSIGN'::operation_type AND
			is_active = FALSE AND
			completed_date_time IS NULL;
			
	-- If request raised then update the data
	IF EXISTS (SELECT 1 FROM asset_assignments_summary WHERE user_id = a_user_id AND
			asset_id = a_asset_id AND
			operation = 'ASSIGN'::operation_type AND
			is_active = FALSE AND
			completed_date_time IS NULL) THEN
		UPDATE asset_assignments_summary SET is_active = TRUE, completed_date_time = NOW() WHERE assignment_id = req_assignment_id;
	ELSE
	-- If admin is voluntarily allocating then insert the new data
		INSERT INTO asset_assignments_summary (user_id, asset_id, operation, is_active, completed_date_time) VALUES
			(a_user_id, a_asset_id, 'ASSIGN'::operation_type, TRUE, NOW()) RETURNING assignment_id INTO req_assignment_id;
	END IF;

	UPDATE asset_count SET asset_count = asset_count - 1 WHERE asset_id = a_asset_id;
END;
$$;


ALTER PROCEDURE public.allocate_asset(IN a_user_id integer, IN a_asset_id integer) OWNER TO postgres;

--
-- TOC entry 246 (class 1255 OID 16815)
-- Name: asset_and_user_type_mapping(integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.asset_and_user_type_mapping(IN new_asset_id integer, IN ut_id integer, OUT res integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
	--CHECKING FOR VALID USER-TYPE-ID
	IF EXISTS(SELECT 1 FROM user_types ut WHERE ut.user_type_id = ut_id) THEN
		INSERT INTO user_asset_mapping (asset_id, user_type_id) VALUES (new_asset_id, ut_id)
		RETURNING asset_id INTO res;
	ELSE
		RAISE EXCEPTION 'User type with ID % does not exist.', ut_id;
	END IF;
END;
$$;


ALTER PROCEDURE public.asset_and_user_type_mapping(IN new_asset_id integer, IN ut_id integer, OUT res integer) OWNER TO postgres;

--
-- TOC entry 248 (class 1255 OID 16802)
-- Name: raise_request(integer, integer, character varying); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.raise_request(IN new_user_id integer, IN new_asset_id integer, IN new_operation character varying)
    LANGUAGE plpgsql
    AS $$
DECLARE
    new_assignment_id INT;
BEGIN
	--Check of valid asset-id
	IF NOT EXISTS (SELECT 1 FROM assets WHERE asset_id = new_asset_id) THEN
		RAISE EXCEPTION 'INVALID ASSET-ID !!!';
	END IF;

	--Check of valid asset-id
	IF EXISTS (SELECT 1 FROM users WHERE user_id = new_user_id AND is_working = False) THEN
		RAISE EXCEPTION 'INVALID USER-ID !!!';
	END IF;
	
	--Check for Mapping
	IF NOT EXISTS (SELECT 1 FROM user_asset_mapping WHERE asset_id = new_asset_id 
		AND user_type_id = (SELECT user_type_id FROM users WHERE user_id = new_user_id)) THEN
			RAISE EXCEPTION 'This Asset is Not Available For Your Role';
	END IF;

	--Check for already allocated Asset
	IF new_operation = 'ASSIGN' AND EXISTS (SELECT 1 FROM asset_assignments_summary
			WHERE user_id = new_user_id
			AND asset_id = new_asset_id
			AND operation = new_operation::operation_type 
			AND is_active = TRUE
			AND completed_date_time IS NOT NULL) THEN
		RAISE EXCEPTION 'Asset Already Allocated !!!';
	END IF;

	--Check if request was already raised
	IF EXISTS (SELECT 1 FROM asset_assignments_summary WHERE user_id = new_user_id AND
			asset_id = new_asset_id AND
			operation = new_operation::operation_type AND
			is_active = FALSE AND
			completed_date_time IS NULL ) THEN
		RAISE EXCEPTION 'Request Already Raised !!!';
	END IF;

	--Check if the user holds the asset to request for retain
	IF new_operation = 'RETAIN' AND NOT EXISTS (SELECT 1 FROM asset_assignments_summary
			WHERE user_id = new_user_id
			AND asset_id = new_asset_id
			AND operation = 'ASSIGN'::operation_type 
			AND is_active = TRUE
			AND completed_date_time IS NOT NULL) THEN
		RAISE EXCEPTION 'This Asset is Not Allocated To This User!!!';
	END IF;

    INSERT INTO asset_assignments_summary (user_id, asset_id, operation, is_active)
    VALUES (new_user_id, new_asset_id, new_operation::operation_type, FALSE)
    RETURNING assignment_id INTO new_assignment_id;

    INSERT INTO request_table (assignment_id, requested_date_time) 
    VALUES (new_assignment_id, NOW());

END;
$$;


ALTER PROCEDURE public.raise_request(IN new_user_id integer, IN new_asset_id integer, IN new_operation character varying) OWNER TO postgres;

--
-- TOC entry 250 (class 1255 OID 16830)
-- Name: remove_asset(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.remove_asset(r_asset_id integer) RETURNS TABLE(r_user_id integer)
    LANGUAGE plpgsql
    AS $$
BEGIN

	if exists(select 1 from asset_assignments_summary where asset_id = r_asset_id
		and operation = 'ASSIGN'::operation_type
		and is_active = TRUE) then
		RETURN QUERY
    		SELECT user_id FROM asset_assignments_summary 
				where asset_id = r_asset_id
				and operation = 'ASSIGN'::operation_type
				and is_active = true;
			--raise exception 'This asset was actively allocated to Several users, Retain their assets to remove this asset !!!';
	else
		update assets set is_giving = false where asset_id = r_asset_id;
	end if;

	
END;
$$;


ALTER FUNCTION public.remove_asset(r_asset_id integer) OWNER TO postgres;

--
-- TOC entry 249 (class 1255 OID 16827)
-- Name: retain_asset(integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.retain_asset(IN r_user_id integer, IN r_asset_id integer)
    LANGUAGE plpgsql
    AS $$
DECLARE
	req_assignment_id INT;
BEGIN
	-- Check whether the user has that asset
	IF NOT EXISTS (SELECT 1 FROM asset_assignments_summary
			WHERE user_id = r_user_id
			AND asset_id = r_asset_id
			AND operation = 'ASSIGN'::operation_type 
			AND is_active = TRUE
			AND completed_date_time IS NOT NULL) THEN
		RAISE EXCEPTION 'Asset Not Allocated !!!';
	END IF;

	--Check for request was already raised THEN storing the req_id in a variable
	SELECT assignment_id INTO req_assignment_id FROM asset_assignments_summary WHERE user_id = r_user_id AND
			asset_id = r_asset_id AND
			operation = 'ASSIGN'::operation_type AND
			is_active = FALSE AND
			completed_date_time IS NULL;

	-- If request raised then update the data
	IF EXISTS (SELECT 1 FROM asset_assignments_summary WHERE user_id = r_user_id AND
			asset_id = r_asset_id AND
			operation = 'RETAIN'::operation_type AND
			is_active = FALSE AND
			completed_date_time IS NULL) THEN
		UPDATE asset_assignments_summary SET is_active = FALSE, completed_date_time = NOW() WHERE assignment_id = req_assignment_id;
	ELSE
	-- If admin is voluntarily retaining then insert the new data
		INSERT INTO asset_assignments_summary (user_id, asset_id, operation, is_active, completed_date_time) VALUES
			(r_user_id, r_asset_id, 'RETAIN'::operation_type, FALSE, NOW()) RETURNING assignment_id INTO req_assignment_id;
	END IF;

	UPDATE asset_assignments_summary SET is_active = FALSE, completed_date_time = NOW() WHERE 
		asset_id = r_asset_id AND
		user_id = r_user_id AND
		operation = 'ASSIGN'::operation_type AND
		is_active = TRUE AND
		completed_date_time IS NOT NULL;
	UPDATE retained_assets SET retained_asset_count = retained_asset_count + 1 WHERE asset_id = r_asset_id;
	
END;
$$;


ALTER PROCEDURE public.retain_asset(IN r_user_id integer, IN r_asset_id integer) OWNER TO postgres;

--
-- TOC entry 247 (class 1255 OID 16817)
-- Name: update_asset_inventory(integer, character varying, character varying, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.update_asset_inventory(IN u_asset_id integer, IN new_asset_name character varying, IN new_asset_type character varying, IN new_asset_count integer)
    LANGUAGE plpgsql
    AS $$
BEGIN
	UPDATE assets SET asset_name = new_asset_name, asset_type = new_asset_type::asset_type
	WHERE asset_id = u_asset_id;

	UPDATE asset_count SET asset_count = new_asset_count WHERE asset_id = u_asset_id;
END;
$$;


ALTER PROCEDURE public.update_asset_inventory(IN u_asset_id integer, IN new_asset_name character varying, IN new_asset_type character varying, IN new_asset_count integer) OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 231 (class 1259 OID 16803)
-- Name: admin_credentials; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.admin_credentials (
    user_name character varying(255) NOT NULL,
    pass_word character varying(255) NOT NULL
);


ALTER TABLE public.admin_credentials OWNER TO postgres;

--
-- TOC entry 218 (class 1259 OID 16660)
-- Name: asset_assignments_summary; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_assignments_summary (
    assignment_id integer NOT NULL,
    user_id integer NOT NULL,
    asset_id integer NOT NULL,
    operation public.operation_type,
    is_active boolean,
    completed_date_time timestamp without time zone
);


ALTER TABLE public.asset_assignments_summary OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16659)
-- Name: asset_assignments_summary_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asset_assignments_summary_assignment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.asset_assignments_summary_assignment_id_seq OWNER TO postgres;

--
-- TOC entry 3703 (class 0 OID 0)
-- Dependencies: 217
-- Name: asset_assignments_summary_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asset_assignments_summary_assignment_id_seq OWNED BY public.asset_assignments_summary.assignment_id;


--
-- TOC entry 220 (class 1259 OID 16667)
-- Name: asset_count; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.asset_count (
    asset_id integer NOT NULL,
    asset_count integer NOT NULL
);


ALTER TABLE public.asset_count OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16666)
-- Name: asset_count_asset_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.asset_count_asset_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.asset_count_asset_id_seq OWNER TO postgres;

--
-- TOC entry 3704 (class 0 OID 0)
-- Dependencies: 219
-- Name: asset_count_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.asset_count_asset_id_seq OWNED BY public.asset_count.asset_id;


--
-- TOC entry 222 (class 1259 OID 16679)
-- Name: assets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.assets (
    asset_id integer NOT NULL,
    asset_name character varying(255) NOT NULL,
    is_giving boolean DEFAULT true NOT NULL,
    asset_type public.asset_type
);


ALTER TABLE public.assets OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16678)
-- Name: current_assets_asset_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.current_assets_asset_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.current_assets_asset_id_seq OWNER TO postgres;

--
-- TOC entry 3705 (class 0 OID 0)
-- Dependencies: 221
-- Name: current_assets_asset_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.current_assets_asset_id_seq OWNED BY public.assets.asset_id;


--
-- TOC entry 224 (class 1259 OID 16686)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    user_name character varying(255) NOT NULL,
    is_working boolean DEFAULT true NOT NULL,
    user_type_id integer
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16685)
-- Name: current_users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.current_users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.current_users_user_id_seq OWNER TO postgres;

--
-- TOC entry 3706 (class 0 OID 0)
-- Dependencies: 223
-- Name: current_users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.current_users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 226 (class 1259 OID 16693)
-- Name: request_table; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.request_table (
    request_id integer NOT NULL,
    assignment_id integer NOT NULL,
    requested_date_time timestamp without time zone NOT NULL
);


ALTER TABLE public.request_table OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16692)
-- Name: request_table_request_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.request_table_request_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.request_table_request_id_seq OWNER TO postgres;

--
-- TOC entry 3707 (class 0 OID 0)
-- Dependencies: 225
-- Name: request_table_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.request_table_request_id_seq OWNED BY public.request_table.request_id;


--
-- TOC entry 227 (class 1259 OID 16699)
-- Name: retained_assets; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.retained_assets (
    asset_id integer NOT NULL,
    retained_asset_count integer NOT NULL
);


ALTER TABLE public.retained_assets OWNER TO postgres;

--
-- TOC entry 228 (class 1259 OID 16709)
-- Name: user_asset_mapping; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_asset_mapping (
    asset_id integer NOT NULL,
    user_type_id integer NOT NULL
);


ALTER TABLE public.user_asset_mapping OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16715)
-- Name: user_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_types (
    user_type_id integer NOT NULL,
    user_type public.user_type NOT NULL
);


ALTER TABLE public.user_types OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16714)
-- Name: user_types_user_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_types_user_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.user_types_user_type_id_seq OWNER TO postgres;

--
-- TOC entry 3708 (class 0 OID 0)
-- Dependencies: 229
-- Name: user_types_user_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_types_user_type_id_seq OWNED BY public.user_types.user_type_id;


--
-- TOC entry 3504 (class 2604 OID 16663)
-- Name: asset_assignments_summary assignment_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_assignments_summary ALTER COLUMN assignment_id SET DEFAULT nextval('public.asset_assignments_summary_assignment_id_seq'::regclass);


--
-- TOC entry 3505 (class 2604 OID 16670)
-- Name: asset_count asset_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_count ALTER COLUMN asset_id SET DEFAULT nextval('public.asset_count_asset_id_seq'::regclass);


--
-- TOC entry 3506 (class 2604 OID 16682)
-- Name: assets asset_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets ALTER COLUMN asset_id SET DEFAULT nextval('public.current_assets_asset_id_seq'::regclass);


--
-- TOC entry 3510 (class 2604 OID 16696)
-- Name: request_table request_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_table ALTER COLUMN request_id SET DEFAULT nextval('public.request_table_request_id_seq'::regclass);


--
-- TOC entry 3511 (class 2604 OID 16718)
-- Name: user_types user_type_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_types ALTER COLUMN user_type_id SET DEFAULT nextval('public.user_types_user_type_id_seq'::regclass);


--
-- TOC entry 3508 (class 2604 OID 16689)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.current_users_user_id_seq'::regclass);


--
-- TOC entry 3697 (class 0 OID 16803)
-- Dependencies: 231
-- Data for Name: admin_credentials; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.admin_credentials (user_name, pass_word) FROM stdin;
admin	admin@123
root	root@123
\.


--
-- TOC entry 3684 (class 0 OID 16660)
-- Dependencies: 218
-- Data for Name: asset_assignments_summary; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_assignments_summary (assignment_id, user_id, asset_id, operation, is_active, completed_date_time) FROM stdin;
49	21	23	ASSIGN	f	2025-01-08 21:06:52.330066
51	21	23	RETAIN	f	2025-01-08 21:11:20.277435
52	21	22	RETAIN	f	2025-01-08 21:18:26.505389
48	21	22	ASSIGN	f	2025-01-08 21:18:26.505389
53	22	23	RETAIN	f	2025-01-08 21:20:30.80786
50	22	23	ASSIGN	f	2025-01-08 21:20:30.80786
\.


--
-- TOC entry 3686 (class 0 OID 16667)
-- Dependencies: 220
-- Data for Name: asset_count; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.asset_count (asset_id, asset_count) FROM stdin;
22	49
23	98
27	100
\.


--
-- TOC entry 3688 (class 0 OID 16679)
-- Dependencies: 222
-- Data for Name: assets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.assets (asset_id, asset_name, is_giving, asset_type) FROM stdin;
22	Phone	f	HARDWARE
23	Laptop	f	HARDWARE
27	Phone	t	HARDWARE
\.


--
-- TOC entry 3692 (class 0 OID 16693)
-- Dependencies: 226
-- Data for Name: request_table; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.request_table (request_id, assignment_id, requested_date_time) FROM stdin;
23	51	2025-01-08 21:13:07.854482
\.


--
-- TOC entry 3693 (class 0 OID 16699)
-- Dependencies: 227
-- Data for Name: retained_assets; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.retained_assets (asset_id, retained_asset_count) FROM stdin;
22	1
23	2
27	0
\.


--
-- TOC entry 3694 (class 0 OID 16709)
-- Dependencies: 228
-- Data for Name: user_asset_mapping; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_asset_mapping (asset_id, user_type_id) FROM stdin;
22	1
23	1
23	2
23	3
27	1
\.


--
-- TOC entry 3696 (class 0 OID 16715)
-- Dependencies: 230
-- Data for Name: user_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_types (user_type_id, user_type) FROM stdin;
1	MANAGER
2	EMPLOYEE
3	TRAINEE
\.


--
-- TOC entry 3690 (class 0 OID 16686)
-- Dependencies: 224
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, user_name, is_working, user_type_id) FROM stdin;
21	UserZ	f	1
22	Sample User 2	f	2
23	User 100	t	1
24	User 100	t	1
\.


--
-- TOC entry 3709 (class 0 OID 0)
-- Dependencies: 217
-- Name: asset_assignments_summary_assignment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asset_assignments_summary_assignment_id_seq', 53, true);


--
-- TOC entry 3710 (class 0 OID 0)
-- Dependencies: 219
-- Name: asset_count_asset_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.asset_count_asset_id_seq', 1, false);


--
-- TOC entry 3711 (class 0 OID 0)
-- Dependencies: 221
-- Name: current_assets_asset_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.current_assets_asset_id_seq', 28, true);


--
-- TOC entry 3712 (class 0 OID 0)
-- Dependencies: 223
-- Name: current_users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.current_users_user_id_seq', 24, true);


--
-- TOC entry 3713 (class 0 OID 0)
-- Dependencies: 225
-- Name: request_table_request_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.request_table_request_id_seq', 23, true);


--
-- TOC entry 3714 (class 0 OID 0)
-- Dependencies: 229
-- Name: user_types_user_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_types_user_type_id_seq', 1, false);


--
-- TOC entry 3529 (class 2606 OID 16809)
-- Name: admin_credentials admin_credentials_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.admin_credentials
    ADD CONSTRAINT admin_credentials_pkey PRIMARY KEY (user_name, pass_word);


--
-- TOC entry 3513 (class 2606 OID 16665)
-- Name: asset_assignments_summary asset_assignments_summary_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_assignments_summary
    ADD CONSTRAINT asset_assignments_summary_pkey PRIMARY KEY (assignment_id);


--
-- TOC entry 3515 (class 2606 OID 16672)
-- Name: asset_count asset_count_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_count
    ADD CONSTRAINT asset_count_pkey PRIMARY KEY (asset_id);


--
-- TOC entry 3517 (class 2606 OID 16684)
-- Name: assets current_assets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.assets
    ADD CONSTRAINT current_assets_pkey PRIMARY KEY (asset_id);


--
-- TOC entry 3519 (class 2606 OID 16691)
-- Name: users current_users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT current_users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 3521 (class 2606 OID 16698)
-- Name: request_table request_table_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_table
    ADD CONSTRAINT request_table_pkey PRIMARY KEY (request_id);


--
-- TOC entry 3523 (class 2606 OID 16703)
-- Name: retained_assets retained_assets_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retained_assets
    ADD CONSTRAINT retained_assets_pkey PRIMARY KEY (asset_id);


--
-- TOC entry 3525 (class 2606 OID 16713)
-- Name: user_asset_mapping user_asset_mapping_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_asset_mapping
    ADD CONSTRAINT user_asset_mapping_pkey PRIMARY KEY (asset_id, user_type_id);


--
-- TOC entry 3527 (class 2606 OID 16720)
-- Name: user_types user_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_types
    ADD CONSTRAINT user_types_pkey PRIMARY KEY (user_type_id);


--
-- TOC entry 3530 (class 2606 OID 16726)
-- Name: asset_assignments_summary fk_asset_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_assignments_summary
    ADD CONSTRAINT fk_asset_id FOREIGN KEY (asset_id) REFERENCES public.assets(asset_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3532 (class 2606 OID 16736)
-- Name: asset_count fk_asset_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_count
    ADD CONSTRAINT fk_asset_id FOREIGN KEY (asset_id) REFERENCES public.assets(asset_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3535 (class 2606 OID 16751)
-- Name: retained_assets fk_asset_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.retained_assets
    ADD CONSTRAINT fk_asset_id FOREIGN KEY (asset_id) REFERENCES public.assets(asset_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3536 (class 2606 OID 16761)
-- Name: user_asset_mapping fk_asset_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_asset_mapping
    ADD CONSTRAINT fk_asset_id FOREIGN KEY (asset_id) REFERENCES public.assets(asset_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3534 (class 2606 OID 16746)
-- Name: request_table fk_request_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.request_table
    ADD CONSTRAINT fk_request_id FOREIGN KEY (assignment_id) REFERENCES public.asset_assignments_summary(assignment_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3531 (class 2606 OID 16731)
-- Name: asset_assignments_summary fk_user_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.asset_assignments_summary
    ADD CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3537 (class 2606 OID 16766)
-- Name: user_asset_mapping fk_user_type_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_asset_mapping
    ADD CONSTRAINT fk_user_type_id FOREIGN KEY (user_type_id) REFERENCES public.user_types(user_type_id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 3533 (class 2606 OID 16772)
-- Name: users fk_user_type_id; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk_user_type_id FOREIGN KEY (user_type_id) REFERENCES public.user_types(user_type_id);


-- Completed on 2025-01-08 21:46:34 IST

--
-- PostgreSQL database dump complete
--

