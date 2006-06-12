/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
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
    private final static Logger m_log = Logger.getLogger("org.glite.data.catalog.service.meta.MetadataCatalogImpl");
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
     * TODO: remove the internal stuff
     */
    public void createEntry(StringPair[] entries)
        throws InvalidArgumentException, InternalException, ExistsException, NotExistsException, AuthorizationException {
        m_log.debug("Entered createEntry.");

        // Check if client is allowed to create a new entry
        // TODO: implement        
        // Internal objects
        int[] schemaIds = new int[entries.length];
        String[] entryNames = new String[entries.length];

        // Check if given parameters are valid
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i].getString1();
            String schemaName = entries[i].getString2();

            // Check entry name validity
            m_cat_helper.checkEntryValidity(entry);

            // Check schema name validity
            m_schema_helper.checkSchemaNameValidity(schemaName);

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

            // Fill internal objects
            //entryNames[i] = entry;
            //schemaIds[i] = schemaId;
            //m_log.debug("entryName: " + entry + "; schemaId: " + schemaId);
        }

        // Get default BasicPermission for client
        BasicPermission defaultPermission = m_authz_helper.getDefaultBasicPermission();

        // Add the new entries
        //m_cat_helper.addEntries(entryNames, schemaIds, defaultPermission);
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

        // Check if given entries exist in the database
        // TODO: this is done during the check of permissions
        /**for (int i = 0; i < entries.length; i++) {
            String[] matchEntries = m_cat_helper.getEntries(entries[i]);

            if (matchEntries.length == 0) {
                m_log.error("Entry not found in the catalog: " + entries[i]);
                throw new NotExistsException("Entry not found in the catalog: " + entries[i]);
            }
        }*/
        // Remove the entries
        m_cat_helper.removeEntries(entries);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#createSchema(java.lang.String, org.glite.data.catalog.service.meta.Attribute[])
     */
    public void createSchema(String schemaName, Attribute[] attributes)
        throws AuthorizationException, ExistsException, InvalidArgumentException, InternalException {
        m_log.debug("Entered createSchema.");

        // Check if client is allowed to create a new schema
        // TODO: implement
        // Check if given parameters (schema name, attribute name and type) are valid
        m_schema_helper.checkSchemaNameValidity(schemaName);
        m_schema_helper.checkAttributesValidity(attributes);

        // Check if schema exists
        String[] schemas = m_schema_helper.getSchemas(schemaName);

        if (schemas.length != 0) {
            m_log.error("There is already one schema with the given name: '" + schemaName + "'");
            throw new ExistsException("There is already one schema with the given name: '" + schemaName + "'");
        }

        // Add the new schema
        m_schema_helper.addSchema(schemaName, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#addSchemaAttributes(java.lang.String, org.glite.data.catalog.service.meta.Attribute[])
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributes)
        throws AuthorizationException, NotExistsException, ExistsException, InvalidArgumentException, InternalException {
        m_log.debug("Entered addSchemaAttributes.");

        // Check if client is allowed to change the schema
        //TODO: implement
        // Check if schema exists
        String[] schemas = m_schema_helper.getSchemas(schemaName);

        if (schemas.length == 0) {
            m_log.error("There is no schema with the given name: '" + schemaName + "'");
            throw new NotExistsException("There is no schema with the given name: '" + schemaName + "'");
        }

        // Check if attributes are valid
        m_schema_helper.checkAttributesValidity(attributes);

        // Add the new attributes to the schema
        m_schema_helper.addSchemaAttributes(schemaName, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered removeSchemaAttributes.");

        // Check if client is allowed to change the schema
        //TODO: implement
        // Check if schema exists
        String[] schemas = m_schema_helper.getSchemas(schemaName);

        if (schemas.length == 0) {
            m_log.error("There is no schema with the given name: '" + schemaName + "'");
            throw new NotExistsException("There is no schema with the given name: '" + schemaName + "'");
        }

        // Remove the attributes from the schema
        m_schema_helper.removeSchemaAttributes(schemaName, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#deleteSchema(java.lang.String)
     */
    public void dropSchema(String schemaName) throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered dropSchema.");

        // Check if client can remove the schema
        //TODO: implement
        // Check if schema exists
        String[] schemas = m_schema_helper.getSchemas(schemaName);

        if (schemas.length == 0) {
            m_log.error("There is no schema with the given name: '" + schemaName + "'");
            throw new NotExistsException("There is no with the given name: '" + schemaName + "'");
        }

        // Remove the schema
        m_schema_helper.removeSchema(schemaName);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#listSchemas()
     */
    public String[] listSchemas() throws AuthorizationException, InternalException {
        m_log.debug("Entered listSchemas.");

        // Check if the client can list the schemas
        //TODO: implement
        // Get the schemas list and return
        return m_schema_helper.getSchemas(null);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#describeSchema(java.lang.String)
     */
    public Attribute[] describeSchema(String schemaName)
        throws AuthorizationException, NotExistsException, InternalException {
        m_log.debug("Entered describeSchema.");

        // Check if the client can get the schema description
        //TODO: implement
        // Check if schema exists
        String[] schemas = m_schema_helper.getSchemas(schemaName);

        if (schemas.length == 0) {
            m_log.error("There is no schema with the given name: '" + schemaName + "'");
            throw new NotExistsException("There is no with the given name: '" + schemaName + "'");
        }

        // Get the schema description and return
        return m_schema_helper.getSchemaDescription(schemaName);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#query(java.lang.String[], java.lang.String)
     */
    public String[] query(String query, String type, int limit, int offset)
        throws InternalException, AuthorizationException, InvalidArgumentException {
        
        // Check for query type validity
        // TODO: implement

        // Check for query validity
        // TODO: implement
        
        // Check for limit/offset validity
        if((limit >= 0 && offset < 0) || (limit < 0 && offset >=0)) {
            throw new InvalidArgumentException("Invalid offset/limit pair given. Either give both or none.");
        }

        // Make the query
        return m_attr_helper.query(query, type, limit, offset);
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
}
