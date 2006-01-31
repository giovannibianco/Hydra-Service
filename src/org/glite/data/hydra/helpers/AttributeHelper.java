/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.NotExistsException;

import org.glite.data.common.helpers.DBManager;

public abstract class AttributeHelper extends MetadataHelper {

    public AttributeHelper(DBManager dbManager) {
        super(dbManager);
    }
    
    public Attribute[] getAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException {
        return new Attribute[] { };
    }

    public void setAttributes(String entry, Attribute[] attributes)
        throws NotExistsException, InternalException {
    }

    public void clearAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException {
    }

    public String[] query(String query, String type, int limit, int offset)
        throws InternalException {
        return new String[] { };
    }
}
