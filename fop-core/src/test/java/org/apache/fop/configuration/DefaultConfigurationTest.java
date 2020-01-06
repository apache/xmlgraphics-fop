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

package org.apache.fop.configuration;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultConfigurationTest {

    DefaultConfiguration configuration;

    @Before
    public void setup() throws Exception {
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        configuration = builder.build(getClass().getResourceAsStream("sample_config.xml"));
    }

    @Test
    public void testGetChild() {
        Configuration fontsConfig = configuration.getChild("fonts");
        assertEquals("fonts element should be direct child", "fop/fonts",  fontsConfig.getLocation());
    }

    @Test
    public void testGetChildren() {
        Configuration[] fontsConfig = configuration.getChildren("fonts");
        assertEquals("only direct children should match", 1, fontsConfig.length);
        assertEquals("fonts element should be direct child", "fop/fonts", fontsConfig[0].getLocation());
    }
}
