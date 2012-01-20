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

import static org.junit.Assert.fail;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

import org.junit.Test;


/**
 * Unit Test for PageSequenceMaster
 *
 */
public class PageSequenceMasterTestCase {

   /**
    * Test that PageProductionException is thrown if the final simple-page-master
    * cannot handle the main-flow of the page sequence
    * @throws Exception exception
    */
   @Test
    public void testGetNextSimplePageMasterException() throws Exception {

        final String mainFlowRegionName = "main";
        final String emptyFlowRegionName = "empty";
        //  Create stubs

        FONode mockParent = mock(FONode.class);
        Root mockRoot = mock(Root.class);
        LayoutMasterSet mockLayoutMasterSet = mock(LayoutMasterSet.class);

        //  This will represent a page master that does not map to the main flow
        //  of the page sequence
        SimplePageMaster mockEmptySPM = mock(SimplePageMaster.class);
        Region mockRegion = mock(Region.class);
        SinglePageMasterReference mockSinglePageMasterReference
                = mock(SinglePageMasterReference.class);
        BlockLevelEventProducer mockBlockLevelEventProducer = mock(BlockLevelEventProducer.class);

        //Stub behaviour
        when(mockParent.getRoot()).thenReturn(mockRoot);
        when(mockRoot.getLayoutMasterSet()).thenReturn(mockLayoutMasterSet);

        //The layout master set should return the empty page master
        when(mockLayoutMasterSet.getSimplePageMaster(anyString())).thenReturn(mockEmptySPM);
        when(mockEmptySPM.getRegion(anyInt())).thenReturn(mockRegion);

        when(mockRegion.getRegionName()).thenReturn(emptyFlowRegionName);

        when(mockSinglePageMasterReference.getNextPageMaster(anyBoolean(), anyBoolean(),
                anyBoolean(), anyBoolean()))
                .thenReturn(null, mockEmptySPM);

        PageSequenceMaster pageSequenceMaster = new PageSequenceMaster(mockParent,
                mockBlockLevelEventProducer);
        pageSequenceMaster.startOfNode();
        pageSequenceMaster.addSubsequenceSpecifier(mockSinglePageMasterReference);

        try {
            pageSequenceMaster.getNextSimplePageMaster(false, false, false, false,
                    mainFlowRegionName);
            fail("The next simple page master does not refer to the main flow");
       } catch (PageProductionException ppe) {
           //Passed test
       }
    }

}

