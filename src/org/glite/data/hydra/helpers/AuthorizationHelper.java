/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;


public interface AuthorizationHelper {
    public String getClientName() throws InternalException;

    public String getClientPrimaryGroup() throws InternalException;

    public BasicPermission getDefaultBasicPermission()
        throws InternalException;

    public void setPermission(PermissionEntry[] permissions)
        throws InvalidArgumentException, InternalException;

    public PermissionEntry[] getPermission(String[] items)
        throws AuthorizationException, InternalException;

    public void checkPermission(String[] entries, Perm patternPerm)
        throws AuthorizationException, NotExistsException, InternalException;
}
