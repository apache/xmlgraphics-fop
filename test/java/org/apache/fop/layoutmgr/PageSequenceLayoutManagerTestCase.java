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

/* $Id:$ */

package org.apache.fop.layoutmgr;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.area.PageViewport;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Region;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.SimplePageMaster;
import org.junit.Test;

public class PageSequenceLayoutManagerTestCase {

    private static final String MAIN_FLOW_NAME = "main";
    private static final String EMPTY_FLOW_NAME = "empty";

    /**
     * Blank pages can be created from empty pages
     *
     * @throws Exception
     */
    @Test
    public void testGetNextPageBlank() throws Exception {

        final Page expectedPage = createPageForRegionName(EMPTY_FLOW_NAME);
        final Page[] providedPages = new Page[]{expectedPage};

        testGetNextPage(providedPages, expectedPage, true);
    }

    /**
     * Empty pages should not be provided by the PageSequenceLayoutManager
     * to layout the main flow
     *
     * @throws Exception
     */
    @Test
    public void testGetNextPageFirstEmpty() throws Exception {

        final Page emptyPage = createPageForRegionName(EMPTY_FLOW_NAME);
        final Page expectedPage = createPageForRegionName(MAIN_FLOW_NAME);
        final Page[] providedPages = new Page[]{emptyPage, expectedPage};

        testGetNextPage(providedPages, expectedPage, false);
    }

    private void testGetNextPage(final Page[] providedPages, Page expectedPage, boolean isBlank) {

        final Flow flow = mock(Flow.class);
        final PageSequence pseq = mock(PageSequence.class);
        final Root root = mock(Root.class);
        final AreaTreeHandler ath = mock(AreaTreeHandler.class);

        when(flow.getFlowName()).thenReturn(MAIN_FLOW_NAME);
        when(pseq.getMainFlow()).thenReturn(flow);
        when(pseq.getRoot()).thenReturn(root);

        PageSequenceLayoutManager sut = new PageSequenceLayoutManager(ath, pseq) {

            @Override
            protected Page createPage(int i, boolean b) {
                return providedPages[i - 1];
            }

            @Override
            protected void finishPage() {
                //nop
            }

            // Expose the protected method for testing
            public Page makeNewPage(boolean isBlank) {
                return super.makeNewPage(isBlank);
            }
        };

        assertEquals(expectedPage, sut.makeNewPage(isBlank));
    }


    private static Page createPageForRegionName(final String regionName) {
        final Page page = mock(Page.class);
        final SimplePageMaster spm = mock(SimplePageMaster.class);
        final PageViewport pageViewport = mock(PageViewport.class);
        final Region region = mock(Region.class);

        when(page.getSimplePageMaster()).thenReturn(spm);
        when(page.getPageViewport()).thenReturn(pageViewport);
        when(spm.getRegion(anyInt())).thenReturn(region);

        when(region.getRegionName()).thenReturn(regionName);

        return page;
    }
}
