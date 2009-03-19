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
    private final static Logger m_log = Logger.getLogger(MySQLSchemaHelper.class);

    /**
     * @param dbManager
     */
    public MySQLSchemaHelper(DBManager dbManager) {
        super(dbManager);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchema(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */

    public void addSchema(String schemaName, Attribute[] attributes) throws InternalException {
        m_log.error("addSchema not implemented in Hydra Service.");
        throw new InternalException("addSchema not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchema(java.lang.String)
     */
    public void removeSchema(String schemaName) throws InternalException {
        m_log.error("removeSchema not implemented in Hydra Service.");
        throw new InternalException("removeSchema not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchemaAttribute(java.lang.String, org.glite.data.catalog.service.Attribute)
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributes) throws InternalException {
        m_log.error("addSchemaAttributes not implemented in Hydra Service.");
        throw new InternalException("addSchemaAttributes not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames) throws InternalException {
        m_log.error("removeSchemaAttributes not implemented in Hydra Service.");
        throw new InternalException("removeSchemaAttributes not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemas(java.lang.String)
     */
    public String[] getSchemas(String schemaNamePattern) throws InternalException {
        m_log.error("getSchemas not implemented in Hydra Service.");
        throw new InternalException("getSchemas not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaDescription(java.lang.String)
     */
    public Attribute[] getSchemaDescription(String schemaName) throws InternalException {
        m_log.error("getSchemaDescription not implemented in Hydra Service.");
        throw new InternalException("getSchemaDescription not implemented in Hydra Service.");
    }

    public void checkSchemaNameValidity(String schemaName)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered checkSchemaNameValidity.");

        if (!MySQLUtils.isValidTableIdentifier(schemaName)) {
            m_log.debug("The schema name given is invalid. Schema name: " + schemaName);
            throw new InvalidArgumentException("The schema name given is invalid. Schema name: " + schemaName);
        }
    }

    public void checkColumnNameValidity(String columnName)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered checkColumnNameValidity.");

        if (!MySQLUtils.isValidColumnIdentifier(columnName)) {
            m_log.debug("The schema name given is invalid. Column name: " + columnName);
            throw new InvalidArgumentException("The schema name given is invalid. Column name: " + columnName);
        }
    }

    public void checkAttributesValidity(Attribute[] attributes) throws InternalException {
        m_log.error("checkAttributesValidity not implemented in Hydra Service");
        throw new InternalException("checkAttributesValidity not implemented in Hydra Service");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaIds(java.lang.String)
     */
    public int getSchemaId(String schemaName) throws InternalException {
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
