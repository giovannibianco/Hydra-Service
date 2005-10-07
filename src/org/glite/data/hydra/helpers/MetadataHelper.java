/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.common.helpers.DBManager;


public abstract class MetadataHelper {
    protected DBManager m_dbmanager = null;

    public MetadataHelper(DBManager dbManager) {
        this.m_dbmanager = dbManager;
    }
}
