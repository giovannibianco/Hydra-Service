/*
 *  Copyright (c) Members of the EGEE Collaboration. 2004.
 *  See http://public.eu-egee.org/partners/ for details on the copyright holders.
 *  For license conditions see the license file or http://www.eu-egee.org/license.html
 */

package org.glite.data.hydra.helpers;

import org.glite.data.common.helpers.LoggerHelper;

import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * This is the listener that loads the alternative log4j config file.
 */
public class HydraContextListener implements ServletContextListener {
    // Logger object
    private final static Logger m_log = Logger.getLogger(HydraContextListener.class);

    public void contextInitialized(ServletContextEvent event) {
        // Load the alternative log4j config file
        LoggerHelper.configureLogger(event.getServletContext());

        m_log.info("Loaded alternative log4j config file.");
    }

    public void contextDestroyed(ServletContextEvent event) { }
}

