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
package org.apache.fop.render.afp;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.helpers.AttributesImpl;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.render.afp.extensions.AFPElementMapping;
import org.apache.fop.render.afp.extensions.AFPExtensionAttachment;
import org.apache.fop.render.afp.extensions.AFPExtensionHandler;
import org.apache.fop.render.afp.extensions.AFPPageOverlay;
import org.apache.fop.render.intermediate.IFContext;

public class PageOverlayTestCase {
    @Test
    public void testPageOverlay() throws Exception {
        Assert.assertEquals(getPageOverlay(), "BEGIN DOCUMENT DOC00001\n"
                + "BEGIN PAGE_GROUP PGP00001\n"
                + "END PAGE_GROUP PGP00001\n"
                + "BEGIN PAGE PGN00001\n"
                + "BEGIN ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "MAP PAGE_OVERLAY Triplets: FULLY_QUALIFIED_NAME,RESOURCE_LOCAL_IDENTIFIER,\n"
                + "DESCRIPTOR PAGE\n"
                + "MIGRATION PRESENTATION_TEXT\n"
                + "END ACTIVE_ENVIRONMENT_GROUP AEG00001\n"
                + "INCLUDE PAGE_OVERLAY\n"
                + "END PAGE PGN00001\n"
                + "END DOCUMENT DOC00001\n");
    }

    private String getPageOverlay() throws Exception {
        FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());
        FOUserAgent ua = fopFactory.newFOUserAgent();
        AFPDocumentHandler documentHandler = new AFPDocumentHandler(new IFContext(ua));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        documentHandler.setResult(new StreamResult(outputStream));
        documentHandler.startDocument();
        documentHandler.startPageSequence("");
        documentHandler.startPage(0, "", "", new Dimension());
        documentHandler.startPageHeader();
        AFPPageOverlay pageOverlay = new AFPPageOverlay();
        pageOverlay.setName("testtest");
        documentHandler.handleExtensionObject(pageOverlay);
        documentHandler.endPageSequence();
        documentHandler.endDocument();
        StringBuilder sb = new StringBuilder();
        new AFPParser(true).read(new ByteArrayInputStream(outputStream.toByteArray()), sb);
        return sb.toString();
    }

    @Test
    public void testXY() throws Exception {
        AFPExtensionHandler extensionHandler = new AFPExtensionHandler();
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute(null, null, "x", null, "1");
        attributes.addAttribute(null, null, "y", null, "1");
        extensionHandler.startElement(AFPExtensionAttachment.CATEGORY, AFPElementMapping.INCLUDE_PAGE_OVERLAY, null,
                attributes);
        extensionHandler.endElement(AFPExtensionAttachment.CATEGORY, AFPElementMapping.INCLUDE_PAGE_OVERLAY, null);
        AFPPageOverlay pageOverlay = (AFPPageOverlay) extensionHandler.getObject();
        Assert.assertEquals(pageOverlay.getX(), 1);
        Assert.assertEquals(pageOverlay.getY(), 1);
    }
}
