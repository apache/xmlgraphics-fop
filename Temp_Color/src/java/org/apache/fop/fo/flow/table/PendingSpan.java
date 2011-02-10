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

package org.apache.fop.fo.flow.table;

/**
 * Used for determining initial values for column-numbers in case of row-spanning cells.
 */
class PendingSpan {

    /**
     * member variable holding the number of rows left
     */
    private int rowsLeft;

    /**
     * Constructor
     *
     * @param rows  number of rows spanned
     */
    public PendingSpan(int rows) {
        rowsLeft = rows;
    }

    /** @return number of rows spanned */
    public int getRowsLeft() {
        return rowsLeft;
    }

    /**
     * Decrement rows spanned.
     * @return number of rows spanned after decrementing
     */
    public int decrRowsLeft() {
        if ( rowsLeft > 0 ) {
            return --rowsLeft;
        } else {
            return 0;
        }
    }

}