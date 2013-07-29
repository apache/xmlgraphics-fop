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

package org.apache.fop.afp.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple compact data structure to model a sparse array
 */
class IntegerKeyStore<T> {

    private static final int RANGE_BIT_SIZE = 8;

    private static final int RANGE_SIZE = 1 << RANGE_BIT_SIZE;

    private final Map<Integer, ArrayList<T>> arrays = new HashMap<Integer, ArrayList<T>>();

    /**
     *
     * @param index a positive integer
     * @param value value to store
     */
    public void put(Integer index, T value) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        int rangeKey = index >> RANGE_BIT_SIZE;
        int rangeIndex = index % RANGE_SIZE;
        ArrayList<T> range = arrays.get(rangeKey);
        if (range == null) {
            range = new ArrayList<T>(Collections.<T>nCopies(RANGE_SIZE, null));
            arrays.put(rangeKey, range);
        }
        range.set(rangeIndex, value);
    }

    /**
     *
     * @param index a positive integer
     * @return value the value associated with the index or null
     */
    public T get(Integer index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        int rangeKey = index >> RANGE_BIT_SIZE;
        int rangeIndex = index % RANGE_SIZE;
        ArrayList<T> range = arrays.get(rangeKey);
        return range == null ? null : range.get(rangeIndex);
    }
}
