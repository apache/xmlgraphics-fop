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

/* $Id: Accessibility.java 1343632 2012-05-29 09:48:03Z vhennebert $ */
package org.apache.fop.activity;

import org.apache.fop.configuration.Configurable;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;

public final class ContainerUtil {

    private ContainerUtil() {
        // Never invoked.
    }

    public static void configure(Configurable configurable, Configuration configuration) {
        try {
            configurable.configure(configuration);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static void initialize(Initializable initializable) {
        try {
            initializable.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
