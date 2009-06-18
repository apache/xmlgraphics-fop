/*
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */

package org.apache.fop.configuration;


// java
import java.io.File;
import java.io.FileNotFoundException;
// fop
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.InputHandler;

/**
 * UserOptions handles loading of configuration files and
 * additional setting of commandline options
 */
public class UserOptions extends SystemOptions {

    protected static final String defaultUserConfigFile =
        "userconfig.xml";

    /**
     * 
     */
    public UserOptions(Configuration configuration) {
        super(configuration);
    }

    /**
     * Configure the system according to the system configuration file
     * config.xml and the user configuration file if it is specified in the
     * system configuration file.
     */
    public void configure()
    throws FOPException, FileNotFoundException {
        loadUserConfiguration(getUserConfigFileName());
        super.configure();
    }
    
    /**
     * Load a user-defined configuration file.
     * An initial attempt is made to use a File generated from
     * <code>userConfigFileName</code> as the configuration reader file input
     * source.  If this fails, an attempt is made to load the file using
     * <code>loadConfiguration</code>.
     * @param userConfigFileName the name of the user configuration file.
     */
    public void loadUserConfiguration(String userConfigFileName) {
        // read user configuration file
        boolean readOk = true;
        if (userConfigFileName == null) {
            log.config("No user config file name");
            return;
        }
        File userConfigFile = new File(userConfigFileName);
        if (userConfigFile == null) {
            return;
        }
        log.config(
                "reading user configuration file " + userConfigFileName);
        try {
            ConfigurationReader reader = new ConfigurationReader(
                    InputHandler.fileInputSource(userConfigFile),
                    configuration);
        } catch (FOPException ex) {
            log.warning("Can't find user configuration file "
                    + userConfigFile + " in user locations");
            if (isDebugMode()) {
                ex.printStackTrace();
            }
            readOk = false;
        }
        if (! readOk) {
            try {
                // Try reading the file using loadConfig()
                super.loadConfiguration(userConfigFileName);
            } catch (FOPException ex) {
                log.warning("Can't find user configuration file "
                        + userConfigFile + " in system locations");
                if (isDebugMode()) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
}
