CREATE TABLE parking_spaces (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE requesters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    parking_space_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_reservations_parking_space
        FOREIGN KEY (parking_space_id)
        REFERENCES parking_spaces(id),

    CONSTRAINT fk_reservations_requester
        FOREIGN KEY (requester_id)
        REFERENCES requesters(id),

    CONSTRAINT chk_reservation_time_range
        CHECK (start_time < end_time)
);