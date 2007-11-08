/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

package org.glite.data.common.helpers;


/**
 * Defines the methods that a maintenance thread must implement.
 */
public interface MaintenanceThread {
    /**
     * To shuitdown the thread. The thread should response to this in a
     * reasonable time.
     */
    public void shutdown();

    /**
     * The run method must exist. This implies that the MaintenacneThread must
     * extend java.lang.Thread.
     */
    public void run();
}
