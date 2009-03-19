/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;


/**
 * Provides implementation for the base Service interface,
 * using the version.properties file, loaded from the classpath.
 *
 * @author <a href="mailto:Akos.Frohner">Akos Frohner</a>
 */
public class ServiceImpl {
    // Log4j logger for class
    static private Logger m_log = Logger.getLogger(ServiceImpl.class);

    /**
     * The name of the version file for the services.
     */
    static protected final String VERSION_PROPERTIES_FILE = "version.properties";

    /**
     * The singleton object.
     */
    static private Properties props = null;

    static {
        initProperties();

        // Sanity check: see if the module version is known
        if (getProperty("VERSION") == null) {
            m_log.fatal("*** FATAL CONFIGURATION ERROR *** The VERSION is unknown ***");
        }
    }

    protected ServiceImpl() {
        initProperties();
    }

    static protected void initProperties() {
        // return if the singleton is already initialized
        if (props != null) {
            return;
        }

        // singleton initialization
        props = new Properties();

        InputStream inp = null;
        try {
            ClassLoader loader = ServiceImpl.class.getClassLoader();

            if (loader != null) {
                inp = loader.getResourceAsStream(VERSION_PROPERTIES_FILE);
            } else {
                inp = ClassLoader.getSystemResourceAsStream(VERSION_PROPERTIES_FILE);
            }

            props.load(new BufferedInputStream(inp));

            m_log.info("Configuration file '" + VERSION_PROPERTIES_FILE + "' loaded");
        } catch (IOException e) {
            m_log.error("Error loading config file " + VERSION_PROPERTIES_FILE + ": " + e);
        } finally {
            try {
                inp.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Returns the value of the given property, or <code>null</code> if
     * there is no such property.
     */
    static protected String getProperty(String key) {
        String value = props.getProperty(key);
        m_log.debug("Property " + key + " = " + value);

        return value;
    }

    public String getVersion() {
        return getProperty("VERSION");
    }

    public String getInterfaceVersion() {
        return getProperty("INTERFACE_VERSION");
    }

    public String getSchemaVersion() {
        return getProperty("SCHEMA_VERSION");
    }

    public String getServiceMetadata(String key) {
        return getProperty(key);
    }
}
