/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.Properties;


/**
 * A configuration bean that uses a Java properties file as
 * its source, which is expected to be in the application's classpath.
 * The name configfile to use is a bean property (configFilename).
 * The class will only read its configuration once, upon creation.
 * There should only ever be one instance of this bean per application.
 */
public class FileConfigurationBean implements ConfigurationBean {
    /* Log4j logger for class */
    private final static Logger m_log = Logger.getLogger(FileConfigurationBean.class);
    private String m_configFilename;
    private boolean m_initialised;
    private Properties m_properties;

    public FileConfigurationBean() {
        m_initialised = false;
        m_log.info("New FileConfigurationBean created");
    }

    /* read the filename */
    private void readConfigFile() throws IOException {
        m_properties = new Properties();

        if (m_configFilename == null) {
            throw new FileNotFoundException("Filename was not specified in bean configuration: " +
                "configFilename property");
        }

        m_log.debug("Reading properties config file: " + m_configFilename);

        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(m_configFilename);
        m_properties.load(stream);
        m_log.debug("Loaded properties OK");
    }

    /**
     * Get the configuration item for the given key
     *
     * @param key the config key to retrieve
     * @return the config value
     */
    public String get(String key) {
        m_log.debug("Fetching configuration key: " + key);

        synchronized (this) {
            if (!m_initialised) {
                init();
            }
        }

        Object o = m_properties.getProperty(key);

        if (o == null) {
            m_log.debug("Key <" + key + "> was not in configuration file." + " Returning null.");

            return null;
        }

        String value = (String) o;
        m_log.debug("Found key <" + key + "> with value <" + value + ">");

        return value;
    }

    /* initiliase the bean */
    synchronized private void init() {
        m_log.debug("Initialising FileConfigurationBean");

        try {
            readConfigFile();
        } catch (IOException e) {
            m_log.error("Could not read config file: " + m_configFilename, e);

            return;
        }

        m_initialised = true;
    }

    public String getConfigFilename() {
        return m_configFilename;
    }

    public void setConfigFilename(String configFilename) {
        this.m_configFilename = configFilename;
    }
}
