CREATE TABLE phone (
    id UUID PRIMARY KEY,
    "name" VARCHAR(50) NOT NULL,
    spec TEXT,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT idx_uk_phone_name UNIQUE ("name")
);

CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    phone_id UUID NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_phone_id__id FOREIGN KEY (phone_id) REFERENCES phone(id) ON DELETE CASCADE
);

CREATE TABLE rental (
    id UUID PRIMARY KEY,
    inventory_id UUID NOT NULL,
    user_email VARCHAR(50) NOT NULL,
    rent_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    return_at TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_rental_inventory_id__id FOREIGN KEY (inventory_id) REFERENCES inventory(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_uk_rental_rent_date_inventory_id_user_email ON rental USING BTREE (rent_at , inventory_id, user_email);
