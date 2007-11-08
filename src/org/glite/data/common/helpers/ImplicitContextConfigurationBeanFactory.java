/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.common.helpers;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;


/**
 * Bean factory for the ImplicitContextConfigurationBean, creates the bean
 * and fills in its internal properties object from the attributes of the
 * factory.
 */
public class ImplicitContextConfigurationBeanFactory implements ObjectFactory {
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
        throws NamingException {
        // Acquire an instance of our specified bean class
        ImplicitContextConfigurationBean bean = new ImplicitContextConfigurationBean();
        Properties props = new Properties();

        // Add all key value pairs found
        Reference ref = (Reference) obj;
        Enumeration addrs = ref.getAll();

        while (addrs.hasMoreElements()) {
            RefAddr addr = (RefAddr) addrs.nextElement();
            String key = addr.getType();
            String value = (String) addr.getContent();
            props.setProperty(key, value);
        }

        bean.setProps(props);

        return (bean);
    }
}
