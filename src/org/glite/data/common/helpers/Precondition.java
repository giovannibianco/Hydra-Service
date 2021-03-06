/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

/*
* Authors: Gavin McCance <gavin.mccance@cern.ch>
* Version: $Id: Precondition.java,v 1.2 2007-11-08 10:41:29 szamsu Exp $
*/
package org.glite.data.common.helpers;

import java.util.Collection;


/**
 * Static precondition checks.
 */
public class Precondition {
    /**
     * Check for null values and empty arrays / collections
     *
     * @return true if not empty and not null
     */
    public static boolean notNullOrEmpty(Object o) {
        if (o == null) {
            return false;
        }

        if (o instanceof Collection) {
            if (((Collection) o).size() == 0) {
                return false;
            }
        }

        if (o.getClass().isArray()) {
            if (0 == ((Object[]) o).length) {
                return false;
            }
        }

        return true;
    }
}
