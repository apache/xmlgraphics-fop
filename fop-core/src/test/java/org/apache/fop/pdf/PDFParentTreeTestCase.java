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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Tests that the nums array in the ParentTree dictionary is correctly being split into
 * separate arrays if the elements number exceeds the set limit.
 */
public class PDFParentTreeTestCase {

    private PDFParentTree parentTree;

    @Before
    public void initializeStructureTree() {
        parentTree = new PDFParentTree();
        PDFDocument pdfDocument = new PDFDocument("test");
        pdfDocument.makeStructTreeRoot(parentTree);
    }

    /**
     * Adds less structured items than the imposed limit which should result
     * in only one nums array being created.
     * @throws Exception
     */
    @Test
    public void testNoSplit() throws Exception {
        assertEquals(getArrayNumber(45), 1);
    }

    /**
     * Adds more than the imposed array limit to test that it splits the
     * nums array into two objects.
     * @throws Exception
     */
    @Test
    public void testSingleSplit() throws Exception {
        assertEquals(getArrayNumber(70), 2);
    }

    /**
     * Adds items to the nums array to cause and test that multiple splits occur
     * @throws Exception
     */
    @Test
    public void testMultipleSplit() throws Exception {
        assertEquals(getArrayNumber(165), 4);
    }

    /**
     * Ensures that items added out of order get added to the correct nums array
     * @throws Exception
     */
    @Test
    public void testOutOfOrderSplit() throws Exception {
        PDFStructElem structElem = mock(PDFStructElem.class);
        for (int num = 50; num < 53; num++) {
            parentTree.addToNums(num, structElem);
        }
        assertEquals(getArrayNumber(50), 2);
        PDFNumberTreeNode treeNode = (PDFNumberTreeNode) parentTree.getKids().get(0);
        for (int num = 0; num < 50; num++) {
            assertTrue(treeNode.getNums().map.containsKey(num));
        }
        treeNode = (PDFNumberTreeNode) parentTree.getKids().get(1);
        for (int num = 50; num < 53; num++) {
            assertTrue(treeNode.getNums().map.containsKey(num));
        }
    }

    /**
     * Gets the number of arrays created for a given number of elements
     * @param elementNumber The number of elements to be added to the nums array
     * @return Returns the number of array objects
     * @throws Exception
     */
    private int getArrayNumber(int elementNumber) throws Exception {
        PDFStructElem structElem = mock(PDFStructElem.class);
        for (int structParent = 0; structParent < elementNumber; structParent++) {
            parentTree.addToNums(structParent, structElem);
        }
        return parentTree.getKids().length();
    }
}
