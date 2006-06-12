/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import java.util.List;
import java.util.ArrayList;

import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;

import org.glite.data.common.helpers.DBManager;

public abstract class AuthorizationHelper extends MetadataHelper {

    public AuthorizationHelper(DBManager dbManager) {
        super(dbManager);
    }
    
    public static String getClientName() 
        throws InternalException {
        return null;
    }
        
    public static String getClientPrimaryGroup() 
        throws InternalException {
        return null;
    }

    public BasicPermission getDefaultBasicPermission()
        throws InternalException {
        return new BasicPermission();
    }

    public static List getAuthzAttributeList() {
        return new ArrayList();
    }

    public void setPermission(PermissionEntry[] permissions)
        throws InvalidArgumentException, InternalException {

    }

    public PermissionEntry[] getPermission(String[] items)
        throws AuthorizationException, InternalException {

        return new PermissionEntry[] { };
    }

    public void checkPermission(String[] entries, Perm patternPerm)
        throws AuthorizationException, NotExistsException, InternalException {

    }

    public void checkSchemaPermission(String schema, Perm patternPerm)
        throws AuthorizationException, NotExistsException, InternalException {

    }
    
}
