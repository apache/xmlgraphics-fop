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

/**
 * Class representing a PDF /ParentTree.
 */
public class PDFParentTree extends PDFNumberTreeNode {

    private static final int MAX_NUMS_ARRAY_SIZE = 50;

    public PDFParentTree() {
        put("Kids", new PDFArray());
    }

    @Override
    public void addToNums(int num, Object object) {
        int arrayIndex = num / MAX_NUMS_ARRAY_SIZE;
        setNumOfKidsArrays(arrayIndex + 1);
        insertItemToNumsArray(arrayIndex, num, object);
    }

    private void setNumOfKidsArrays(int numKids) {
        for (int i = getKids().length(); i < numKids; i++) {
            PDFNumberTreeNode newArray = new PDFNumberTreeNode();
            newArray.setNums(new PDFNumsArray(newArray));
            newArray.setLowerLimit(i * MAX_NUMS_ARRAY_SIZE);
            newArray.setUpperLimit(i * MAX_NUMS_ARRAY_SIZE);
            addKid(newArray);
        }
    }

    /**
     * Registers a child object and adds it to the Kids array.
     * @param kid The child PDF object to be added
     */
    private void addKid(PDFObject kid) {
        assert getDocument() != null;
        getDocument().assignObjectNumber(kid);
        getDocument().addTrailerObject(kid);
        ((PDFArray) get("Kids")).add(kid);
    }

    private void insertItemToNumsArray(int array, int num, Object object) {
        assert getKids().get(array) instanceof PDFNumberTreeNode;
        PDFNumberTreeNode numsArray = (PDFNumberTreeNode) getKids().get(array);
        numsArray.addToNums(num, object);
    }
}




