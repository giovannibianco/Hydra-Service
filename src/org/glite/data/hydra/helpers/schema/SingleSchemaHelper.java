/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra.helpers.schema;

import org.apache.axis.InternalException;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.ExistsException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.hydra.helpers.SchemaHelper;

public class SingleSchemaHelper extends SchemaHelper {

    public SingleSchemaHelper(DBManager dbManager) {
        super(dbManager);
    }
    
    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchema(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void addSchema(String schemaName, Attribute[] attributes)
        throws ExistsException, InvalidArgumentException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchema(java.lang.String)
     */
    public void removeSchema(String schemaName) throws NotExistsException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#removeSchemaAttributes(java.lang.String, java.lang.String[])
     */
    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws NotExistsException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemas(java.lang.String)
     */
    public String[] getSchemas(String schemaNnamePattern)
        throws InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaDescription(java.lang.String)
     */
    public Attribute[] getSchemaDescription(String schemaName)
        throws NotExistsException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#addSchemaAttributes(java.lang.String, org.glite.data.catalog.service.Attribute[])
     */
    public void addSchemaAttributes(String schemaName, Attribute[] attributeNames)
        throws ExistsException, InternalException, InvalidArgumentException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#checkSchemaNameValidity(java.lang.String)
     */
    public void checkSchemaNameValidity(String schemaName)
        throws InvalidArgumentException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#checkAttributesValidity(org.glite.data.catalog.service.Attribute[])
     */
    public void checkAttributesValidity(Attribute[] attributes)
        throws InvalidArgumentException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.SchemaHelper#getSchemaId(java.lang.String)
     */
    public int getSchemaId(String schemaName) throws org.glite.data.catalog.service.InternalException {
        // TODO Auto-generated method stub
        return 0;
    }
}
