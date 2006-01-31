/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers.catalog;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.StringPair;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.hydra.GliteUtils;
import org.glite.data.hydra.helpers.CatalogHelper;
import org.glite.data.hydra.helpers.MetadataHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.ArrayList;


public class MySQLCatalogHelper extends CatalogHelper {
    // Logger object
    private final static Logger m_log = Logger.getLogger("org.glite.data.catalog.service.meta.mysql.MySQLCatalogHelper");

    /**
     * @param dbManager
     */
    public MySQLCatalogHelper(DBManager dbManager) {
        super(dbManager);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.CatalogHelper#addEntries(org.glite.data.catalog.service.StringPair[])
     */
    public void addEntries(StringPair[] entries, BasicPermission basicPermission)
        throws InternalException {
        m_log.debug("Entered addEntries.");

        // Check for valid permission and entries
        if(entries == null) {
            m_log.debug("No entries given... nothing to be added.");
            return;
        }
        if(basicPermission == null) {
            m_log.error("Cannot add entries as basic permission given was null.");
            return;
        }
         
        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat_entry = null;
        PreparedStatement p_stat_principal = null;
        PreparedStatement p_stat_group = null;

        // Prepare the SQL string for the entry
        String sqlEntry = "INSERT INTO t_entry (entry_name, owner_id, group_id," +
            " user_perm, group_perm, other_perm, schema_id, creation_time) " +
            " SELECT ?, P1.principal_id, P2.principal_id, ?, ?, ?, S.schema_id, ?" +
            " FROM t_principal P1, t_principal P2, t_schema S" +
            " WHERE P1.principal_name = ? AND P2.principal_name = ? AND S.schema_name = ?";

        // Prepare the SQL string for principals
        String sqlPrincipal = "INSERT IGNORE INTO t_principal (principal_name) VALUES (?)";

        // Prepare shared objects
        Timestamp creationTime = new Timestamp(System.currentTimeMillis());

        try {
            // Add all entries
            conn = m_dbmanager.getConnection(false);
            p_stat_entry = m_dbmanager.prepareStatement(conn, sqlEntry);
            p_stat_principal = m_dbmanager.prepareStatement(conn, sqlPrincipal);

            for (int i = 0; i < entries.length; i++) {
                String entry = entries[i].getString1();
                String schema = entries[i].getString2();

                // Add client principals (or at least try)
                p_stat_principal.setString(1, basicPermission.getUserName());
                p_stat_principal.addBatch();
                
                // Add group principals (or at least try)
                p_stat_principal.setString(1, basicPermission.getGroupName());
                p_stat_principal.addBatch();
                
                // Add the entry
                p_stat_entry.setString(1, entry);
                p_stat_entry.setInt(2, GliteUtils.convertPermToInt(basicPermission.getUserPerm()));
                p_stat_entry.setInt(3, GliteUtils.convertPermToInt(basicPermission.getGroupPerm()));
                p_stat_entry.setInt(4, GliteUtils.convertPermToInt(basicPermission.getOtherPerm()));
                p_stat_entry.setTimestamp(5, creationTime);
                p_stat_entry.setString(6, basicPermission.getUserName());
                p_stat_entry.setString(7, basicPermission.getGroupName());
                p_stat_entry.setString(8, schema);
                p_stat_entry.addBatch();
            }

            // Execute the batch operations
            p_stat_principal.executeBatch();
            p_stat_entry.executeBatch();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.error("Error while inserting entries. Exception was: ", sqle);
            m_dbmanager.rollbackRegardless(conn);	
            throw new InternalException("Error inserting new entries in the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat_entry);
            m_dbmanager.cleanupResources(p_stat_group);
            m_dbmanager.cleanupResources(p_stat_principal);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.CatalogHelper#removeEntries(java.lang.String[])
     */
    public void removeEntries(String[] entries) throws InternalException {
        m_log.debug("Entered removeEntries.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;

        // Prepare the SQL string
        String sql = "DELETE FROM t_entry WHERE entry_name = ?";

        try {
            // Remove the entries
            conn = m_dbmanager.getConnection(false);
            p_stat = m_dbmanager.prepareStatement(conn, sql);

            for (int i = 0; i < entries.length; i++) {
                p_stat.setString(1, entries[i]);
                p_stat.addBatch();
            }

            p_stat.executeBatch();

            // Commit the changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.error("Error while removing entries. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);	
            throw new InternalException("Error removing entries from the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.CatalogHelper#getEntries(java.lang.String)
     */
    public String[] getEntries(String entryPattern) throws InternalException {
        m_log.debug("Entered getEntries.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat = null;
        ResultSet rs = null;

        // Internal objects
        String pattern = "%s"; // default is to retrieve all entries
        ArrayList entries = new ArrayList();

        // Update pattern when one was provided
        if (entryPattern != null) {
            pattern = entryPattern;
        }

        // Prepare the SQL string
        String sql = "SELECT entry_name FROM t_entry WHERE entry_name LIKE ?";

        try {
            // Execute the query			
            conn = m_dbmanager.getConnection(true);
            p_stat = m_dbmanager.prepareStatement(conn, sql);
            p_stat.setString(1, pattern);

            rs = p_stat.executeQuery();

            // Parse the results
            while (rs.next()) {
                entries.add(rs.getString("entry_name"));
            }
        } catch (Exception sqle) {
            m_log.error("Error returning entries. Exception was: " + sqle);
            throw new InternalException("Error accessing entries in the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat);
            m_dbmanager.cleanupResources(conn);
            m_dbmanager.cleanupResources(rs);
        }

        // Convert and return the results
        return (String[]) entries.toArray(new String[] {  });
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.CatalogHelper#checkEntryValidity(java.lang.String)
     */
    public void checkEntryValidity(String entry) throws InvalidArgumentException, InternalException {
        m_log.debug("Entered checkEntryValidity.");

        //TODO: implement
    }
}
