/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.ExistsException;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;

import org.glite.data.common.helpers.DBManager;

public abstract class SchemaHelper extends MetadataHelper {

    public SchemaHelper(DBManager dbManager) {
        super(dbManager);
    }
    
    public void addSchema(String schemaName, Attribute[] attributes)
        throws ExistsException, InvalidArgumentException, InternalException {

    }

    public void removeSchema(String schemaName) 
        throws NotExistsException, InternalException {

    }

    public void addSchemaAttributes(String schemaName, Attribute[] attributeNames)
        throws ExistsException, InternalException, InvalidArgumentException {
    
    }

    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws NotExistsException, InternalException {

    }

    public String[] getSchemas(String schemaNamePattern)
        throws InternalException {
        return new String[] { };
    }

    public int getSchemaId(String schemaName) 
        throws InternalException {
        return -1;
    }

    public Attribute[] getSchemaDescription(String schemaName)
        throws NotExistsException, InternalException {
        return new Attribute[] { };
    }

    public void checkSchemaNameValidity(String schemaName)
        throws InvalidArgumentException, InternalException {

    }

    public void checkAttributesValidity(Attribute[] attributes)
        throws InvalidArgumentException, InternalException {

    }
}
