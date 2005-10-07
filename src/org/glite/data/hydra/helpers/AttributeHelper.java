/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.catalog.service.Attribute;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.NotExistsException;


public interface AttributeHelper {
    public Attribute[] getAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException;

    public void setAttributes(String entry, Attribute[] attributes)
        throws NotExistsException, InternalException;

    public void clearAttributes(String entry, String[] attributeNames)
        throws NotExistsException, InternalException;
}
