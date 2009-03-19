/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra.helpers.attribute;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.StringPair;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.common.helpers.DBException;
import org.glite.data.hydra.helpers.AttributeHelper;
import org.glite.data.hydra.helpers.authz.MySQLAuthorizationHelper;
import org.glite.data.hydra.helpers.MySQLUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;


public class MySQLAttributeHelper extends AttributeHelper {
    // Logger object
    private final static Logger m_log = Logger.getLogger(MySQLAttributeHelper.class);

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
        String filter = "";

        if (attributeNames != null) {
            for (int i = 0; i < attributeNames.length; i++) {
                filter += (i==0 ? "" : ", ") + validateColumnName(attributeNames[i]);
            }
        } else {
            filter = "*";
        }

        String sql = "SELECT " + filter + " FROM " + validateSchemaName(schemaName) + " WHERE entry_id = ?";

        try {
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            p_stat.setInt(1, entryID);

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
            p_stat = getSetAttributesSQLUpdateStatement(conn, schemaName, entryID, attributes);
            int updateResult = p_stat.executeUpdate();

            m_log.debug("Update returned: " + updateResult);

            // Create a new record in the table if one was not there
            if (updateResult == 0) {
                p_stat = getSetAttributesSQLInsertStatement(conn, schemaName, entryID, attributes);
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
            p_stat = getSetAttributesSQLUpdateStatement(conn, schemaName, entryID, attributes);
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

    private PreparedStatement getSetAttributesSQLUpdateStatement(Connection conn, String schemaName, int entryID, Attribute[] attributes)
        throws DBException, SQLException {

        PreparedStatement p_stat;

        /* create the sql command */

        String settings = "";
        for (int i = 0; i < attributes.length; i++) {
            settings += (i==0 ? "" : ", ") + validateColumnName(attributes[i].getName()) + " = ?";
        }

        String sql = "UPDATE " + validateSchemaName(schemaName) + " SET " + 
            settings + " WHERE entry_id = ?";

        /* prepare and fill the statement */

        p_stat = m_dbmanager.prepareStatement(conn, sql);

        int a = 1;
        for (int i = 0; i < attributes.length; i++) {
            p_stat.setString(a++, attributes[i].getValue());
        }
        p_stat.setInt(a++, entryID);

        return p_stat;
    }

    private PreparedStatement getSetAttributesSQLInsertStatement(Connection conn, String schemaName, int entryID, Attribute[] attributes)
        throws DBException, SQLException {
        
        PreparedStatement p_stat;

        /* create the sql command */

        String columns = "";
        String values = "";

        for (int i = 0; i < attributes.length; i++) {
            columns += (i==0 ? "" : ", ") + validateColumnName(attributes[i].getName());
            values += (i==0 ? "?" : ", ?");
        }

        String sql = "INSERT INTO " + validateSchemaName(schemaName) +
            " (" + columns  + ", entry_id) VALUES (" + values + ", ?)";

        /* prepare and fill the statement */

        p_stat = m_dbmanager.prepareStatement(conn, sql);

        int a = 1;
        for (int i = 0; i < attributes.length; i++) {
            p_stat.setString(a++, attributes[i].getValue());
        }
        p_stat.setInt(a++, entryID);

        return p_stat;
    }

    private String validateColumnName(final String s) throws IllegalArgumentException {
        if (!MySQLUtils.isValidColumnIdentifier(s)) {
            throw new IllegalArgumentException("Invalid Column Name: " + s);
        }
        return s;
    }

    private String validateSchemaName(final String s) throws IllegalArgumentException {
        if (!MySQLUtils.isValidTableIdentifier(s)) {
            throw new IllegalArgumentException("Invalid Schema Name: " + s);
        }
        return s;
    }

}
