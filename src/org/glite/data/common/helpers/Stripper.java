/*
 * Copyright (c) Members of the EGEE Collaboration. 2004.
 * See http://eu-egee.org/partners/ for details on the copyright holders.
 * For license conditions see the license file or http://www.apache.org/licenses/LICENSE-2.0
 */

/*
 * Authors: Gavin McCance <gavin.mccance@cern.ch>
 * Version: $Id: Stripper.java,v 1.2 2007-11-08 10:41:29 szamsu Exp $
 */
 
 
/* COMMENT: lfns cannot have characters normally allowed in FS , i.e. =,& etc. as they checked by URI checker here*/
package org.glite.data.common.helpers;

import org.apache.log4j.Logger;

import org.doomdark.uuid.UUID;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Class for stripping and regularising LFNs and GUIDs.
 */
public class Stripper {
    /* Log4j logger for class */
    private final static Logger m_log = Logger.getLogger(Stripper.class);

    /**
     * LFN output must be of form "/path/path/filename"
     * Allowable input forms are:
     * <ul>
     * <li> /path/path/filename, //path/path/filename, etc
     * <li> lfn:/path/path/filename
     * <li> lfn://path/path/filename, lfn:///path/path/filename, etc
     * </ul>
     * In general: all multiple /'s are changed to a single "/"; the prefix "lfn:"
     * is removed; "/./" is resolved to "/"; "path1/path2/../" is resolved to "path1/";
     * the trailing "/" is always removed if it exists.
     *
     * @param lfn
     * @return the stripped LFN
     * @throws IllegalArgumentException in case of malformed LFN
     */
    public static String regulariseLFN(String lfn) throws IllegalArgumentException {
        URI uri = null;

        if (lfn == null) {
            m_log.error("LFN was null!");
            throw new IllegalArgumentException("LFN was null!");
        }

        try {
            uri = new URI(lfn);
            
        } catch (URISyntaxException e) {
            m_log.error("Bad URI format: ", e);
            throw new IllegalArgumentException("Bad URI format: " + e);
        }

        if ((uri.getScheme() != null) && (!"lfn".equalsIgnoreCase(uri.getScheme()))) {
            m_log.error(("Bad scheme <" + uri.getScheme() + ">: it must be 'lfn' or null"));
            throw new IllegalArgumentException(("Bad scheme <" + uri.getScheme() + ">: it must be 'lfn' or null"));
        }

        URI normal = uri.normalize();

        String normalPath = normal.getPath();

        if (normalPath == null) { // checks for missing "/" and prefixed
            m_log.error("LFN was unparsable: " + lfn);
            throw new IllegalArgumentException("Path was unparsable: " + lfn);
        } else if (normalPath.endsWith("/") && normalPath.length() > 1) {
            return normalPath.substring(0, normalPath.length() - 1);
        } else if (!normalPath.startsWith("/")) {
            m_log.error("LFN was unparsable: " + lfn);
            throw new IllegalArgumentException("Path did not start with '/': " + uri.getPath());
        } else {
            return normalPath;
        }
    }

    /**
     * Regularise a GUID.
     *
     * Removes the "guid:" prefix.
     * Sets up lowercase.
     * Validates the guid if requested.
     *
     * @param guidin - the GUID to regularise
     * @param validate if true, the GUID is validated using Jug.
     * @return
     * @throws IllegalArgumentException if the GUID was bad
     */
    public static String regulariseGUID(String guidin, boolean validate)
        throws IllegalArgumentException {
        if (guidin == null) {
            m_log.error("GUID was null");
            throw new IllegalArgumentException("GUID was null");
        }

        String guid = null;

        if (guidin.startsWith("guid:")) {
            guid = guidin.substring(5);
        } else {
            guid = guidin;
        }

        if (!validate) {
            return guid.toLowerCase();
        }

        try {
            UUID uuid = new UUID(guid);

            return uuid.toString();
        } catch (NumberFormatException e) {
            m_log.error("GUID was malformed: " + guidin);
            throw new IllegalArgumentException("GUID was malformed: " + guidin);
        }
    }

     /**
     * Regularise a ClientDN.
     *
     * Removes the "/CN=proxy" prefix.
     *
     * @param guidin - the GUID to regularise
     * @param validate if true, the GUID is validated using Jug.
     * @return
     * @throws IllegalArgumentException if the GUID was bad
     */
    public static String regulariseClientDN(String clientDN)
        throws IllegalArgumentException {
    
        String clientDNStripped = clientDN;
        m_log.debug("Client Certificate : " + clientDN);
        while (clientDNStripped.endsWith("/CN=proxy")) {
            clientDNStripped = clientDNStripped.substring(0, clientDNStripped.length() - 9);
               //m_log.debug("Client Certificate : " + clientDNStripped);
        }
        if (clientDNStripped == null) {
            m_log.error("Malformed certificate (null client DN)");
            throw new IllegalArgumentException("The client certificate is malformed. Access is denied.");
        }
        return clientDNStripped; 
    }

    
}
