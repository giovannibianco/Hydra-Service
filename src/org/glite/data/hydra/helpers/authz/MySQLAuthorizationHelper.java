/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
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

import org.glite.security.SecurityContext;
import org.glite.security.util.DNHandler;
import org.glite.security.voms.VOMSAttribute;
import org.glite.security.voms.VOMSValidator;
import org.glite.security.voms.BasicVOMSTrustStore;
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
    
    // Service level authorization attributes
    public static String createVomsAttribute = null;
    public static String adminVomsAttribute  = null;

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

        // Set create attribute
        createVomsAttribute = config.get("create_voms_attribute");
        if((createVomsAttribute != null) && 
           ("".equals(createVomsAttribute) || "@CREATE_VOMS_ATTRIBUTE@".equals(createVomsAttribute))) {
            createVomsAttribute = null;
        }

        // Set admin attribute
        adminVomsAttribute = config.get("admin_voms_attribute");
        if((adminVomsAttribute != null) && 
           ("".equals(adminVomsAttribute) || "@ADMIN_VOMS_ATTRIBUTE@".equals(adminVomsAttribute))) {
            adminVomsAttribute = null;
        }
        
        // Set vomsdir location
        String vomsdir = config.get("vomsdir");
        if((vomsdir != null) && (! vomsdir.equals("@VOMSDIR@"))) {
            VOMSValidator.setTrustStore(new BasicVOMSTrustStore(vomsdir, 300000));
        }


        m_log.info("DEFAULTS: DN='" + defaultClientDN + "'::GROUP='" + defaultGroupName + "'::USERPERMISSIONS=" 
                + GliteUtils.convertPermToInt(defaultUserPerm) + "::GROUPPERMISSIONS=" + GliteUtils.convertPermToInt(defaultGroupPerm)
                + "::OTHERPERMISSIONS=" + GliteUtils.convertPermToInt(defaultOtherPerm)
                + "::CREATE=" + createVomsAttribute
                + "::ADMIN=" + adminVomsAttribute
                + "::vomsdir=" + vomsdir);
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
        String sqlACLScratch = "DELETE t_acl FROM t_acl, t_entry WHERE t_entry.entry_name = ? AND t_entry.entry_id = t_acl.entry_id";

        // Prepare the ACL insert sql string
        String sqlACL = "INSERT INTO t_acl (entry_id, principal_id, perm)" + 
            " SELECT E.entry_id, P.principal_id, ?" +
            " FROM t_entry E, t_principal P" + 
            " WHERE E.entry_name = ? AND P.principal_name = ?";

        try {
            conn = m_dbmanager.getConnection(false);
            p_stat_basic_perm = m_dbmanager.prepareStatement(conn, sqlBasicPerm);
            p_stat_principal = m_dbmanager.prepareStatement(conn, sqlPrincipal);
            p_stat_acl_scratch = m_dbmanager.prepareStatement(conn, sqlACLScratch);
            p_stat_acl = m_dbmanager.prepareStatement(conn, sqlACL);

            for (int i = 0; i < permissions.length; i++) {
                String entry = permissions[i].getItem();
                Permission permission = permissions[i].getPermission();
                

                // Add the user and group principals
                p_stat_principal.setString(1, permission.getUserName());
                p_stat_principal.addBatch();
                p_stat_principal.setString(1, permission.getGroupName());
                p_stat_principal.addBatch();
                
                m_log.debug("New Basic Permissions (entry: " + entry + 
                            "; owner: " + permission.getUserName() +
                            "/" + permission.getGroupName());

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
                ACLEntry[] acl = permission.getAcl();
                if (acl != null) {
                    for (int j = 0; j < acl.length; j++) {
                        m_log.debug("\tNew ACL (entry: " + entry + "; principal: " + acl[j].getPrincipal());

                        // Add the ACL principal
                        p_stat_principal.setString(1, acl[j].getPrincipal());
                        p_stat_principal.addBatch();

                        // Add the ACL entry
                        Perm perm = acl[j].getPrincipalPerm();
                        p_stat_acl.setInt(1, GliteUtils.convertPermToInt(perm));
                        p_stat_acl.setString(2, entry);
                        p_stat_acl.setString(3, acl[j].getPrincipal());
                        p_stat_acl.addBatch();
                    }
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
    	principalList.add(clientName); // add the client dn to the list       

        // admin can do anything
        if((adminVomsAttribute != null) && principalList.contains(adminVomsAttribute)) {
            m_log.info(clientName + " is authorized as admin");
            return;
        }

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
                    throw new NotExistsException("The entry could not be found in the database.");
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
                } else if (principalList.contains(groupPrincipal) && checkPermPattern(patternPerm, groupPerm)) {
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
    
    public void checkSchemaPermission(String schema, Perm patternPerm)
        throws AuthorizationException, InternalException {
        m_log.debug("Entered checkSchemaPermission.");
        
        // Internal representation of the client's attributes
        String clientName = MySQLAuthorizationHelper.getClientName();
        List principalList = MySQLAuthorizationHelper.getAuthzAttributeList();
        principalList.add(clientName); // add the client dn to the list
        
        // admin can do anything
        if((adminVomsAttribute != null) && principalList.contains(adminVomsAttribute)) {
            m_log.info(clientName + " is authorized as admin");
            return;
        }

        // TODO: implement these tests as permissions on schema objects
        if(patternPerm.isWrite()) {
            if((createVomsAttribute != null) && (!principalList.contains(createVomsAttribute))) {
                m_log.debug("client is not allowed to create a new entry in " + schema);
                throw new AuthorizationException("client is not allowed to create a new entry in " + schema);
            }
            else {
                return;
            }
        }
    }
    

    public static String getClientName() throws InternalException {
        m_log.debug("Entered getClientName.");

        // Set the client name to default
        String clientName = null;

        // Get the security context
        InitSecurityContext.init();
        SecurityContext sc = SecurityContext.getCurrentContext();

        // Update the client name if one is available
        clientName = DNHandler.getDN(sc.getClientName()).getX500();

        m_log.debug("Exiting getClientName: " + clientName);
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
        List authzAttributes = getAuthzAttributeList();

        // Set the group name
        try {
            groupName = (String) authzAttributes.get(0);
        } catch (NullPointerException e) {
            groupName = defaultGroupName;
        } catch (IndexOutOfBoundsException e) {
            groupName = defaultGroupName;
        }

        m_log.debug("Exiting getClientPrimaryGroup: " + groupName);
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
        String primaryGroup = MySQLAuthorizationHelper.getClientPrimaryGroup();

        basicPermission.setUserName(clientName);
        basicPermission.setGroupName(primaryGroup);
        basicPermission.setUserPerm(defaultUserPerm);
        basicPermission.setGroupPerm(defaultGroupPerm);
        basicPermission.setOtherPerm(defaultOtherPerm);

        return basicPermission;
    }

    public static List getAuthzAttributeList() {
        m_log.debug("Entered getAuthzAttributeList.");

        List clientAttributes = new ArrayList();

        try {
            SecurityContext sc = SecurityContext.getCurrentContext();
            
            List vomsCerts = sc.getVOMSValidator().validate().getVOMSAttributes();
            m_log.debug("getAuthzAttributeList voms cert number: " + vomsCerts.size());

            for (int i = 0; i < vomsCerts.size(); i++) {
                List vomsAttributes = ((VOMSAttribute) vomsCerts.get(i)).getFullyQualifiedAttributes();
                m_log.debug("getAuthzAttributeList VOMS Attributes: " + vomsAttributes);
                clientAttributes.addAll(vomsAttributes);
            }

            return clientAttributes;
        } catch (NullPointerException e) {
            return new ArrayList();
        } catch (java.lang.IllegalArgumentException e) {
            return new ArrayList();
        }
    
    }

    private static boolean checkPermPattern(Perm pattern, Perm perm) {
        //TODO: find a more clever way
        if (pattern.isPermission() && !perm.isPermission()) {
            return false;
        }
        
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

}
