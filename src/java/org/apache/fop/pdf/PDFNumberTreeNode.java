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
 * Class representing a PDF number tree node.
 */
public class PDFNumberTreeNode extends PDFDictionary {

    private static final String KIDS = "Kids";
    private static final String NUMS = "Nums";
    private static final String LIMITS = "Limits";

    /**
     * create a named destination
     */
    public PDFNumberTreeNode() {
        /* generic creation of PDF object */
        super();
    }

    /**
     * Sets the Kids array.
     * @param kids the Kids array
     */
    public void setKids(PDFArray kids) {
        put(KIDS, kids);
    }

    /**
     * Returns the Kids array.
     * @return the Kids array
     */
    public PDFArray getKids() {
        return (PDFArray)get(KIDS);
    }

    /**
     * Sets the Nums array.
     * @param nums the Nums array
     */
    public void setNums(PDFNumsArray nums) {
        put(NUMS, nums);
    }

    /**
     * Returns the Nums array.
     * @return the Nums array
     */
    public PDFNumsArray getNums() {
        return (PDFNumsArray)get(NUMS);
    }

    /**
     * Sets the lower limit value of the Limits array.
     * @param key the lower limit value
     */
    public void setLowerLimit(Integer key) {
        PDFArray limits = prepareLimitsArray();
        limits.set(0, key);
    }

    /**
     * Returns the lower limit value of the Limits array.
     * @return the lower limit value
     */
    public Integer getLowerLimit() {
        PDFArray limits = prepareLimitsArray();
        return (Integer)limits.get(0);
    }

    /**
     * Sets the upper limit value of the Limits array.
     * @param key the upper limit value
     */
    public void setUpperLimit(Integer key) {
        PDFArray limits = prepareLimitsArray();
        limits.set(1, key);
    }

    /**
     * Returns the upper limit value of the Limits array.
     * @return the upper limit value
     */
    public Integer getUpperLimit() {
        PDFArray limits = prepareLimitsArray();
        return (Integer)limits.get(1);
    }


    private PDFArray prepareLimitsArray() {
        PDFArray limits = (PDFArray)get(LIMITS);
        if (limits == null) {
            limits = new PDFArray(this, new Object[2]);
            put(LIMITS, limits);
        }
        if (limits.length() != 2) {
            throw new IllegalStateException("Limits array must have 2 entries");
        }
        return limits;
    }

}

