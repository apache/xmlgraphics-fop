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

package org.apache.fop.config;

import static org.junit.Assert.fail;

import org.apache.fop.apps.FOPException;
import org.junit.Test;

/**
 * Super class for several user configuration failure cases.
 */
public abstract class BaseDestructiveUserConfigTestCase extends BaseUserConfigTestCase {

    /**
     * Test the user configuration failure.
     */
    @Test
    public void testUserConfig() {
        try {
            initConfig();
            convertFO();
            fail( getName() + ": Expected Configuration Exception" );
        } catch (FOPException e) {
            // this *should* happen!
        } catch (Exception e) {
            e.printStackTrace();
            fail( getName() + ": Expected FOPException but got: " + e.getMessage() );
        }
    }
}
