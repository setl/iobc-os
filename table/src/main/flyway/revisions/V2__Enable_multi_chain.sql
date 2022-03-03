SET SEARCH_PATH TO iobc;
--
-- Add the chain ID column to the address table
ALTER TABLE iobc.address
    ADD COLUMN chainId VARCHAR(30) NOT NULL DEFAULT 'NONE';