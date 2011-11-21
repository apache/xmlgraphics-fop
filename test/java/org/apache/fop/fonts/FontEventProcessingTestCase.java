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

package org.apache.fop.fonts;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

import org.apache.xmlgraphics.util.MimeConstants;

import org.apache.fop.apps.FOPException;
import org.apache.fop.events.EventProcessingTestCase;

/**
 * Testing font events.
 */
public class FontEventProcessingTestCase {

    private EventProcessingTestCase eventsTests = new EventProcessingTestCase();

    private static final String CONFIG_BASE_DIR = EventProcessingTestCase.CONFIG_BASE_DIR;

    @Test
    public void testFont() throws FOPException, TransformerException, IOException, SAXException {
        InputStream inStream = getClass().getResourceAsStream("substituted-font.fo");
        eventsTests.doTest(inStream, null, FontEventProducer.class.getName() + ".fontSubstituted",
                MimeConstants.MIME_PDF);
    }

    @Test
    public void testFontWithBadDirectory() throws FOPException, TransformerException, IOException,
            SAXException {
        InputStream inStream = getClass().getResourceAsStream("substituted-font.fo");
        eventsTests.doTest(inStream, CONFIG_BASE_DIR + "test_fonts_directory_bad.xconf",
                FontEventProducer.class.getName() + ".fontDirectoryNotFound",
                MimeConstants.MIME_PDF);
    }

}
