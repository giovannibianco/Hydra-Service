/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;


/**
 * Contains {@link #start} and {@link #stop} methods for starting and stopping
 * auxiliary services of the catalog service environment. This class can be used
 * both by the web application and the testing classes to encapsulate the
 * initialization and shutdown logic.
 */
public class ServiceSetup {
    private static final Logger log = Logger.getLogger(ServiceSetup.class);

    /**
     * Initializes the Service Java environment.
     */
    public static void start() {
        try {
            // Initialize the base interface implementation
            Class.forName("org.glite.data.common.helpers.ServiceImpl");
        } catch (ClassNotFoundException e) {
            log.fatal("*** FATAL DEPLOYMENT PROBLEM *** an essential class was not found:" + e.toString());
        }

        log.info("The org.glite.data service is started");
    }

    /**
     * Stops background threads, cleans up Java environment.
     */
    public static void stop() {
        log.info("The org.glite.data service is stopped");
    }
}
