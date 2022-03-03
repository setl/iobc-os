CREATE SCHEMA IF NOT EXISTS iobc;

CREATE TABLE IF NOT EXISTS iobc.address
(
    chain_id      VARCHAR   NOT NULL,
    chain_brand   VARCHAR   NOT NULL,
    wallet_id     INTEGER   NOT NULL,
    address_type  INTEGER   NOT NULL,
    setl_address  VARCHAR   NOT NULL PRIMARY KEY,
    chain_address VARCHAR   NOT NULL,
    key_type      VARCHAR   NOT NULL,
    wrap_id       VARCHAR   NOT NULL,
    public_key    VARBINARY NOT NULL,
    private_key   VARBINARY NOT NULL
);


CREATE INDEX IF NOT EXISTS iobc.i_address_wallet
    ON iobc.address (wallet_id, setl_address);

CREATE UNIQUE INDEX IF NOT EXISTS iobc.i_address_chain
    ON iobc.address (chain_address);


CREATE TABLE IF NOT EXISTS iobc.configuration
(
    pair_name  VARCHAR NOT NULL
        CONSTRAINT configuration_pkey PRIMARY KEY,
    pair_value VARCHAR
);


CREATE TABLE IF NOT EXISTS iobc.token
(
    symbol          VARCHAR                  NOT NULL PRIMARY KEY,
    controller      VARCHAR                  NOT NULL REFERENCES iobc.address,
    chain_id        VARCHAR                  NOT NULL,
    chain_brand     VARCHAR                  NOT NULL,
    create_time     TIMESTAMP WITH TIME ZONE NOT NULL,
    is_loading      BOOLEAN                  NOT NULL,
    name            VARCHAR                  NOT NULL,
    additional_data VARCHAR                  NOT NULL
);


