/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.util.Properties;


/**
 * Configuration bean that uses the bean's own factory to set the configuration properties. Any configuration properties
 * found for this bean;s factory will be added. Generally, it is only suitable for smallish configuration sets. The bean
 * does not expose any public setter or getter methods. <br><br> There should only ever be one instance of this bean per
 * application. <br><br> The bean autoinitialised by its factory upon creation.
 */
public class ImplicitContextConfigurationBean implements ConfigurationBean {
    /* Log4j logger for class */
    private final static Logger m_log = Logger.getLogger(ImplicitContextConfigurationBean.class);
    private Properties m_props;

    public ImplicitContextConfigurationBean() {
        m_log.info("Created new ImplicitContextConfigurationBean");
    }

    /**
     * Get the configuration item for the given key
     *
     * @param key
     * @return the value
     */
    public String get(String key) {
        m_log.debug("Fetching configuration key: " + key);

        Object o = m_props.getProperty(key);

        if (o == null) {
            m_log.debug("Key <" + key + "> was not in configuration file." + " Returning null.");

            return null;
        }

        String value = (String) o;
        m_log.debug("Found key <" + key + "> with value <" + value + ">");

        return value;
    }

    public Properties getProps() {
        return m_props;
    }

    public void setProps(Properties props) {
        m_props = props;
    }
}
