/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.common.helpers;


/**
 * Indicates paramater not found in servlet request. com.oreilly again..
 */
public class ParamaterNotFoundException extends Exception {
    public ParamaterNotFoundException() {
        super();
    }

    public ParamaterNotFoundException(String message) {
        super(message);
    }
}
