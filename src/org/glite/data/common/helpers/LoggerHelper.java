/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public abstract class LoggerHelper {
	
	//static private Logger m_log = Logger.getLogger(LoggerHelper.class);

	static private boolean wasCalled = false;
	
	public static void configureLogger(ServletContext ctx) {

		// Take the value from a config param in context.xml
		String log4jPropLocation = ctx.getInitParameter("log4j.configFile.path");

		configureLogger(log4jPropLocation);
	}
    
	public static void configureLogger(String propFileLocation) {

		// Make sure it is only called once
		if(!wasCalled) {
			
			// Load the new configuration and set the watch if a location was given
		    if(propFileLocation != null) {
		    	PropertyConfigurator.configureAndWatch(propFileLocation, 60000);
		    	//m_log.info("Alternative log4j properties file location: " + propFileLocation);
		    } else {
		    	//m_log.info("No alternative log4j properties file location given. Will try classpath.");
		    }
		    
			wasCalled = true;
		}
	
	}

}
