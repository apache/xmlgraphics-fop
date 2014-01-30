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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.io.IOUtils;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPage;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentTitle;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.IFContext;

/**
 * Tests the image handling in PostScript output.
 */
public class ImageHandlingTestCase extends AbstractPostScriptTest {

    /**
     * Tests JPEG handling.
     * @throws Exception if an error occurs
     */
    @Test
    public void testJPEGImageLevel3() throws Exception {
        innerTestJPEGImage(3);
    }

    /**
     * Tests JPEG handling.
     * @throws Exception if an error occurs
     */
    @Test
    public void testJPEGImageLevel2() throws Exception {
        innerTestJPEGImage(2);
    }

    private void innerTestJPEGImage(int level) throws Exception {
        FOUserAgent ua = fopFactory.newFOUserAgent();
        PSDocumentHandler handler = new PSDocumentHandler(new IFContext(ua));
        PSRenderingUtil psUtil = handler.getPSUtil();
        psUtil.setLanguageLevel(level);
        psUtil.setOptimizeResources(true);
        ua.setDocumentHandlerOverride(handler);

        // Prepare output file
        File outputFile = renderFile(ua, "ps-jpeg-image.fo",
                "-if-l" + psUtil.getLanguageLevel());
        verifyPostScriptFile(outputFile, psUtil.getLanguageLevel());
    }

    private void verifyPostScriptFile(File psFile, int level)
                throws IOException, DSCException {
        InputStream in = new java.io.FileInputStream(psFile);
        in = new java.io.BufferedInputStream(in);
        try {
            DSCParser parser = new DSCParser(in);

            DSCCommentPages pages = (DSCCommentPages)gotoDSCComment(parser, DSCConstants.PAGES);
            assertEquals(1, pages.getPageCount());

            //Skip procsets and encoding
            gotoDSCComment(parser, DSCConstants.BEGIN_RESOURCE);
            gotoDSCComment(parser, DSCConstants.BEGIN_RESOURCE);
            gotoDSCComment(parser, DSCConstants.BEGIN_RESOURCE);
            gotoDSCComment(parser, DSCConstants.BEGIN_RESOURCE);

            PSResource form2 = new PSResource(PSResource.TYPE_FORM, "FOPForm:2");
            checkResourceComment(parser, DSCConstants.BEGIN_RESOURCE, form2);
            DSCCommentTitle title = (DSCCommentTitle)parser.nextEvent().asDSCComment();
            assertEquals("image/jpeg test/resources/images/bgimg300dpi.jpg", title.getTitle());

            String resourceContent = getResourceContent(parser);

            if (level == 3) {
                assertContains(resourceContent, "/FOPForm:2");
                assertContains(resourceContent, "/DCTDecode filter");
                assertContains(resourceContent, "/ReusableStreamDecode filter");
            } else {
                assertContains(resourceContent, "/FOPForm:2");
                assertContains(resourceContent, "/DCTDecode filter");
                assertAbsent(resourceContent, "/ReusableStreamDecode filter");
            }

            //---=== Page 1 ===---
            DSCCommentPage page = (DSCCommentPage)gotoDSCComment(parser, DSCConstants.PAGE);
            assertEquals(1, page.getPagePosition());

            PSResource form1 = new PSResource(PSResource.TYPE_FORM, "FOPForm:1");
            checkResourceComment(parser, DSCConstants.BEGIN_RESOURCE, form1);
            title = (DSCCommentTitle)parser.nextEvent().asDSCComment();
            assertEquals("image/jpeg test/resources/images/bgimg72dpi.jpg", title.getTitle());
            resourceContent = getResourceContent(parser);

            if (level == 3) {
                assertContains(resourceContent, "/FOPForm:1");
                assertContains(resourceContent, "/DCTDecode filter");
                assertContains(resourceContent, "/ReusableStreamDecode filter");
            } else {
                assertContains(resourceContent, "/FOPForm:1");
                assertContains(resourceContent, "/DCTDecode filter");
                assertAbsent(resourceContent, "/ReusableStreamDecode filter");
            }

        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    private void assertContains(String text, String searchString) {
        assertTrue("Text doesn't contain '" + searchString + "'", text.indexOf(searchString) >= 0);
    }

    private void assertAbsent(String text, String searchString) {
        assertTrue("Text contains '" + searchString + "'", text.indexOf(searchString) < 0);
    }

    private String getResourceContent(DSCParser parser) throws IOException, DSCException {
        StringBuffer sb = new StringBuffer();
        while (parser.hasNext()) {
            DSCEvent event = parser.nextEvent();
            if (event.isLine()) {
                sb.append(event.asLine().getLine()).append('\n');
            } else if (event.isDSCComment()) {
                if (DSCConstants.END_RESOURCE.equals(event.asDSCComment().getName())) {
                    break;
                }
            }
        }
        return sb.toString();
    }

}
