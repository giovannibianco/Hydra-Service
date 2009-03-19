/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.ExistsException;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;
import org.glite.data.catalog.service.StringPair;
import org.glite.data.common.helpers.ConfigurationBean;
import org.glite.data.common.helpers.DBException;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.common.helpers.JNDIBeanFetcher;
import org.glite.data.hydra.helpers.AttributeHelper;
import org.glite.data.hydra.helpers.AuthorizationHelper;
import org.glite.data.hydra.helpers.CatalogHelper;
import org.glite.data.hydra.helpers.SchemaHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.naming.NamingException;


/**
 * Implementation class
 */
public class MetadataCatalogImpl {
    // Logger object
    private final static Logger m_log = Logger.getLogger(MetadataCatalogImpl.class);
    private static final String m_dbpool = "jdbc/hydra";

    // DB interaction objects
    private DBManager m_dbmanager = null;

    // Helper Objects
    private AttributeHelper m_attr_helper = null;
    private CatalogHelper m_cat_helper = null;
    private SchemaHelper m_schema_helper = null;
    private AuthorizationHelper m_authz_helper = null;

    /**
     * Class construtor
     */
    public MetadataCatalogImpl() {
        m_log.info("New MetadataCatalogImpl instance created");

        // Create a new DBManager to handle the database interaction 
        try {
            m_dbmanager = new DBManager(m_dbpool);
        } catch (DBException sqle) {
            m_log.error("Could not get a connection to DB pool '" + m_dbpool + "': ", sqle);
        }

        // Get service configuration
        ConfigurationBean config = null;

        try {
            config = (ConfigurationBean) JNDIBeanFetcher.fetchBean("config");
        } catch (NamingException e) {
            m_log.error("Error retrieving service configuration", e);
        }

        // Create a new AttributeHelper object according to configuration
        String attributeHelperClass = config.get("attribute_helper_class");

        try {
            m_attr_helper = (AttributeHelper) getHelperObject(attributeHelperClass);
        } catch (Exception e) {
            m_log.error("Error loading AttributeHelper. Class " + attributeHelperClass + " could not be loaded.");
        }

        // Create a new CatalogHelper object according to configuration
        String catalogHelperClass = config.get("catalog_helper_class");

        try {
            m_cat_helper = (CatalogHelper) getHelperObject(catalogHelperClass);
        } catch (Exception e) {
            m_log.error("Error loading service. Class " + catalogHelperClass + " could not be loaded.");
        }

        // Create a new SchemaHelper object according to configuration
        String schemaHelperClass = config.get("schema_helper_class");

        try {
            m_schema_helper = (SchemaHelper) getHelperObject(schemaHelperClass);
        } catch (Exception e) {
            m_log.error("Error loading service. Class " + schemaHelperClass + " could not be loaded.");
        }

        // Create a new AuthorizationHelper
        String authorizationHelperClass = config.get("authorization_helper_class");

        try {
            m_authz_helper = (AuthorizationHelper) getHelperObject(authorizationHelperClass);
        } catch (Exception e) {
            m_log.error("Error loading service. Class " + authorizationHelperClass + " could not be loaded.");
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#setAttributes(java.lang.String, org.glite.data.catalog.service.meta.Attribute[])
     */
    public void setAttributes(String entry, Attribute[] attributes)
        throws AuthorizationException, NotExistsException, InvalidArgumentException, InternalException {
        m_log.debug("Entered setAttributes.");

        // Check if client is allowed to set metadata for entry
        Perm patternPerm = GliteUtils.convertIntToPerm(128);
        m_authz_helper.checkPermission(new String[] { entry }, patternPerm);

        // Set the attributes
        m_attr_helper.setAttributes(entry, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#clearAttributes(java.lang.String, java.lang.String[])
     */
    public void clearAttributes(String entry, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered clearAttributes.");

        // Check if client is allowed to set metadata for entry
        Perm patternPerm = GliteUtils.convertIntToPerm(128);
        m_authz_helper.checkPermission(new String[] { entry }, patternPerm);

        // Clear the attributes
        m_attr_helper.clearAttributes(entry, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#getAttributes(java.lang.String, java.lang.String[])
     */
    public Attribute[] getAttributes(String entry, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered getAttributes.");

        // Check if client is allowed to get metadata for entry
        Perm patternPerm = GliteUtils.convertIntToPerm(64);
        m_authz_helper.checkPermission(new String[] { entry }, patternPerm);

        // Get the attributes
        return m_attr_helper.getAttributes(entry, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#listAttributes(java.lang.String)
     */
    public Attribute[] listAttributes(String entry)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered listAttributes.");

        // Check if client is allowed to get metadata for entry
        Perm patternPerm = GliteUtils.convertIntToPerm(64);
        m_authz_helper.checkPermission(new String[] { entry }, patternPerm);

        // List the attributes
        return m_attr_helper.getAttributes(entry, null);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#createEntry(java.lang.String[], java.lang.String[])
     */
    public void createEntry(StringPair[] entries)
        throws InvalidArgumentException, InternalException, ExistsException, NotExistsException, AuthorizationException {
        m_log.debug("Entered createEntry.");

        Perm writePerm = GliteUtils.convertIntToPerm(8);
           
        // Check if given parameters are valid
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i].getString1();
            String schemaName = entries[i].getString2();

            // Check entry name validity
            m_cat_helper.checkEntryValidity(entry);

            // Check schema name validity
            m_schema_helper.checkSchemaNameValidity(schemaName);

            // Check if client is allowed to create a new entry
            m_authz_helper.checkSchemaPermission(schemaName, writePerm);

            // Check if entry already exists in catalog
            String[] matchEntries = m_cat_helper.getEntries(entry);

            if (matchEntries.length != 0) {
                m_log.error("There is already an entry with the given name: " + entry + ".");
                throw new ExistsException("There is already an entry with the given name: " + entry + ".");
            }

            // Check if schema exists in catalog
            int schemaId = m_schema_helper.getSchemaId(schemaName);

            if (schemaId == 0) {
                m_log.error("Schema not found in the catalog: " + schemaName);
                throw new NotExistsException("Schema not found in the catalog: " + schemaName);
            }
        }

        // Get default BasicPermission for client
        BasicPermission defaultPermission = m_authz_helper.getDefaultBasicPermission();

        // Add the new entries
        m_cat_helper.addEntries(entries, defaultPermission);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#removeEntry(java.lang.String[])
     */
    public void removeEntry(String[] entries) throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered removeEntry.");

        // Check if client is allowed to get metadata for entry
        Perm patternPerm = GliteUtils.convertIntToPerm(2);
        m_authz_helper.checkPermission(entries, patternPerm);

        // Remove the entries
        m_cat_helper.removeEntries(entries);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#createSchema(java.lang.String, org.glite.data.catalog.service.meta.Attribute[])
     */
    public void createSchema(String schemaName, Attribute[] attributes) throws InternalException {
        m_log.debug("createSchema not implemented in Hydra Service.");
        throw new InternalException("createSchema not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#addSchemaAttributes(java.lang.String, org.glite.data.catalog.service.meta.Attribute[])
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributes) throws InternalException {
        m_log.debug("addSchemaAttributes not implemented in Hydra Service.");
        throw new InternalException("addSchemaAttributes not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames) throws InternalException {
        m_log.debug("removeSchemaAttributes not implemented in Hydra Service.");
        throw new InternalException("removeSchemaAttributes not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#deleteSchema(java.lang.String)
     */
    public void dropSchema(String schemaName) throws InternalException {
        m_log.debug("dropSchema not implemented in Hydra Service.");
        throw new InternalException("dropSchema not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#listSchemas()
     */
    public String[] listSchemas() throws InternalException {
        m_log.debug("listSchemas not implemented in Hydra Service.");
        throw new InternalException("listSchemas not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#describeSchema(java.lang.String)
     */
    public Attribute[] describeSchema(String schemaName) throws InternalException {
        m_log.debug("describeSchema not implemented in Hydra Service.");
        throw new InternalException("describeSchema not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#query(java.lang.String[], java.lang.String)
     */
    public String[] query(String query, String type, int limit, int offset) throws InternalException {
        m_log.debug("query not implemented in Hydra Service.");
        throw new InternalException("query not implemented in Hydra Service.");
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#setPermissions(org.glite.data.catalog.service.PermissionEntry[])
     */
    public void setPermission(PermissionEntry[] permissions)
        throws InternalException, AuthorizationException, NotExistsException, InvalidArgumentException {
        // Get the entries
        String[] entries = new String[permissions.length];

        for (int i = 0; i < permissions.length; i++) {
            entries[i] = permissions[i].getItem();
        }

        // Check if user is allowed to change permissions for entries
        Perm patternPerm = GliteUtils.convertIntToPerm(1);
        m_authz_helper.checkPermission(entries, patternPerm);
        
        // Set the permissions
        m_authz_helper.setPermission(permissions);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#getPermissions(java.lang.String[])
     */
    public PermissionEntry[] getPermission(String[] items)
        throws InternalException, AuthorizationException, NotExistsException {
        // Check if user is allowed to get permissions for items
        Perm patternPerm = GliteUtils.convertIntToPerm(32);
        m_authz_helper.checkPermission(items, patternPerm);

        // Get the permissions
        return m_authz_helper.getPermission(items);
    }

    // Private Methods
    private Object getHelperObject(String helperClassName)
        throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, 
            InstantiationException, IllegalAccessException, InvocationTargetException {
        m_log.debug("Entered getHelperObject.");

        // Get the class references for the helper and DBManager objects
        Class storageClass = Class.forName(helperClassName);
        Class[] types = { DBManager.class };

        // Get a reference to the helper object constructor
        Constructor constructor = storageClass.getConstructor(types);

        // Create the constructor parameters array
        Object[] params = { m_dbmanager };

        // Create a new helper object instance and return
        return constructor.newInstance(params);
    }
    
    public String getSchemaVersion() throws InternalException {
        return m_cat_helper.getSchemaVersion();
    }
}
