/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.common.helpers;


/**
 * Indictaes that som databse operation went wrong. Tjis exception SHOULD NOT
 * be thrown across the wire!
 */
public class DBException extends Exception {

    public DBException() {
        super();
    }

    public DBException(String e) {
        super(e);
    }

}
