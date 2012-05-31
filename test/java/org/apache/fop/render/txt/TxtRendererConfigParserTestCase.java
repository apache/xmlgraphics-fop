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

package org.apache.fop.render.txt;

import org.junit.Test;

import org.apache.fop.apps.AbstractRendererConfigParserTester;
import org.apache.fop.apps.TxtRendererConfBuilder;
import org.apache.fop.render.RendererConfig.RendererConfigParser;
import org.apache.fop.render.txt.TxtRendererConfig.TxtRendererConfigOptions;
import org.apache.fop.render.txt.TxtRendererConfig.TxtRendererConfigParser;

import static org.junit.Assert.assertEquals;

public class TxtRendererConfigParserTestCase
        extends AbstractRendererConfigParserTester<TxtRendererConfBuilder, TxtRendererConfig> {

    public TxtRendererConfigParserTestCase() {
        super(new TxtRendererConfigParser(), TxtRendererConfBuilder.class);
    }

    @Test
    public void testEncoding() throws Exception {
        parseConfig(createRenderer().setEncoding("UTF-16"));
        assertEquals("UTF-16", conf.getEncoding());

        // Check validation isn't done at this point
        parseConfig(createRenderer().setEncoding("RandomString"));
        assertEquals("RandomString", conf.getEncoding());

        // Check the default behaviour is expected
        parseConfig(createRenderer());
        assertEquals(TxtRendererConfigOptions.ENCODING.getDefaultValue(), conf.getEncoding());
    }

}
