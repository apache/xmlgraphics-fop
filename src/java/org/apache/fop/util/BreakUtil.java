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

import org.apache.fop.fo.Constants;

/**
 * A utility class for manipulating break classes (the break-before and break-after properties).
 */
public final class BreakUtil {

    private BreakUtil() { }

    // TODO replace that with a proper 1.5 enumeration ASAP
    private static int getBreakClassPriority(int breakClass) {
        switch (breakClass) {
        case Constants.EN_AUTO:      return 0;
        case Constants.EN_COLUMN:    return 1;
        case Constants.EN_PAGE:      return 2;
        case Constants.EN_EVEN_PAGE: return 3;
        case Constants.EN_ODD_PAGE:  return 3;
        default: throw new IllegalArgumentException();
        }
    }

    /**
     * Compares the given break classes and return the one that wins. even-page and
     * odd-page win over page, which wins over column, which wins over auto. If even-page
     * and odd-page are compared to each other, which one will be returned is undefined.
     *
     * @param break1 a break class, one of {@link Constants#EN_AUTO},
     * {@link Constants#EN_COLUMN}, {@link Constants#EN_PAGE},
     * {@link Constants#EN_EVEN_PAGE}, {@link Constants#EN_ODD_PAGE}
     * @param break2 another break class
     * @return the break class that wins the comparison
     */
    public static int compareBreakClasses(int break1, int break2) {
        // TODO implement some warning mechanism if even-page and odd-page are being compared
        int p1 = getBreakClassPriority(break1);
        int p2 = getBreakClassPriority(break2);
        if (p1 < p2) {
            return break2;
        } else {
            return break1;
        }
    }

}
