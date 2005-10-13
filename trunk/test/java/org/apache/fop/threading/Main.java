/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.threading;

import java.io.File;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;

public class Main {

    public static void main(String[] args) {
        try {
            //Read configuration
            File cfgFile = new File(args[0]);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            Configuration cfg = builder.buildFromFile(cfgFile);
            
            //Setup testbed
            FOPTestbed testbed = new FOPTestbed();
            ContainerUtil.enableLogging(testbed, new ConsoleLogger(ConsoleLogger.LEVEL_INFO));
            ContainerUtil.configure(testbed, cfg);
            ContainerUtil.initialize(testbed);
            
            //Start tests
            testbed.doStressTest();
            
            System.exit(0);
        } catch (Exception e) {
            System.err.println(ExceptionUtil.printStackTrace(e));
            System.exit(-1);
        }
    }
}