/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers.attribute;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.StringPair;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.hydra.helpers.AttributeHelper;
import org.glite.data.hydra.helpers.MetadataHelper;
import org.glite.data.hydra.helpers.authz.MySQLAuthorizationHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.ArrayList;
import java.util.List;


public class MySQLAttributeHelper extends AttributeHelper {
    // Logger object
    private final static Logger m_log = Logger.getLogger(
            "org.glite.data.catalog.service.meta.mysql.MySQLAttributeHelper");

    /**
     * @param dbManager
     */
    public MySQLAttributeHelper(DBManager dbManager) {
        super(dbManager);
    }

    /* (non-Javadoc)
     * Usage of ResultSetMetadata: the mm-mysql drivers implementation sucks! TypeName comes without precision,
     * you should use getPrecision and getScale to retrieve precision of numeric types but getDisplaySize for char types,
     * in one special case - TINYINT - the thing returns simply TINY ????? which is not a valid MySQL type.
     * @see org.glite.data.catalog.service.meta.helpers.AttributeHelper#getAttributes(java.lang.String, java.lang.String[])
     */
    public Attribute[] getAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException {
        m_log.debug("Entered getAttributes.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Internal objects
        ResultSet rs = null;
        Attribute[] attributes = null;
        String schemaName = null;
        int entryID;

        // Get the entry information
        StringPair entryAndSchema = this.getEntryInfo(entry);
        entryID = Integer.parseInt(entryAndSchema.getString1());
        schemaName = entryAndSchema.getString2();

        // Prepare SQL string
        String sql = null;

        if (attributeNames != null) {
            sql = getAttributesSQLString(entryID, schemaName, attributeNames);
        } else {
            sql = getAllAttributesSQLString(entryID, schemaName);
        }

        m_log.debug("SQL String: " + sql);

        try {
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sql);

            // Execute query
            rs = p_stat.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();

            // Prepare the returned attributes array
            if (attributeNames != null) {
                attributes = new Attribute[attributeNames.length];
            } else {
                attributes = new Attribute[rsmd.getColumnCount() - 1]; // entry_id does not go as attribute
            }

            if (rs.next()) {
                // Parse the results and fill up the array of Attributes
                int attrCounter = 0; // do not count entry_id

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    Attribute attr = new Attribute();
                    String attrName = rsmd.getColumnName(i);

                    if (!attrName.equals("entry_id")) {
                        attr.setName(attrName);
                        attr.setType(rsmd.getColumnTypeName(i));
                        attr.setValue(rs.getString(i));
                        attributes[attrCounter] = attr;
                        ++attrCounter;
                    }
                }
            }
        } catch (Exception sqle) {
            m_log.debug("Could not get attributes for entry. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);			
            throw new InternalException("Error getting attributes for entry.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(rs);
        }

        return attributes;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AttributeHelper#setAttributes(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void setAttributes(String entry, Attribute[] attributes)
        throws NotExistsException, InternalException {
        m_log.debug("Entered setAttributes.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Internal Objects
        String schemaName = null;
        int entryID;

        // Get the entry information
        StringPair entryAndSchema = this.getEntryInfo(entry);
        entryID = Integer.parseInt(entryAndSchema.getString1());
        schemaName = entryAndSchema.getString2();

        // Set the attributes
        try {
            conn = m_dbmanager.getConnection(false);

            // Try to update existing record
            String sqlUpdate = getSetAttributesSQLUpdateString(schemaName, entryID, attributes);
            m_log.debug("SQL Update String: " + sqlUpdate);
            p_stat = m_dbmanager.prepareStatement(conn, sqlUpdate);

            int updateResult = p_stat.executeUpdate();
            m_log.debug("Update returned: " + updateResult);

            // Create a new record in the table if one was not there
            if (updateResult == 0) {
                String sqlInsert = getSetAttributesSQLInsertString(schemaName, entryID, attributes);
                m_log.debug("SQL Insert String: " + sqlInsert);
                p_stat = m_dbmanager.prepareStatement(conn, sqlInsert);
                p_stat.executeUpdate();
            }

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not set attributes for entry. Exception was: " + sqle);
           	m_dbmanager.rollbackRegardless(conn);
            throw new InternalException("Error setting attributes for entry.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AttributeHelper#clearAttributes(java.lang.String, java.lang.String[])
     */
    public void clearAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException {
        m_log.debug("Entered clearAttributes.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Internal Objects
        String schemaName = null;
        int entryID;

        // Get the entry information
        StringPair entryAndSchema = this.getEntryInfo(entry);
        entryID = Integer.parseInt(entryAndSchema.getString1());
        schemaName = entryAndSchema.getString2();

        // Generate empty attribute objects
        Attribute[] attributes = new Attribute[attributeNames.length];

        for (int i = 0; i < attributeNames.length; i++) {
            Attribute attr = new Attribute();
            attr.setName(attributeNames[i]);
            attr.setValue("");
            attributes[i] = attr;
        }

        // Clear the attributes
        try {
            conn = m_dbmanager.getConnection(false);

            // Try to update existing record
            String sqlUpdate = getSetAttributesSQLUpdateString(schemaName, entryID, attributes);
            m_log.debug("SQL Update String: " + sqlUpdate);
            p_stat = m_dbmanager.prepareStatement(conn, sqlUpdate);

            int updateResult = p_stat.executeUpdate();
            m_log.debug("Update returned: " + updateResult);

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.debug("Could not clear attributes for entry. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);			
            throw new InternalException("Error clearing attributes for entry.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    public String[] query(String query, String type, int limit, int offset)
        throws InternalException {
        m_log.debug("Entered query.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;
        ResultSet rs = null;

        // Internal objects
        ArrayList entries = new ArrayList();

        // Make the query
        try {
            conn = m_dbmanager.getConnection(false);

            // Process the user VOMS attributes
            List attrs = MySQLAuthorizationHelper.getAuthzAttributeList();
            String attrsString = null;
            if(attrs != null) {
                if(attrs.size() > 0) {
                    attrsString = "'" + attrs.get(0) + "'";
                    for(int i=1; i < attrs.size(); i++) {
                        attrsString += ",'" + attrs.get(i) + "'";
                    }
                }
            }

            String sql = "SELECT entry_name "
                + "FROM t_entry T "
                + "LEFT JOIN t_schema S ON S.schema_id = T.schema_id "
                + "LEFT JOIN t_principal UP ON UP.principal_id = T.owner_id "
                + "LEFT JOIN t_principal GP ON GP.principal_id = T.group_id "
                + "WHERE S.schema_name = '" + query + "' "
                + "  AND ( "
                + "    ((T.other_perm & 4) = 4) "
                + "    OR "
                + "    (UP.principal_name = '" + MySQLAuthorizationHelper.getClientName() + "' AND (T.user_perm & 4) = 4) ";
            // Add group permissions if VOMS attributes were exposed
            if(attrsString != null) {
                sql += " OR (GP.principal_name IN ('default-group') AND (T.group_perm & 4) = 4) ";
            }
            // Wrap up
            sql += ") ORDER BY creation_time DESC ";
            // Add limits if given
            if(limit != -1) {
                sql+= " LIMIT " + offset + ", " + limit;
            }
            m_log.debug("SQL:" + sql);

            p_stat = m_dbmanager.prepareStatement(conn, sql);

            // Execute query
            rs = p_stat.executeQuery();

            // Parse results
            while(rs.next()) {
                entries.add(rs.getString("entry_name"));
            }
            m_log.debug("Returning " + entries.size() + " entries.");
        } catch (Exception sqle) {
            m_log.debug("Failed to execute query. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);			
            throw new InternalException("Failed to execute query on database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(rs);
        }

        // Return the results
        return (String[])entries.toArray(new String[] {});
    }

    private StringPair getEntryInfo(String entry) throws NotExistsException {
        m_log.debug("Entered getEntryInfo.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;
        ResultSet rs = null;

        // Internal Objects
        StringPair entryAndSchema = null;

        // Prepare the query SQL string
        String sqlQuery = "SELECT E.entry_id, S.schema_name FROM t_entry E, t_schema S" +
            " WHERE E.schema_id = S.schema_id AND E.entry_name = ?";

        // Execute the schema query for the entry
        try {
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sqlQuery);

            p_stat.setString(1, entry);
            rs = p_stat.executeQuery();

            if (rs.next()) {
                entryAndSchema = new StringPair();
                entryAndSchema.setString1(rs.getString("entry_id"));
                entryAndSchema.setString2(rs.getString("schema_name"));
            }
        } catch (Exception sqle) {
            m_log.debug("Entry " + entry + " not found in the catalog.");
            throw new NotExistsException("Entry " + entry + " not found in the catalog.");
        } finally {
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(p_stat);
        }

        return entryAndSchema;
    }

    private String getSetAttributesSQLUpdateString(String schemaName, int entryID, Attribute[] attributes) {
        // Init string
        String sqlUpdate = "UPDATE " + schemaName + " SET ";

        // Parse attributes and set strings
        for (int i = 0; i < (attributes.length - 1); i++) {
            sqlUpdate += (attributes[i].getName() + " = '" + attributes[i].getValue() + "', ");
        }

        sqlUpdate += (attributes[attributes.length - 1].getName() + " = '" +
        attributes[attributes.length - 1].getValue() + "'" + " WHERE entry_id = " + entryID);

        // Return
        return sqlUpdate;
    }

    private String getSetAttributesSQLInsertString(String schemaName, int entryID, Attribute[] attributes) {
        // Init partial strings
        String sqlInsert = "INSERT INTO " + schemaName;
        String sqlInsertFields = " (";
        String sqlInsertValues = " VALUES (";

        // Parse attributes and set strings
        for (int i = 0; i < attributes.length; i++) {
            sqlInsertFields += (attributes[i].getName() + ", ");
            sqlInsertValues += ("'" + attributes[i].getValue() + "', ");
        }

        sqlInsertFields += "entry_id)";
        sqlInsertValues += (entryID + ")");

        // Wrap up and return
        return sqlInsert + sqlInsertFields + sqlInsertValues;
    }

    private String getSetAttributesSQLUniqueString(String schemaName, int entryID, Attribute[] attributes) {
        String sqlUpdate = "INSERT INTO " + schemaName;
        String sqlUpdateFields = " (";
        String sqlUpdateValues = " VALUES (";
        String sqlUpdateDuplicates = " ON DUPLICATE KEY UPDATE ";

        for (int i = 0; i < (attributes.length - 1); i++) {
            sqlUpdateFields += (attributes[i].getName() + ", ");
            sqlUpdateValues += ("'" + attributes[i].getValue() + "', ");
            sqlUpdateDuplicates += (attributes[i].getName() + " = '" + attributes[i].getValue() + "', ");
        }

        sqlUpdateFields += (attributes[attributes.length - 1].getName() + ")");
        sqlUpdateValues += ("'" + attributes[attributes.length - 1].getValue() + "')");
        sqlUpdateDuplicates += (attributes[attributes.length - 1].getName() + " = '" +
        attributes[attributes.length - 1].getValue() + "'" + " WHERE entry_id = " + entryID);

        // Wrap up 
        return sqlUpdate + sqlUpdateFields + sqlUpdateValues + sqlUpdateDuplicates;
    }

    private String getAttributesSQLString(int entryID, String schemaName, String[] attributeNames) {
        String sql = "SELECT ";

        for (int i = 0; i < (attributeNames.length - 1); i++) {
            sql += (attributeNames[i] + ", ");
        }

        sql += (attributeNames[attributeNames.length - 1] + " FROM " + schemaName + " WHERE entry_id = " + entryID);

        return sql;
    }

    private String getAllAttributesSQLString(int entryID, String schemaName) {
        return "SELECT * FROM " + schemaName + " WHERE entry_id = " + entryID;
    }

}
