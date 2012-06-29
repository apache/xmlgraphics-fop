/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.apache.avalon.framework.ExceptionUtil;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.ConsoleLogger;

/**
 * Starter class for the multi-threading testbed.
 */
public class Main {

    private static void prompt() throws IOException {
        BufferedReader in = new BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.print("Press return to continue...");
        in.readLine();
    }

    /**
     * Main method.
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            //Read configuration
            File cfgFile = new File(args[0]);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            Configuration cfg = builder.buildFromFile(cfgFile);

            boolean doPrompt = cfg.getAttributeAsBoolean("prompt", false);
            if (doPrompt) {
                prompt();
            }

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
