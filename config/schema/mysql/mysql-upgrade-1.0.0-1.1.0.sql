--
-- Upgrade from 1.0.0 (without explicit schema version) to 1.1.0
-- 


--
-- Schema version
--
CREATE TABLE t_meta_vers (
  major INTEGER NOT NULL,
  minor INTEGER NOT NULL,
  patch INTEGER NOT NULL
);
INSERT INTO t_meta_vers (major,minor,patch) VALUES (1,1,0);

--
-- Default schema for Hydra keystore
--  
CREATE TABLE eds (
  entry_id      INT NOT NULL,
  edscipher     VARCHAR(50),
  edskey        VARCHAR(200),
  edsiv         VARCHAR(200),
  edskeyinfo    VARCHAR(200)
);

--
-- Insert entry for eds schema
--
INSERT INTO t_schema (schema_name) VALUES ('eds');
 
