--
-- The MySQL schema for the Metadata Catalog database backend
--

--
-- Table holding item entries
--	entry_id:			internal identifier
--  entry_name:			string referencing the entry in the catalog (lfn, guid, ...)
--	user_name:			string with the individual owner of the entry (normally its DN)
--	group_name:			string with the group owner of the entry
--	user_perm:			integer containing the permissions for the entry owner
--	group_perm:			integer containing the permissions for the entry group
--	other_perm:			integer containing the permissions for all others on the entry
--  schema_id:			reference to the schema the entry belongs through an internal identifier
--	basic_perm_id:		reference to the basic permissions entry
-- Additional internal attributes
--  creation_time:		timestamp of the entry creation
--
DROP TABLE IF EXISTS t_entry;
CREATE TABLE t_entry (
	entry_id		INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	entry_name		VARCHAR(250) UNIQUE NOT NULL,
	owner_id		INT NOT NULL REFERENCES t_principal(principal_id),
	group_id		INT NOT NULL REFERENCES t_principal(principal_id),
	user_perm		TINYINT(3) UNSIGNED NOT NULL,
	group_perm		TINYINT(3) UNSIGNED NOT NULL,
	other_perm		TINYINT(3) UNSIGNED NOT NULL,	
	schema_id		INT	NOT NULL REFERENCES t_schema(schema_id),	
	creation_time	TIMESTAMP NOT NULL
) TYPE=INNODB;

--
-- Table holding schemas
--	schema_id:			internal identifier
--	schema_name:		a unique name for referencing the schema
--	schema_description:	a longer text description of the schema ?? needed
--
DROP TABLE IF EXISTS t_schema;
CREATE TABLE t_schema (
	schema_id			INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	schema_name			VARCHAR(100) NOT NULL UNIQUE
) TYPE=INNODB;

--
-- Table holding principals
--	principal_id:	internal identifier
--	principal_name:	user/client DN
--
DROP TABLE IF EXISTS t_principal;
CREATE TABLE t_principal (
	principal_id	INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	principal_name	VARCHAR(250) UNIQUE NOT NULL
) TYPE=INNODB;

--
-- Table holding acls
--	entry_id:		reference to the entry
--	principal_id:	reference to the principal
--	get_meta_perm:	boolean defining if given principal/client/user can get metadata for entry
--	set_meta_perm:	boolean defining if given principal/client/user can set metadata for entry
--
DROP TABLE IF EXISTS t_acl;
CREATE TABLE t_acl (
	entry_id		INT NOT NULL REFERENCES t_entry(entry_id),
	principal_id	INT NOT NULL REFERENCES t_principal(principal_id),
	perm			TINYINT(3) UNSIGNED NOT NULL,
	PRIMARY KEY (entry_id, principal_id)
) TYPE=INNODB;

