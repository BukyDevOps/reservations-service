CREATE TABLE IF NOT EXISTS reservation (
    id bigint PRIMARY KEY,
    accommodation_id bigint,
    guests_num integer,
    host_id bigint,
    price_by_guest double precision,
    reservation_end date,
    reservation_start date,
    reservation_status smallint,
    total_price double precision,
    user_id bigint,
    CONSTRAINT reservation_reservation_status_check CHECK (((reservation_status >= 0) AND (reservation_status <= 7)))
);