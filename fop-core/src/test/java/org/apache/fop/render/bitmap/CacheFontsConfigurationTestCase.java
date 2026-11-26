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

package org.apache.fop.render.bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Test;
import static org.junit.Assert.fail;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;

/**
 * An invalid font configuration (strict) fails with a {@link FOPException} when the renderer is set up,
 * with and without cache-fonts.
 */
public class CacheFontsConfigurationTestCase {

    private static FopFactory factory(boolean cacheFonts) throws Exception {
        String conf = "<fop version=\"1.0\"><strict-configuration>true</strict-configuration><renderers>"
                + "<renderer mime=\"image/png\"><fonts cache-fonts=\"" + cacheFonts + "\">"
                // "|" is not allowed in a URI: with strict configuration, reading the font list fails
                + "<font embed-url=\"bad|name.ttf\">"
                + "<font-triplet name=\"Missing\" style=\"normal\" weight=\"normal\"/></font>"
                + "</fonts></renderer></renderers></fop>";
        return new FopConfParser(new ByteArrayInputStream(conf.getBytes("UTF-8")), new File(".").toURI())
                .getFopFactoryBuilder().build();
    }

    private static void assertNewFopFails(boolean cacheFonts) throws Exception {
        FopFactory factory = factory(cacheFonts);
        try {
            factory.newFop(MimeConstants.MIME_PNG, factory.newFOUserAgent(), new ByteArrayOutputStream());
            fail("FOPException expected for an invalid font URI");
        } catch (FOPException e) {
            // expected
        }
    }

    @Test
    public void testInvalidFontFailsWithoutCache() throws Exception {
        assertNewFopFails(false);
    }

    @Test
    public void testInvalidFontFailsWithCache() throws Exception {
        assertNewFopFails(true);
    }
}
