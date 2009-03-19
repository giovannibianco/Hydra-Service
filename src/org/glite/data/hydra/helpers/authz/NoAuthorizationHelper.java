/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.hydra.helpers.authz;

import org.glite.data.catalog.service.AuthorizationException;
import org.glite.data.catalog.service.InternalException;
import org.glite.data.catalog.service.InvalidArgumentException;
import org.glite.data.catalog.service.NotExistsException;
import org.glite.data.catalog.service.Perm;
import org.glite.data.catalog.service.PermissionEntry;
import org.glite.data.common.helpers.DBManager;
import org.glite.data.hydra.helpers.AuthorizationHelper;

public class NoAuthorizationHelper extends AuthorizationHelper {

    public NoAuthorizationHelper(DBManager dbManager) {
        super(dbManager);
    }
    
    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#setPermission(org.glite.data.catalog.service.PermissionEntry[])
     */
    public void setPermission(PermissionEntry[] permissions)
        throws InvalidArgumentException, InternalException {
    }

    /* (non-Javadoc)
     * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#getPermission(java.lang.String[])
     */
    public PermissionEntry[] getPermission(String[] items)
        throws AuthorizationException, InternalException {
        return null;
    }

	/* (non-Javadoc)
	 * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#checkPermission(java.lang.String[], org.glite.data.catalog.service.Perm)
	 */
	public void checkPermission(String[] entries, Perm patternPerm) throws AuthorizationException, NotExistsException, InternalException {

	}

	/* (non-Javadoc)
	 * @see org.glite.data.catalog.service.meta.helpers.AuthorizationHelper#checkSchemaPermission(java.lang.String[], org.glite.data.catalog.service.Perm)
	 */
	public void checkSchemaPermission(String[] entries, Perm patternPerm) throws AuthorizationException, NotExistsException, InternalException {

	}
}
