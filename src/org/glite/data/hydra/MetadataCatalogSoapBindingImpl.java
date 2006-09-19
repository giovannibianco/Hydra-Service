/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra;

import org.apache.log4j.Logger;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.ExistsException;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;
import org.glite.data.catalog.service.StringPair;
import org.glite.data.hydra.MetadataCatalog;
import org.glite.data.common.helpers.ServiceImpl;


/**
 * Implementation of a standalone Metadata Catalog. It covers the functionality defined in both the
 * MetadataCatalog and MetadataSchema interfaces.
 */
public class MetadataCatalogSoapBindingImpl extends ServiceImpl implements MetadataCatalog {
    // Logger object
    private final static Logger m_log = Logger.getLogger(
            "org.glite.data.catalog.service.meta.MetadataCatalogSoapBindingImpl");

    // The Metadata Catalog implementation object
    private MetadataCatalogImpl m_catalog = null;

    public MetadataCatalogSoapBindingImpl() throws InternalException {
        try {
            m_catalog = new MetadataCatalogImpl();
            
            String implSchemaVersion = getSchemaVersion();
            String dbSchemaVersion = m_catalog.getSchemaVersion();
            if(implSchemaVersion == null 
                    || dbSchemaVersion == null 
                    || ! dbSchemaVersion.equals(implSchemaVersion)) {
                m_log.error("DB schema version (" + dbSchemaVersion
                        + ") does not match the implementation (" + implSchemaVersion
                        + ")");
                throw new InternalException("DB schema do no match the implementation.");
            }
        } catch (Exception e) {
            m_log.error("Error while starting up the catalog: ", e);
            throw new InternalException("Error while starting up the catalog.");
        }
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#createEntry(org.glite.data.catalog.service.StringPair[])
     */
    public void createEntry(StringPair[] entries)
        throws InvalidArgumentException, InternalException, ExistsException, NotExistsException, AuthorizationException {
        m_catalog.createEntry(entries);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataCatalog#removeEntry(java.lang.String[])
     */
    public void removeEntry(String[] items) throws AuthorizationException, NotExistsException, InternalException {
        m_catalog.removeEntry(items);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataBase#setAttributes(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void setAttributes(String item, Attribute[] attributes)
        throws AuthorizationException, NotExistsException, InvalidArgumentException, InternalException {
        m_catalog.setAttributes(item, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataBase#clearAttributes(java.lang.String, java.lang.String[])
     */
    public void clearAttributes(String item, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        m_catalog.clearAttributes(item, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataBase#getAttributes(java.lang.String, java.lang.String[])
     */
    public Attribute[] getAttributes(String item, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        return m_catalog.getAttributes(item, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataBase#listAttributes(java.lang.String)
     */
    public Attribute[] listAttributes(String item) throws AuthorizationException, NotExistsException, InternalException {
        // TODO Auto-generated method stub
        return m_catalog.listAttributes(item);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataBase#query(java.lang.String, java.lang.String, int, int)
     */
    public String[] query(String query, String type, int limit, int offset)
        throws InternalException, AuthorizationException, InvalidArgumentException {
        return m_catalog.query(query, type, limit, offset);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.fas.FASBase#setPermission(org.glite.data.catalog.service.PermissionEntry[])
     */
    public void setPermission(PermissionEntry[] permissions)
        throws InternalException, AuthorizationException, NotExistsException, InvalidArgumentException {
        m_catalog.setPermission(permissions);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.fas.FASBase#getPermission(java.lang.String[])
     */
    public PermissionEntry[] getPermission(String[] items)
        throws InternalException, AuthorizationException, NotExistsException {
        return m_catalog.getPermission(items);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.fas.FASBase#checkPermission(java.lang.String[], org.glite.data.catalog.service.Perm)
     */
    public void checkPermission(String[] items, Perm perm)
        throws AuthorizationException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#createSchema(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void createSchema(String schemaName, Attribute[] attributes)
        throws AuthorizationException, ExistsException, InvalidArgumentException, InternalException {
        m_catalog.createSchema(schemaName, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#addSchemaAttributes(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributes)
        throws AuthorizationException, NotExistsException, ExistsException, InvalidArgumentException, InternalException {
        m_catalog.addSchemaAttributes(schemaName, attributes);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws AuthorizationException, NotExistsException, InternalException {
        m_catalog.removeSchemaAttributes(schemaName, attributeNames);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#dropSchema(java.lang.String)
     */
    public void dropSchema(String schemaName) throws AuthorizationException, NotExistsException, InternalException {
        m_catalog.dropSchema(schemaName);
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#listSchemas()
     */
    public String[] listSchemas() throws AuthorizationException, InternalException {
        return m_catalog.listSchemas();
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.MetadataSchema#describeSchema(java.lang.String)
     */
    public Attribute[] describeSchema(String schemaName)
        throws AuthorizationException, NotExistsException, InternalException {
        return m_catalog.describeSchema(schemaName);
    }
}
