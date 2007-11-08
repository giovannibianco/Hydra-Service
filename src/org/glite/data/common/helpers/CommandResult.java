/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://eu-egee.org/license.html
 */

package org.glite.data.common.helpers;


/**
 * See {@link StreamChomp}.<br>
 * <p/>
 * Command result class for reading the output of a Runtimne.exec properly.
 * The usage is as follows:<br>
 * <br>
 * <code>
            Runtime runtime = Runtime.getRuntime();<br>
            Process proc = null;<br>
            proc = runtime.exec(command.toString());<br>
            StreamChomp stdout = new StreamChomp(proc.getInputStream(),
                                                 "stdout");<br>
            StreamChomp stderr = new StreamChomp(proc.getErrorStream(),
                                                 "stderr");<br>
            stdout.start();<br>
            stderr.start();<br>
            exitCode = proc.waitFor(); <br>
            while (!stdout.isDone()) { <br>
                Thread.sleep(10);<br>
            }<br>
            while (!stderr.isDone()) {<br>
                Thread.sleep(10);<br>
            }<br>
            commandResult.setOut(stdout);<br>
            commandResult.setErr(stderr);<br>
            commandResult.setRetCode(exitCode);<br>
 </code>
 */
public final class CommandResult {
    /** output stream. */
    private StreamChomp m_out;

    /** error stream.  */
    private StreamChomp m_err;

    /** return code. */
    private int m_retCode;

    /**
     * Get output of command.
     *
     * @return output stream
     */
    public StreamChomp getOut() {
        return m_out;
    }

    /**
     * Bean setter method.
     *
     * @param out the output to be set
     */
    public void setOut(final StreamChomp out) {
        m_out = out;
    }

    /**
     * Get error of command.
     *
     * @return error stream
     */
    public StreamChomp getErr() {
        return m_err;
    }

    /**
     * Bean setter method.
     *
     * @param err the error to be set
     */
    public void setErr(final StreamChomp err) {
        m_err = err;
    }

    /**
     * Get return code of command.
     *
     * @return Return code
     */
    public int getRetCode() {
        return m_retCode;
    }

    /**
     * Bean setter method.
     *
     * @param retCode return code
     */
    public void setRetCode(final int retCode) {
        m_retCode = retCode;
    }
}
