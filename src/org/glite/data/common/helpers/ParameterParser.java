/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;

import javax.servlet.ServletRequest;


/**
 * Fetches servlet paramaters. Mostly borrowed from com.oreilly.. :)
 */
public class ParameterParser {
    /* Log4j logger for class */
    private final static Logger m_log = Logger.getLogger(ParameterParser.class);
    private ServletRequest m_req = null;
    private String m_encoding = null;

    public ParameterParser(ServletRequest req) {
        m_req = req;
    }

    public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
        "".getBytes(encoding); // check encoding is valid.
        m_encoding = encoding;
    }

    public String getStringParameter(String param) throws ParamaterNotFoundException {
        String[] values = m_req.getParameterValues(param);

        if (values == null) {
            throw new ParamaterNotFoundException(param + " not found");
        } else if (values[0].length() == 0) {
            throw new ParamaterNotFoundException(param + " was empty");
        } else if (m_encoding == null) {
            return values[0];
        } else {
            try {
                return new String(values[0].getBytes("8859_1"), m_encoding);
            } catch (UnsupportedEncodingException e) {
                return values[0];
            }
        }
    }

    public String getStringParameter(String param, String def) {
        try {
            String s = getStringParameter(param);

            return s;
        } catch (ParamaterNotFoundException e) {
            return def;
        }
    }
}
