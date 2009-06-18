/* 
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the 
 * LICENSE file included with these sources."
 */

package org.apache.fop.apps;

import org.apache.fop.configuration.Configuration;
/**
 * class representing the version of FOP.
 */
public class Version {

    /**
     * get the version of FOP
     *
     * @return the version string
     */
    public static String getVersion() {
        return Configuration.getStringValue("version");
    }
}
