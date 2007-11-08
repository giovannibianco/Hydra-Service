/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra.helpers.schema;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.ExistsException;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.hydra.helpers.MySQLUtils;
import org.glite.data.hydra.helpers.SchemaHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;


//TODO: Figure out a policy for allowing types for attributes
// For example, MySQL will accept BOOL, but stores it as TINYINT(1)
// If schema description is asked later, TINYINT(1) will be retrieved
// Probably the best way is to restrict types to those actually used in the backend
public class MySQLSchemaHelper extends SchemaHelper {
    // Logger object
    private final static Logger m_log = Logger.getLogger("org.glite.data.catalog.service.meta.mysql.MySQLSchemaHelper");

    /**
     * @param dbManager
     */
    public MySQLSchemaHelper(DBManager dbManager) {
        super(dbManager);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchema(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */

    //TODO: add creation_time and creator_dn to schema??
    public void addSchema(String schemaName, Attribute[] attributes)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered addSchema");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Initiate the table creation sql string
        String sqlNewTable = "CREATE TABLE " + schemaName + " (entry_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, ";

        // Add each of the attributes of the new table
        for (int i = 0; i < (attributes.length - 1); i++) {
            Attribute attribute = attributes[i];
            sqlNewTable += (attribute.getName() + " " + attribute.getType() + " DEFAULT '" + attribute.getValue() +
            "', ");
        }

        Attribute attribute = attributes[attributes.length - 1];
        sqlNewTable += (attribute.getName() + " " + attribute.getType() + " DEFAULT '" + attribute.getValue() + "'");

        // Complement the table creation sql string
        sqlNewTable += ") TYPE=INNODB";
        m_log.debug("Table Creation SQL String: " + sqlNewTable);

        // Prepare the SQL string for adding the new entry in the schema table
        String sqlNewSchemaRecord = "INSERT INTO t_schema (schema_name) VALUES (?)";

        // Create new schema in the database
        try {
            conn = m_dbmanager.getConnection(false);

            // Create the new table in the database
            p_stat = m_dbmanager.prepareStatement(conn, sqlNewTable);
            p_stat.executeUpdate();

            // Add the new record to the schema table in the database
            p_stat = m_dbmanager.prepareStatement(conn, sqlNewSchemaRecord);
            p_stat.setString(1, schemaName);
            p_stat.executeUpdate();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not create new schema. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);
            throw new InternalException("Error creating new schema in database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchema(java.lang.String)
     */
    public void removeSchema(String schemaName) throws InternalException {
        m_log.debug("Entered removeSchema.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Construct the SQL strings
        String sqlDeleteSchemaEntry = "DELETE FROM t_schema WHERE schema_name = ?";
        String sqlDeleteSchemaTable = "DROP TABLE " + schemaName;

        // Delete the schema from the database
        try {
            conn = m_dbmanager.getConnection(false);

            // Delete the schema entry in the schema table
            p_stat = conn.prepareStatement(sqlDeleteSchemaEntry);
            p_stat.setString(1, schemaName);
            p_stat.executeUpdate();

            // Drop the table from the database
            p_stat = conn.prepareStatement(sqlDeleteSchemaTable);
            p_stat.executeUpdate();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not delete schema from database. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);			
            throw new InternalException("Error deleting schema from database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchemaAttribute(java.lang.String, org.glite.data.catalog.service.Attribute)
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributes)
        throws ExistsException, InternalException {
        m_log.debug("Entered addSchemaAttribute.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Construct the SQL string
        String sql = "ALTER TABLE " + schemaName;

        for (int i = 0; i < (attributes.length - 1); i++) {
            Attribute attr = attributes[i];
            sql += (" ADD COLUMN " + attr.getName() + " " + attr.getType() + " DEFAULT '" + attr.getValue() + "', ");
        }

        Attribute attr = attributes[attributes.length - 1];
        sql += (" ADD COLUMN " + attr.getName() + " " + attr.getType() + " DEFAULT '" + attr.getValue() + "'");
        m_log.debug("SQL String: " + sql);

        // Add the new attributes to the schema
        try {
            conn = m_dbmanager.getConnection(false);

            // Add the new column to the schema table
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            p_stat.executeUpdate();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not add new columns to the schema table in the database. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);			
            throw new InternalException("Error adding new attributes to the schema in the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws InternalException, NotExistsException {
        m_log.debug("Entered removeSchemaAttributes.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Construct the SQL string
        String sql = "ALTER TABLE " + schemaName;

        for (int i = 0; i < (attributeNames.length - 1); i++) {
            sql += (" DROP COLUMN " + attributeNames[i] + ", ");
        }

        sql += ("DROP COLUMN " + attributeNames[attributeNames.length - 1]);

        try {
            conn = m_dbmanager.getConnection(false);

            // Drop the attributes from the schema table
            p_stat = conn.prepareStatement(sql);
            p_stat.executeUpdate();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not remove the columns from the schema table. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);		
            throw new InternalException("Error removing schema attributes from the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemas(java.lang.String)
     */
    public String[] getSchemas(String schemaNamePattern)
        throws InternalException {
        m_log.debug("Entered getSchemas.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;
        ResultSet rs = null;

        // Internal Objects
        ArrayList schemas = null;
        String pattern = "%"; // default is for all schemas to be returned

        // Update schema pattern if one was provided
        if (schemaNamePattern != null) {
            pattern = schemaNamePattern;
        }

        // Prepare the sql string
        String sql = "SELECT schema_name FROM t_schema WHERE schema_name LIKE ?";

        try {
            // Execute the query			
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            p_stat.setString(1, pattern);

            rs = p_stat.executeQuery();

            // Parse the results
            schemas = new ArrayList();

            while (rs.next()) {
                schemas.add(rs.getString("schema_name"));
            }
        } catch (Exception sqle) {
            m_log.debug("Error while returning schemas. Exception was: ", sqle);
            throw new InternalException("Error accessing schemas in the database");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(rs);
        }

        // Convert and return the results
        return (String[]) schemas.toArray(new String[] {  });
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaDescription(java.lang.String)
     */
    public Attribute[] getSchemaDescription(String schemaName)
        throws InternalException {
        m_log.debug("Entered getSchemaDescription.");

        //TODO: Make this method use rs.getMetadata() instead of
        // MySQL specific stuff - mm mysql drivers to not implement this method
        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        //ResultSetMetaData rsmd = null;
        ResultSet rs = null;

        // Attribute holder array
        ArrayList schemaAttributes = new ArrayList();

        // Prepare the SQL string - we'll use a dummy query
        String sql = "DESC " + schemaName; // setting in p_stat puts '' around string and fails

        try {
            conn = m_dbmanager.getConnection(false);

            // Get the schema table metadata
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            rs = p_stat.executeQuery();

            // Build the schema attributes array
            //int numColumns = rsmd.getColumnCount();
            //m_log.debug("num columns: " + numColumns);
            while (rs.next()) {
                // Skip the entry_id column - it is not an attribute, but the identifier
                if (rs.getString("Field").equals("entry_id")) {
                    continue;
                }

                // Get the attribute information
                Attribute attr = new Attribute();
                String attrName = rs.getString("Field");
                String attrType = rs.getString("Type").toUpperCase();
                String attrValue = rs.getString("Default");
                m_log.debug("Name: " + attrName + "\tType: " + attrType + "\tValue: " + attrValue);

                /**attr.setName(rsmd.getColumnName(i));
                attr.setType(rsmd.getColumnTypeName(i));*/
                attr.setName(attrName);
                attr.setType(attrType);
                attr.setValue(attrValue);
                schemaAttributes.add(attr);
            }
        } catch (Exception sqle) {
            m_log.debug("Error while checking schema table description. Exception was: ", sqle);
            throw new InternalException("Error accessing schema description in the database");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }

        return (Attribute[]) schemaAttributes.toArray(new Attribute[] {  });
    }

    public void checkSchemaNameValidity(String schemaName)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered checkSchemaNameValidity.");

        if (!MySQLUtils.isValidTableIdentifier(schemaName)) {
            m_log.debug("The schema name given is invalid. Schema name: " + schemaName);
            throw new InvalidArgumentException("The schema name given is invalid. Schema name: " + schemaName);
        }
    }

    public void checkAttributesValidity(Attribute[] attributes)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered checkAttributesValidity.");

        //TODO: implement - entry_id needs to be invalid
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaIds(java.lang.String)
     */
    public int getSchemaId(String schemaName) throws InternalException {
        //TODO: schema ids are not really exposed in the interface, so
        // maybe MetadataCatalogImpl should deal only with schema names
        m_log.debug("Entered getSchemaId.");

        // Internal objects
        int schemaId = 0;

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;
        ResultSet rs = null;

        // Prepare the SQL string
        String sql = "SELECT schema_id FROM t_schema WHERE schema_name = ?";

        // Execute the query
        try {
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            p_stat.setString(1, schemaName);

            rs = p_stat.executeQuery();

            if (rs.next()) {
                schemaId = rs.getInt("schema_id");
            }
        } catch (Exception sqle) {
            m_log.debug("Error while returning schemas. Exception was: ", sqle);
            throw new InternalException("Error accessing schemas in the database");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(rs);
        }

        // Convert and return the results
        return schemaId;
    }
}
