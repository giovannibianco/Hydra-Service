/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * Utility class to wrap JNDI bean fetch.
 */
public class JNDIBeanFetcher {
    /* Log4j logger for class org.glite.data.common.helpers.JNDIBeanFetcher */
    private final static Logger m_log = Logger.getLogger("org.glite.data.common.helpers.JNDIBeanFetcher");

    public static Object fetchBean(String beanFactoryName)
        throws NamingException {
        Context initCtx = new InitialContext();
        m_log.debug("JNDI: Looking up java:comp/env");

        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        m_log.debug("JNDI: Looking up bean factory named: " + "bean/" + beanFactoryName);

        Object o = envCtx.lookup("bean/" + beanFactoryName);
        m_log.info("JNDI: fetched bean/" + beanFactoryName + " bean");

        return o;
    }
}
