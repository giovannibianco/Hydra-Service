/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;


/**
 * Base interface for configuration beans. The configuration update semantics
 * (i.e whether subsequent changes to the configuration are made live) is up
 * to the implementing bean.)
 */
public interface ConfigurationBean {
    /**
     * Get the configuration item for the given key.
     *
     * @param key The key
     * @return the value
     */
    String get(String key);
}
