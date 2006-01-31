/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra;

import org.glite.data.catalog.service.Perm;


public class GliteUtils {
    
    public static int convertPermToInt(Perm permission) {

        // Return no permissions if a null object was passed
        if(permission == null) {
            return 0;
        }
        
        // Else convert the object into an int value
        int permInt = 0;
        permInt += (1 * (permission.isPermission() ? 1 : 0));
        permInt += (2 * (permission.isRemove() ? 1 : 0));
        permInt += (4 * (permission.isRead() ? 1 : 0));
        permInt += (8 * (permission.isWrite() ? 1 : 0));
        permInt += (16 * (permission.isExecute() ? 1 : 0));
        permInt += (32 * (permission.isList() ? 1 : 0));
        permInt += (64 * (permission.isGetMetadata() ? 1 : 0));
        permInt += (128 * (permission.isSetMetadata() ? 1 : 0));

        return permInt;
    }

    public static Perm convertIntToPerm(int perm) {
        Perm tempPerm = new Perm();

        tempPerm.setPermission(((perm & 1) == 1) ? true : false);
        tempPerm.setRemove(((perm & 2) == 2) ? true : false);
        tempPerm.setRead(((perm & 4) == 4) ? true : false);
        tempPerm.setWrite(((perm & 8) == 8) ? true : false);
        tempPerm.setExecute(((perm & 16) == 16) ? true : false);
        tempPerm.setList(((perm & 32) == 32) ? true : false);
        tempPerm.setGetMetadata(((perm & 64) == 64) ? true : false);
        tempPerm.setSetMetadata(((perm & 128) == 128) ? true : false);

        return tempPerm;
    }
}
