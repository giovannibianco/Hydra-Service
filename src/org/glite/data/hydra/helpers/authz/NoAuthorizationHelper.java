/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers.authz;

import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.BasicPermission;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;
import org.glite.data.hydra.helpers.AuthorizationHelper;


public class NoAuthorizationHelper implements AuthorizationHelper {
    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getClientDN()
     */
    public String getClientName() throws InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getClientPrimaryGroup()
     */
    public String getClientPrimaryGroup() throws InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getBasicPermission()
     */
    public BasicPermission getDefaultBasicPermission()
        throws InternalException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#setPermission(org.glite.data.catalog.service.PermissionEntry[])
     */
    public void setPermission(PermissionEntry[] permissions)
        throws InvalidArgumentException, InternalException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getPermission(java.lang.String[])
     */
    public PermissionEntry[] getPermission(String[] items)
        throws AuthorizationException, InternalException {
        // TODO Auto-generated method stub
        return null;
    }

	/* (non-Javadoc)
	 * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#checkPermission(java.lang.String[], org.glite.data.catalog.service.Perm)
	 */
	public void checkPermission(String[] entries, Perm patternPerm) throws AuthorizationException, NotExistsException, InternalException {
		// TODO Auto-generated method stub
		
	}
}
