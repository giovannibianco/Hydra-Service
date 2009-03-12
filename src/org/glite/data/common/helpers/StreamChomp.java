/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Handles the results from the Runtine.exec().
 * See {@link org.glite.data.common.helpers.CommandResult} for usage.
 */
public class StreamChomp extends Thread {
    /* Log4j logger for class */
    private final static Logger m_log = Logger.getLogger(StreamChomp.class);
    private boolean m_isDone;
    private StringBuffer m_result;
    private InputStream m_stream;
    private String m_streamName;

    public StreamChomp(InputStream stream, String name) {
        m_stream = stream;
        m_streamName = name;
        m_result = new StringBuffer();
        m_isDone = false;
    }

    public void run() {
        Thread.currentThread().setName(m_streamName.trim() + "_chomp");
        m_log.debug("Processing stream: " + m_streamName);

        try {
            InputStreamReader isr = new InputStreamReader(m_stream);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                m_result.append(line);
            }
        } catch (IOException e) {
            m_log.error("Error processing stream: " + m_streamName, e);
        }

        m_isDone = true;
    }

    public boolean isDone() {
        return m_isDone;
    }

    public StringBuffer getResult() {
        return m_result;
    }

    public String getStreamName() {
        return m_streamName;
    }
}
