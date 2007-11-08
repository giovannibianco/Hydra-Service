/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * ServletContextListener implementation to properly set up and shut down the
 * database connections of the service.
 * <p/>
 * <p>Example configuration in web.xml:
 * <pre>
 *   &lt;listener&gt;
 *     &lt;listener-class&gt;
 *       org.edg.data.common.helpers.ServiceContextListener
 *     &lt;/listener-class&gt;
 *   &lt;/listener&gt;
 * </pre>
 *
 * @author <a href="mailto:Akos.Frohner@cern.ch">Frohner Akos</a>
 */
public class ServiceContextListener implements ServletContextListener {
    final static private Logger log = Logger.getLogger(ServiceContextListener.class);

    public void contextInitialized(ServletContextEvent event) {

        final ServletContext context = event.getServletContext();
        
        // Load the alternative log4j config file
        LoggerHelper.configureLogger(context);
        
      	log.info("catalog service is being started ...");
      	
      	ServiceSetup.start();

    }

    public void contextDestroyed(ServletContextEvent event) {

    	final ServletContext context = event.getServletContext();
    	
        // Load the alternative log4j config file
        LoggerHelper.configureLogger(context);
        
        ServiceSetup.stop();
    }
}
