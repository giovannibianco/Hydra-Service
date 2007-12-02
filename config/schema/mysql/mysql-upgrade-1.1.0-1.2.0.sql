--
-- Upgrade from 1.1.0  to 1.2.0
-- 


DELETE FROM t_meta_vers WHERE major = 1 AND minor = 1 and patch = 0;
INSERT INTO t_meta_vers (major,minor,patch) VALUES (1,2,0);

--
-- Default schema for Hydra keystore
--  
ALTER TABLE eds (
  ADD COLUMN edskeysneeded	INT NOT NULL,
  ADD COLUMN edskeyindex	INT NOT NULL
);

