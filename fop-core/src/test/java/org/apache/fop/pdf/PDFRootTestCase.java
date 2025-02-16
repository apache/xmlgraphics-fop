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
package org.apache.fop.pdf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.Event;
import org.apache.fop.events.EventListener;

public class PDFRootTestCase {

    @Test
    public void testAddAf() {
        String germanAe = "\u00E4";
        String unicodeFilename = "t" + germanAe + "st.pdf";
        PDFFileSpec fileSpec = new PDFFileSpec(unicodeFilename);

        String filename = fileSpec.getFilename();

        PDFDocument doc = new PDFDocument("");
        doc.getRoot().addAF(fileSpec);

        assertEquals(filename, fileSpec.getFilename());
        assertEquals(unicodeFilename, fileSpec.getUnicodeFilename());
    }

    @Test
    public void testLanguage() {
        PDFDocument document = new PDFDocument("");
        FOUserAgent ua = FopFactory.newInstance(new File(".").toURI()).newFOUserAgent();
        final List<Event> events = new ArrayList<>();
        ua.getEventBroadcaster().addEventListener(new EventListener() {
            public void processEvent(Event event) {
                events.add(event);
            }
        });
        document.getFactory().setEventBroadcaster(ua.getEventBroadcaster());
        PDFRoot root = new PDFRoot(document, new PDFPages(document));
        root.setLanguage(Locale.US);
        Assert.assertTrue(events.isEmpty());
        root.setLanguage(Locale.UK);
        assertEquals(events.get(0).getEventKey(), "languageChanged");
    }
}
