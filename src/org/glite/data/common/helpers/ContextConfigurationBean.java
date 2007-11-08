/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Base class for a configuration bean that uses the bean's own factory to
 * set the configuration properties. This class should be subclassed with the
 * relevant setter and getter methods for each config item. As such it is only
 * suitable for smallish configuration sets. After initialisation, any call to
 * the relevant setter method will show up live in the configuration.
 * <br><br>
 * There should only ever be one instance of this bean per application.
 * <br><br>
 * The bean actively reflects the name of its getter method per call to
 * {@link #get} rather than caching, so is not designed for heavy use.
 */
public abstract class ContextConfigurationBean implements ConfigurationBean {
    /* Log4j logger for class
    */
    private final static Logger m_log = Logger.getLogger("org.glite.data.common.helpers.ContextConfigurationBean");
    private PropertyDescriptor[] m_beanProps = null;

    public ContextConfigurationBean() {
        // for super
        m_log.info("Created new ContextConfigurationBean");
    }

    /**
     * Get the configuration item for the given key
     *
     * @param key
     * @return the value
     */
    public String get(String key) {
        Method getter = introspect(key);

        if (getter == null) {
            m_log.debug("Could not find bean property with key <" + key + ">." + " Returning null.");

            return null;
        }

        Object o;

        try {
            o = getter.invoke(this, (java.lang.Object[])null);
        } catch (IllegalAccessException e) {
            m_log.fatal("Bean introspection pointed to an illegal method: " + getter.toString(), e);
            throw new AssertionError("Bean introspection pointed to an illegal method: " + getter.toString());
        } catch (InvocationTargetException e) {
            m_log.fatal("Bean introspection pointed to an illegal method: " + getter.toString(), e);
            throw new AssertionError("Bean introspection pointed to an illegal method: " + getter.toString());
        }

        String value = (String) o;
        m_log.debug("Found key <" + key + "> with value <" + value + ">");

        return value;
    }

    private Method introspect(String key) {
        synchronized (this) {
            if (m_beanProps == null) {
                try {
                    BeanInfo info = Introspector.getBeanInfo(this.getClass());
                    m_beanProps = info.getPropertyDescriptors();
                } catch (IntrospectionException e) {
                    m_log.error("Introspection error on ContextConfigurationBean");

                    return null;
                }
            }
        }

        Method getter = null;

        for (int i = 0; i < m_beanProps.length; i++) {
            PropertyDescriptor propertyDescriptor = m_beanProps[i];

            if (key.equals(propertyDescriptor.getName())) {
                getter = propertyDescriptor.getReadMethod();
            }
        }

        return getter;
    }
}
