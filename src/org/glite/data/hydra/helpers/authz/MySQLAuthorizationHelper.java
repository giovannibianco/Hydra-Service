/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers.authz;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.ACLEntry;
import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.Permission;
import org.glite.data.catalog.service.PermissionEntry;
import org.glite.data.common.helpers.ConfigurationBean;
import org.glite.data.common.helpers.DBException;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.common.helpers.JNDIBeanFetcher;
import org.glite.data.hydra.GliteUtils;
import org.glite.data.hydra.helpers.AuthorizationHelper;
import org.glite.data.hydra.helpers.MetadataHelper;

import org.glite.security.SecurityContext;
import org.glite.security.util.axis.InitSecurityContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

public class MySQLAuthorizationHelper extends AuthorizationHelper {
    // Logger object
    private final static Logger m_log = Logger.getLogger(
            "org.glite.data.catalog.service.meta.helpers.MySQLAuthorizationHelper");

    public static final String DEFAULT_CLIENT_DN = "default-dn";
    public static final String DEFAULT_GROUP_NAME = "default-group";
    public static final int DEFAULT_USER_PERM = 207;
    public static final int DEFAULT_GROUP_PERM = 0;
    public static final int DEFAULT_OTHER_PERM = 0;
        
    // Default client dn objects
    public static String defaultClientDN = null;
    public static String defaultGroupName = null;

    // Default Permissions objects
    public static Perm defaultUserPerm = null;
    public static Perm defaultGroupPerm = null;
    public static Perm defaultOtherPerm = null;

