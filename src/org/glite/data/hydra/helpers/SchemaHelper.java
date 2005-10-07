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


public interface SchemaHelper {
    public void addSchema(String schemaName, Attribute[] attributes)
        throws ExistsException, InvalidArgumentException, InternalException;

    public void removeSchema(String schemaName) throws NotExistsException, InternalException;

    public void addSchemaAttributes(String schemaName, Attribute[] attributeNames)
        throws ExistsException, InternalException, InvalidArgumentException;

    public void removeSchemaAttributes(String schemaName, String[] attributeNames)
        throws NotExistsException, InternalException;

    public String[] getSchemas(String schemaNamePattern)
        throws InternalException;

    public int getSchemaId(String schemaName) throws InternalException;

    public Attribute[] getSchemaDescription(String schemaName)
        throws NotExistsException, InternalException;

    public void checkSchemaNameValidity(String schemaName)
        throws InvalidArgumentException, InternalException;

    public void checkAttributesValidity(Attribute[] attributes)
        throws InvalidArgumentException, InternalException;
}
