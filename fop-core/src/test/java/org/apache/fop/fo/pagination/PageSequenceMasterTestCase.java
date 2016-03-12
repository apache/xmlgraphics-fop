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
import org.xml.sax.Locator;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

/**
 * Unit Test for PageSequenceMaster
 *
 */
public class PageSequenceMasterTestCase {

    /**
     * Test that block level events are produced in line with
     *  XSL:FO - 6.4.8 fo:page-sequence-master -
     * "It is an error if the entire sequence of sub-sequence-specifiers children is exhausted
     *  while some areas returned by an fo:flow are not placed. Implementations may recover,
     *  if possible, by re-using the sub-sequence-specifier that was last used to generate a page."
     *
     * @throws Exception exception
     */
    @Test
     public void testGetNextSimplePageMasterExhausted() throws Exception {

         //Test when the last sub-sequence specifier is not repeatable
        testGetNextSimplePageMasterExhausted(true);

         //Test when the last sub-sequence specifier is repeatable
        testGetNextSimplePageMasterExhausted(false);

     }

     private void testGetNextSimplePageMasterExhausted(boolean canResume) throws Exception {

         SimplePageMaster spm = mock(SimplePageMaster.class);
         SubSequenceSpecifier mockSinglePageMasterReference
                 = mock(SubSequenceSpecifier.class);
         BlockLevelEventProducer mockBlockLevelEventProducer = mock(BlockLevelEventProducer.class);

         // subject under test
         PageSequenceMaster pageSequenceMaster = createPageSequenceMaster(
                 mockBlockLevelEventProducer);
         pageSequenceMaster.addSubsequenceSpecifier(mockSinglePageMasterReference);

         //Setup to mock the exhaustion of the last sub-sequence specifier
         when(mockSinglePageMasterReference.getNextPageMaster(anyBoolean(), anyBoolean(),
                 anyBoolean(), anyBoolean())).thenReturn(null, spm);

         //Need this for the method to return normally
         when(mockSinglePageMasterReference.canProcess(anyString())).thenReturn(true);

         when(mockSinglePageMasterReference.isReusable()).thenReturn(canResume);

         pageSequenceMaster.getNextSimplePageMaster(false, false, false, false, null);

         verify(mockBlockLevelEventProducer).pageSequenceMasterExhausted((Locator)anyObject(),
                 anyString(), eq(canResume), (Locator)anyObject());
     }

     /**
      * Test that PageProductionException is thrown if the final simple-page-master
      * cannot handle the main-flow of the page sequence
      * @throws Exception exception
      */
    @Test
      public void testGetNextSimplePageMasterException() throws Exception {

          final String mainFlowRegionName = "main";
          final String emptyFlowRegionName = "empty";

          //  This will represent a page master that does not map to the main flow
          //  of the page sequence
          SimplePageMaster mockEmptySPM = mock(SimplePageMaster.class);
          Region mockRegion = mock(Region.class);
          SinglePageMasterReference mockSinglePageMasterReference
                  = mock(SinglePageMasterReference.class);
          BlockLevelEventProducer mockBlockLevelEventProducer = mock(BlockLevelEventProducer.class);

          LayoutMasterSet mockLayoutMasterSet = mock(LayoutMasterSet.class);
          //The layout master set should return the empty page master
          when(mockLayoutMasterSet.getSimplePageMaster(anyString())).thenReturn(mockEmptySPM);
          when(mockEmptySPM.getRegion(anyInt())).thenReturn(mockRegion);

          when(mockRegion.getRegionName()).thenReturn(emptyFlowRegionName);

          when(mockSinglePageMasterReference.getNextPageMaster(anyBoolean(), anyBoolean(),
                  anyBoolean(), anyBoolean()))
                  .thenReturn(null, mockEmptySPM);

          PageSequenceMaster pageSequenceMaster = createPageSequenceMaster(mockLayoutMasterSet,
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


     private PageSequenceMaster createPageSequenceMaster(
             BlockLevelEventProducer blockLevelEventProducer) throws FOPException {

         return createPageSequenceMaster(mock(LayoutMasterSet.class), blockLevelEventProducer);
     }

     private PageSequenceMaster createPageSequenceMaster(LayoutMasterSet layoutMasterSet,
             BlockLevelEventProducer blockLevelEventProducer) throws FOPException {
         FONode mockParent = mock(FONode.class);
         Root mockRoot = mock(Root.class);

         //Stub generic components
         when(mockParent.getRoot()).thenReturn(mockRoot);
         when(mockRoot.getLayoutMasterSet()).thenReturn(layoutMasterSet);

         PageSequenceMaster psm =  new PageSequenceMaster(mockParent, blockLevelEventProducer);
         psm.startOfNode();

         return psm;
     }

}