    /**
    * @param dbManager
    */
    public MySQLAuthorizationHelper(DBManager dbManager) {
        super(dbManager);

        // Load service configuration object
        ConfigurationBean config = null;
        try {
            config = (ConfigurationBean) JNDIBeanFetcher.fetchBean("config");
        } catch (NamingException e) {
            m_log.error("Error retrieving service configuration", e);
        }

        // Set default user permissions
        String userPerm = config.get("default_user_permissions");
        if(userPerm != null) {
            defaultUserPerm = GliteUtils.convertIntToPerm(Integer.parseInt(userPerm));
        } else {
            defaultUserPerm = GliteUtils.convertIntToPerm(DEFAULT_USER_PERM);
        }

        // Set default group permissions
        String groupPerm = config.get("default_group_permissions");
        if(groupPerm != null) {
            defaultGroupPerm = GliteUtils.convertIntToPerm(Integer.parseInt(groupPerm));
        } else {
            defaultGroupPerm = GliteUtils.convertIntToPerm(DEFAULT_GROUP_PERM);
        }

        // Set default other permissions
        String otherPerm = config.get("default_other_permissions");
        if(otherPerm != null) {
            defaultOtherPerm = GliteUtils.convertIntToPerm(Integer.parseInt(otherPerm));
        } else {
            defaultOtherPerm = GliteUtils.convertIntToPerm(DEFAULT_OTHER_PERM);
        }

        // Set default client DN
        String defaultDN = config.get("default_dn");
        if(defaultDN != null) {
            defaultClientDN = defaultDN;
        } else {
            defaultClientDN = DEFAULT_CLIENT_DN;
        }

        // Set default group name
        String defaultGroup = config.get("default_group");
        if(defaultGroup != null) {
            defaultGroupName = defaultGroup;
        } else {
            defaultGroupName = DEFAULT_GROUP_NAME;
        }

        m_log.info("DEFAULTS: DN='" + defaultClientDN + "'::GROUP='" + defaultGroupName + "'::USERPERMISSIONS=" 
                + GliteUtils.convertPermToInt(defaultUserPerm) + "::GROUPPERMISSIONS=" + GliteUtils.convertPermToInt(defaultGroupPerm)
                + "::OTHERPERMISSIONS=" + GliteUtils.convertPermToInt(defaultOtherPerm));
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#setPermission(org.glite.data.catalog.service.PermissionEntry[])
     * TODO: check for duplicate entries in the acl (get error from mysql)
     */
    public void setPermission(PermissionEntry[] permissions)
        throws InvalidArgumentException, InternalException {
        m_log.debug("Entered setPermission.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat_basic_perm = null;
        PreparedStatement p_stat_principal = null;
        PreparedStatement p_stat_acl_scratch = null;
        PreparedStatement p_stat_acl = null;

        // Prepare the BasicPermissions update sql string
        String sqlBasicPerm = "UPDATE t_entry E, t_principal P1, t_principal P2 " +
            "SET E.owner_id = P1.principal_id, E.group_id = P2.principal_id, E.user_perm = ?, E.group_perm = ?, E.other_perm = ? " +
            "WHERE E.entry_name = ? AND P1.principal_name = ? AND P2.principal_name = ?";

        // Prepare the principal sql strings
        String sqlPrincipal = "INSERT IGNORE INTO t_principal (principal_name) VALUES (?)";
        
        // Prepare the ACL scratch sql string
        String sqlACLScratch = "DELETE t_acl FROM t_acl, t_entry WHERE t_entry.entry_name = ?";

        // Prepare the ACL insert sql string
        String sqlACL = "INSERT INTO t_acl (entry_id, principal_id, perm)" + " SELECT E.entry_id, P.principal_id, ?" +
            " FROM t_entry E, t_principal P" + " WHERE E.entry_name = ? AND P.principal_name = ?";

        try {
            conn = m_dbmanager.getConnection(false);
            p_stat_basic_perm = m_dbmanager.prepareStatement(conn, sqlBasicPerm);
            p_stat_principal = m_dbmanager.prepareStatement(conn, sqlPrincipal);
            p_stat_acl_scratch = m_dbmanager.prepareStatement(conn, sqlACLScratch);
            p_stat_acl = m_dbmanager.prepareStatement(conn, sqlACL);

            for (int i = 0; i < permissions.length; i++) {
                String entry = permissions[i].getItem();
                Permission permission = permissions[i].getPermission();
                ACLEntry[] acl = permission.getAcl();

                // Add the principal (or at least try)
                p_stat_principal.setString(1, acl[i].getPrincipal());
                p_stat_principal.addBatch();

                m_log.debug("New Basic Permissions (entry: " + entry + "; owner: " + permission.getUserName());

                // Update basic permissions for entry
                p_stat_basic_perm.setInt(1, GliteUtils.convertPermToInt(permission.getUserPerm()));
                p_stat_basic_perm.setInt(2, GliteUtils.convertPermToInt(permission.getGroupPerm()));
                p_stat_basic_perm.setInt(3, GliteUtils.convertPermToInt(permission.getOtherPerm()));
                p_stat_basic_perm.setString(4, entry);
                p_stat_basic_perm.setString(5, permission.getUserName());
                p_stat_basic_perm.setString(6, permission.getGroupName());
                p_stat_basic_perm.addBatch();

                // Scratch ACL for entry
                p_stat_acl_scratch.setString(1, entry);
                p_stat_acl_scratch.addBatch();

                // Add new entries in ACL
                for (int j = 0; i < acl.length; i++) {
                    m_log.debug("\tNew ACL (entry: " + entry + "; principal: " + acl[i].getPrincipal());

                    // Add the acl entry
                    Perm perm = acl[i].getPrincipalPerm();
                    p_stat_acl.setInt(1, GliteUtils.convertPermToInt(perm));
                    p_stat_acl.setString(2, entry);
                    p_stat_acl.setString(3, acl[i].getPrincipal());
                    p_stat_acl.addBatch();
                }
            }

            // Execute changes
            p_stat_principal.executeBatch();
            p_stat_basic_perm.executeBatch();
            p_stat_acl_scratch.executeBatch();
            p_stat_acl.executeBatch();

            // Commit changes
            m_dbmanager.commit(conn);
        } catch (Exception sqle) {
            m_log.error("Error setting permissions of entries. Exception was: " + sqle);
            m_dbmanager.rollbackRegardless(conn);            
            throw new InternalException("Error setting permission of entries in the database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat_basic_perm);
            m_dbmanager.cleanupResources(p_stat_acl);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getPermission(java.lang.String[])
     */
    public PermissionEntry[] getPermission(String[] items)
        throws AuthorizationException, InternalException {
        m_log.debug("Entered getPermission.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat_basic_perm = null;
        PreparedStatement p_stat_acl = null;
        ResultSet rsbp = null;
        ResultSet rsAcl = null;

        // Internal objects
        PermissionEntry[] permEntries = new PermissionEntry[items.length];

        // Prepare the BasicPermissions sql string
        String sqlBasicPerms = "SELECT entry_id, P1.principal_name AS owner_name, P2.principal_name AS group_name, user_perm, group_perm, other_perm " +
            " FROM t_entry E, t_principal P1, t_principal P2" +
            " WHERE entry_name = ? AND P1.principal_id = E.owner_id AND P2.principal_id = E.group_id";

        // Prepare the acls sql string
        String sqlAcl = "SELECT principal_name, perm FROM t_acl, t_principal" +
            " WHERE entry_id = ? AND t_acl.principal_id = t_principal.principal_id";

        try {
            conn = m_dbmanager.getConnection(false);
            p_stat_basic_perm = m_dbmanager.prepareStatement(conn, sqlBasicPerms);
            p_stat_acl = m_dbmanager.prepareStatement(conn, sqlAcl);

            // Iterate through the items
            for (int i = 0; i < items.length; i++) {
                Permission permission = new Permission();
                ArrayList acl = new ArrayList();

                // Get the basic permissions
                p_stat_basic_perm.setString(1, items[i]);
                rsbp = p_stat_basic_perm.executeQuery();

                if (rsbp.next()) {
                    permission.setUserName(rsbp.getString("owner_name"));
                    permission.setGroupName(rsbp.getString("group_name"));
                    permission.setUserPerm(GliteUtils.convertIntToPerm(rsbp.getInt("user_perm")));
                    permission.setGroupPerm(GliteUtils.convertIntToPerm(rsbp.getInt("group_perm")));
                    permission.setOtherPerm(GliteUtils.convertIntToPerm(rsbp.getInt("other_perm")));
                } else {
                    throw new NotExistsException("Entry '" + items[i] + "' was not found in the catalog.");
                }

                // Get the acl
                p_stat_acl.setInt(1, rsbp.getInt("entry_id"));
                rsAcl = p_stat_acl.executeQuery();

                while (rsAcl.next()) {
                    ACLEntry aclEntry = new ACLEntry();

                    // Build the perm object
                    Perm perm = GliteUtils.convertIntToPerm(rsAcl.getInt("perm"));

                    // Build the acl entry object
                    aclEntry.setPrincipal(rsAcl.getString("principal_name"));
                    aclEntry.setPrincipalPerm(perm);

                    // Add to the list
                    acl.add(aclEntry);
                }

                // Add to permissions
                permission.setAcl((ACLEntry[]) acl.toArray(new ACLEntry[] {  }));

                PermissionEntry permEntry = new PermissionEntry();
                permEntry.setItem(items[i]);
                permEntry.setPermission(permission);
                permEntries[i] = permEntry;
            }
        } catch (Exception sqle) {
            m_log.error("Error getting permissions of entries. Exception was: " + sqle);
            throw new InternalException("Error getting permission of entries from database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat_basic_perm);
            m_dbmanager.cleanupResources(p_stat_acl);
            m_dbmanager.cleanupResources(rsbp);
            m_dbmanager.cleanupResources(rsAcl);
            m_dbmanager.cleanupResources(conn);
        }

        return permEntries;
    }

    /* (non-Javadoc)
    * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#checkPermission()
    */
    public void checkPermission(String[] entries, Perm patternPerm)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered checkPermission.");

        // Database Objects
        Connection conn = null;
        PreparedStatement p_stat_basic_perm = null;
        PreparedStatement p_stat_acl = null;
        ResultSet rsbp = null;
        ResultSet rsAcl = null;

        // Internal Objects
        String clientName = MySQLAuthorizationHelper.getClientName();
        List principalList = MySQLAuthorizationHelper.getAuthzAttributeList();
        if(principalList == null) {
        	principalList = new ArrayList();
        }
    	principalList.add(clientName); // add the client dn to the list        	

        // Prepare the basic permissions sql string
        String sqlBasicPerm = "SELECT P1.principal_name AS owner_name, P2.principal_name AS group_name, E.user_perm, E.group_perm, E.other_perm" +
            " FROM t_entry E, t_principal P1, t_principal P2" +
            " WHERE E.entry_name = ? AND P1.principal_id = E.owner_id AND P2.principal_id = E.group_id";

        // Prepare the ACL sql string
        String principalListString = "(";
        for(int i=0; i < principalList.size()-1; i++) {
        	principalListString += "'" + principalList.get(i) + "', ";
        }
        principalListString += "'" + principalList.get(principalList.size()-1) + "')";
        String sqlACL = "SELECT A.perm FROM t_acl A, t_entry E, t_principal P" +
            " WHERE E.entry_name = ? AND P.principal_name IN " + principalListString +
			" AND E.entry_id = A.entry_id AND P.principal_id = A.principal_id";

        // Check permissions
        try {
            conn = m_dbmanager.getConnection(false);
            p_stat_basic_perm = m_dbmanager.prepareStatement(conn, sqlBasicPerm);
            p_stat_acl = m_dbmanager.prepareStatement(conn, sqlACL);

            // Iterate through entries
            for (int i = 0; i < entries.length; i++) {
                // Get the basic permissions for the entry
                p_stat_basic_perm.setString(1, entries[i]);
                rsbp = p_stat_basic_perm.executeQuery();

                if (!rsbp.next()) {
                    m_log.error("The entry could not be found in the database (but it should have).");
                    throw new NotExistsException("The entry could not be found in the database."); //TODO: check
                }

                m_log.debug("checking basic permissions.");
                // Do the check for the basic permissions
                String ownerPrincipal = rsbp.getString("owner_name");
                String groupPrincipal = rsbp.getString("group_name");
                Perm userPerm = GliteUtils.convertIntToPerm(rsbp.getInt("user_perm"));
                Perm groupPerm = GliteUtils.convertIntToPerm(rsbp.getInt("group_perm"));
                Perm otherPerm = GliteUtils.convertIntToPerm(rsbp.getInt("other_perm"));

                boolean basicPermission = false;

                if (clientName.equals(ownerPrincipal) && checkPermPattern(patternPerm, userPerm)) {
                    basicPermission = true;
                } else if (clientExposesAttribute(groupPrincipal) && checkPermPattern(patternPerm, groupPerm)) {
                    basicPermission = true;
                } else if (checkPermPattern(patternPerm, otherPerm)) {
                    basicPermission = true;
                }

                // Jump to next entry if basic permissions were ok for client
                if (basicPermission) {
                    continue;
                }

                m_log.debug("checking acl...");
                // Now go check the ACL (the principal list contains the client dn plus all the voms attributes)
               	p_stat_acl.setString(1, entries[i]);
               	rsAcl = p_stat_acl.executeQuery();
                	
               	while(rsAcl.next()) {
               		Perm aclPerm = GliteUtils.convertIntToPerm(rsAcl.getInt("perm"));
               		if (checkPermPattern(patternPerm, aclPerm)) {
               			m_log.debug("ok from acl...");
               			return; // Give permission if acl is good
               		}
               	}
                m_log.debug("unauthorized...");
                // If we got here no permission should be granted
                m_log.debug("Cannot perform action for entry '" + entries[i] + "'.");
                throw new AuthorizationException("Cannot perform action for entry '" + entries[i] + "'.");
            }
        } catch (SQLException sqle) {
            m_log.error("Error getting permissions of entries. Exception was: " + sqle);
            throw new InternalException("Error getting permission of entries from database.");
        } catch (DBException sqle) {
            m_log.error("Error getting permissions of entries. Exception was: " + sqle);
            throw new InternalException("Error getting permission of entries from database.");
        } finally {
            m_dbmanager.cleanupResources(p_stat_basic_perm);
            m_dbmanager.cleanupResources(p_stat_acl);
            m_dbmanager.cleanupResources(rsbp);
            m_dbmanager.cleanupResources(rsAcl);
            m_dbmanager.cleanupResources(conn);
        }
    }

    /* (non-Javadoc)
    * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getClientDN()
    */
    public static String getClientName() throws InternalException {
        m_log.debug("Entered getClientName.");

        // Set the client name to default
        String clientName = null;

        // Get the security context
        SecurityContext sc = getSecurityContext();

        // Update the client name if one is available
        clientName = sc.getClientName();

        if (clientName != null) {
            return clientName;
        } else {
            return defaultClientDN;
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getClientGroup()
     */
    public static String getClientPrimaryGroup() throws InternalException {
        m_log.debug("Entered getClientPrimaryGroup.");

        String groupName = null;

        // Get list of attributes from the security context
        SecurityContext sc = getSecurityContext();
        List authzAttributes = sc.getAuthorizedAttributes();

        // Set the group name
        if ((authzAttributes != null) && (authzAttributes.get(0) != null)) {
            groupName = (String) authzAttributes.get(0);
        } else {
            groupName = defaultGroupName;
        }

        return groupName;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getBasicPermission()
     */
    public BasicPermission getDefaultBasicPermission()
        throws InternalException {
        m_log.debug("Entered getBasicPermission.");

        // Create a new BasicPermission for current client
        BasicPermission basicPermission = new BasicPermission();
        String clientName = MySQLAuthorizationHelper.getClientName();

        basicPermission.setUserName(clientName);
        basicPermission.setGroupName(MySQLAuthorizationHelper.getClientPrimaryGroup());
        basicPermission.setUserPerm(defaultUserPerm);
        basicPermission.setGroupPerm(defaultGroupPerm);
        basicPermission.setOtherPerm(defaultOtherPerm);

        return basicPermission;
    }

    public static List getAuthzAttributeList() {

    	// Get list of attributes from the security context
        SecurityContext sc = getSecurityContext();
        return sc.getAuthorizedAttributes();
        
    }

    private static SecurityContext getSecurityContext() {
        InitSecurityContext.init();

        return SecurityContext.getCurrentContext();
    }

    private static boolean checkPermPattern(Perm pattern, Perm perm) {
        //TODO: find a more clever way
        if (pattern.isRead() && !perm.isRead()) {
            return false;
        }

        if (pattern.isWrite() && !perm.isWrite()) {
            return false;
        }

        if (pattern.isExecute() && !perm.isExecute()) {
            return false;
        }

        if (pattern.isList() && !perm.isList()) {
            return false;
        }

        if (pattern.isRemove() && !perm.isRemove()) {
            return false;
        }

        if (pattern.isSetMetadata() && !perm.isSetMetadata()) {
            return false;
        }

        if (pattern.isGetMetadata() && !perm.isGetMetadata()) {
            return false;
        }

        return true;
    }

    private static boolean clientExposesAttribute(String attribute) {
        // Get list of attributes from the security context
        SecurityContext sc = getSecurityContext();
        List authzAttributes = sc.getAuthorizedAttributes();

        // Check if attribute given is in the list
        if (authzAttributes != null) {
            return authzAttributes.contains(attribute);
        } else {
            return false;
        }
    }
    
}
