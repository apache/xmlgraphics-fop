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

package org.apache.fop.render.ps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCListener;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.DefaultNestedDocumentHandler;
import org.apache.xmlgraphics.ps.dsc.events.AbstractResourcesDSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCAtend;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBeginDocument;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentNeededResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentSuppliedResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentEndOfFile;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentIncludeResource;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPage;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.IFContext;

/**
 * Tests the PostScript resource optimization (selective de-duplication of
 * images that are used multiple times).
 */
public class ResourceOptimizationTestCase extends AbstractPostScriptTest {

    /**
     * Tests resource optimization.
     * @throws Exception if an error occurs
     */
    @Test
    public void testResourceOptimization() throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        PSDocumentHandler handler = new PSDocumentHandler(new IFContext(ua));
        // This is the important part: we're enabling resource optimization
        handler.getPSUtil().setOptimizeResources(true);
        ua.setDocumentHandlerOverride(handler);

        // Prepare output file
        File outputFile = renderFile(ua, "ps-resources.fo",
                "-if-l" + handler.getPSUtil().getLanguageLevel());
        verifyPostScriptFile(outputFile);
    }

    private void verifyPostScriptFile(File psFile) throws IOException, DSCException {
        InputStream in = new java.io.FileInputStream(psFile);
        in = new java.io.BufferedInputStream(in);
        try {
            DSCParser parser = new DSCParser(in);

            //The first form is for arrow_down_small.png (to be reused)
            PSResource form1 = new PSResource(PSResource.TYPE_FORM, "FOPForm:1");
            PSResource helvetica = new PSResource(PSResource.TYPE_FONT, "Helvetica");
            PSResource helveticaBold = new PSResource(PSResource.TYPE_FONT, "Helvetica-Bold");

            PSResource res;
            DSCCommentPages pages = (DSCCommentPages)gotoDSCComment(parser, DSCConstants.PAGES);
            assertEquals(2, pages.getPageCount());

            DSCCommentDocumentSuppliedResources supplied
                = (DSCCommentDocumentSuppliedResources)gotoDSCComment(parser,
                        DSCConstants.DOCUMENT_SUPPLIED_RESOURCES);
            Set resources = supplied.getResources();
            assertEquals(5, resources.size());
            assertTrue(resources.contains(form1));
            assertTrue("Expected barcode.eps as supplied resource",
                    resources.contains(new PSResource(PSResource.TYPE_FILE,
                            "test/resources/images/barcode.eps")));

            DSCCommentDocumentNeededResources needed
                = (DSCCommentDocumentNeededResources)gotoDSCComment(parser,
                        DSCConstants.DOCUMENT_NEEDED_RESOURCES);
            resources = needed.getResources();
            assertEquals(2, resources.size());
            assertTrue("Expected Helvetica as needed resource",
                    resources.contains(new PSResource(PSResource.TYPE_FONT, "Helvetica")));
            assertTrue("Expected Helvetica-Bold as needed resource",
                    resources.contains(new PSResource(PSResource.TYPE_FONT, "Helvetica-Bold")));

            //Some document structure checking
            assertNotNull(gotoDSCComment(parser, DSCConstants.BEGIN_DEFAULTS));
            assertNotNull(gotoDSCComment(parser, DSCConstants.END_DEFAULTS));
            assertNotNull(gotoDSCComment(parser, DSCConstants.BEGIN_PROLOG));
            assertNotNull(gotoDSCComment(parser, DSCConstants.END_PROLOG));
            assertNotNull(gotoDSCComment(parser, DSCConstants.BEGIN_SETUP));

            //Check includes for the two referenced base 14 fonts
            DSCCommentIncludeResource include;
            Collection strings = new java.util.HashSet(
                    Arrays.asList(new String[] {"Helvetica", "Helvetica-Bold"}));
            for (int i = 0; i < 2; i++) {
                include = (DSCCommentIncludeResource)gotoDSCComment(
                        parser, DSCConstants.INCLUDE_RESOURCE);
                res = include.getResource();
                assertEquals(PSResource.TYPE_FONT, res.getType());
                strings.remove(res.getName());
            }
            assertEquals(0, strings.size());

            checkResourceComment(parser, DSCConstants.BEGIN_RESOURCE,
                    new PSResource(PSResource.TYPE_ENCODING, "WinAnsiEncoding"));

            //Here, we encounter form 1 again
            checkResourceComment(parser, DSCConstants.BEGIN_RESOURCE, form1);

            assertNotNull(gotoDSCComment(parser, DSCConstants.END_SETUP));
            //Now the actual pages begin

            //---=== Page 1 ===---
            DSCCommentPage page = (DSCCommentPage)gotoDSCComment(parser, DSCConstants.PAGE);
            assertEquals(1, page.getPagePosition());

            assertEquals(DSCAtend.class,
                    gotoDSCComment(parser, DSCConstants.PAGE_RESOURCES).getClass());
            assertNotNull(gotoDSCComment(parser, DSCConstants.BEGIN_PAGE_SETUP));
            assertNotNull(gotoDSCComment(parser, DSCConstants.END_PAGE_SETUP));

            PSResource form2 = new PSResource(PSResource.TYPE_FORM, "FOPForm:2");
            checkResourceComment(parser, DSCConstants.BEGIN_RESOURCE, form2);
            assertNotNull(gotoDSCComment(parser, DSCConstants.PAGE_TRAILER));

            AbstractResourcesDSCComment pageResources;
            pageResources = (AbstractResourcesDSCComment)gotoDSCComment(
                    parser, DSCConstants.PAGE_RESOURCES);
            resources = pageResources.getResources();
            assertEquals(5, resources.size());
            assertTrue(resources.contains(form1));
            assertTrue(resources.contains(form2));
            assertTrue(resources.contains(helvetica));
            assertTrue(resources.contains(helveticaBold));

            //---=== Page 2 ===---
            page = (DSCCommentPage)gotoDSCComment(parser, DSCConstants.PAGE);
            assertEquals(2, page.getPagePosition());

            assertEquals(DSCAtend.class,
                    gotoDSCComment(parser, DSCConstants.PAGE_RESOURCES).getClass());
            assertNotNull(gotoDSCComment(parser, DSCConstants.BEGIN_PAGE_SETUP));
            assertNotNull(gotoDSCComment(parser, DSCConstants.END_PAGE_SETUP));

            DSCCommentBeginDocument beginDocument;
            beginDocument = (DSCCommentBeginDocument)gotoDSCComment(
                    parser, DSCConstants.BEGIN_DOCUMENT);
            assertEquals("test/resources/images/barcode.eps",
                    beginDocument.getResource().getName());
            DSCListener listener = new DefaultNestedDocumentHandler(null);
            listener.processEvent(beginDocument, parser);

            //And again (the barcode is generated twice)
            beginDocument = (DSCCommentBeginDocument)gotoDSCComment(
                    parser, DSCConstants.BEGIN_DOCUMENT);
            assertEquals("test/resources/images/barcode.eps",
                    beginDocument.getResource().getName());
            listener.processEvent(beginDocument, parser);

            assertNotNull(gotoDSCComment(parser, DSCConstants.PAGE_TRAILER));
            pageResources = (AbstractResourcesDSCComment)gotoDSCComment(
                    parser, DSCConstants.PAGE_RESOURCES);
            resources = pageResources.getResources();
            assertEquals(6, resources.size());
            assertTrue(resources.contains(form1));
            assertFalse(resources.contains(form2));
            assertTrue(resources.contains(helvetica));
            assertTrue(resources.contains(helveticaBold));
            assertTrue(resources.contains(beginDocument.getResource()));

            assertNotNull(gotoDSCComment(parser, DSCConstants.TRAILER));
            //No headers in between, as they should have been put at the beginning of the file
            assertEquals(DSCCommentEndOfFile.class, parser.nextEvent().asDSCComment().getClass());

        } finally {
            IOUtils.closeQuietly(in);
        }
    }

}
