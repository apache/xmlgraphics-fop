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

package org.apache.fop.fo.pagination;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.FODocumentParser;
import org.apache.fop.fo.FODocumentParser.FOEventHandlerFactory;
import org.apache.fop.fo.FOEventHandler;

public class LayoutMasterSetTestCase {

    private static class FlowMappingTester extends FOEventHandler {

        private static final String[][] FLOW_MAPPINGS = {

            {"first-page-before", "xsl-region-before"},
            {"first-page-after", "xsl-region-after"},
            {"first-page-start", "xsl-region-start"},
            {"first-page-end", "xsl-region-end"},

            {"odd-page-before", "xsl-region-before"},
            {"odd-page-after", "xsl-region-after"},
            {"odd-page-start", "xsl-region-start"},
            {"odd-page-end", "xsl-region-end"},

            {"odd-page-before", "xsl-region-before"},
            {"odd-page-after", "xsl-region-after"},
            {"odd-page-start", "xsl-region-start"},
            {"odd-page-end", "xsl-region-end"},

            {"blank-page-before", "xsl-region-before"},
            {"blank-page-after", "xsl-region-after"},
            {"blank-page-start", "xsl-region-start"},
            {"blank-page-end", "xsl-region-end"},

            {"last-page-before", "xsl-region-before"},
            {"last-page-after", "xsl-region-after"},
            {"last-page-start", "xsl-region-start"},
            {"last-page-end", "xsl-region-end"},

            {"xsl-footnote-separator", "xsl-footnote-separator"}

        };

        FlowMappingTester(FOUserAgent userAgent) {
            super(userAgent);
        }

        @Override
        public void startPageSequence(PageSequence pageSeq) {
            super.startPageSequence(pageSeq);
            LayoutMasterSet layoutMasterSet = pageSeq.getRoot().getLayoutMasterSet();
            for (String[] mapping : FLOW_MAPPINGS) {
                assertEquals(mapping[1], layoutMasterSet.getDefaultRegionNameFor(mapping[0]));
            }
        }

    }

    /**
     * Tests the {@link LayoutMasterSet#getDefaultRegionNameFor(String)} method.
     */
    @Test
    public void testFlowMapping() throws Exception {
        FODocumentParser foDocumentParser = FODocumentParser.newInstance(new FOEventHandlerFactory() {

            public FOEventHandler newFOEventHandler(FOUserAgent foUserAgent) {
                return new FlowMappingTester(foUserAgent);
            }
        });
        foDocumentParser.parse(getClass().getResourceAsStream("side-regions.fo"));
    }

}
