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

package org.apache.fop.util;

import java.util.List;

/**
 * Provides helper functions for {@link java.util.List}.
 * 
 */
public final class ListUtil {

    private ListUtil() {
        // Utility class.
    }

    /**
     * Retrieve the last element from a list.
     * 
     * @param list
     *            The list to work on
     * @return last element
     */
    public static Object getLast(List list) {
        return list.get(list.size() - 1);
    }

    /**
     * Retrieve and remove the last element from a list.
     * 
     * @param list
     *            The list to work on
     * @return previous last element
     */
    public static Object removeLast(List list) {
        return list.remove(list.size() - 1);
    }
}
