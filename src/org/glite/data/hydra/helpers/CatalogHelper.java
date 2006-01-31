/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.StringPair;

import org.glite.data.common.helpers.DBManager;

public abstract class CatalogHelper extends MetadataHelper {

    public CatalogHelper(DBManager dbManager) {
        super(dbManager);
    }

    public void checkEntryValidity(String entry) 
        throws InvalidArgumentException, InternalException {
    }

    public String[] getEntries(String pattern) 
        throws InternalException {
        return new String[] { };
    }

    public void addEntries(StringPair[] entries, BasicPermission basicPermission)
        throws InternalException {
    }

    public void removeEntries(String[] entries) 
        throws InternalException {
    }
}
