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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class provides a cache for {@link DecimalFormat} instance. {@link DecimalFormat} itself
 * is not thread-safe but since FOP needs to format a lot of numbers the same way, it shall
 * be cached in a {@link ThreadLocal}.
 */
public final class DecimalFormatCache {

    private DecimalFormatCache() {
    }

    private static final String BASE_FORMAT = "0.################";

    private static class DecimalFormatThreadLocal extends ThreadLocal {

        private int dec;

        public DecimalFormatThreadLocal(int dec) {
            this.dec = dec;
        }

        protected synchronized Object initialValue() {
            String s = "0";
            if (dec > 0) {
                s = BASE_FORMAT.substring(0, dec + 2);
            }
            DecimalFormat df = new DecimalFormat(s, new DecimalFormatSymbols(Locale.US));
            return df;
        }
    };

    //DecimalFormat is not thread-safe!
    private static final ThreadLocal[] DECIMAL_FORMAT_CACHE = new DecimalFormatThreadLocal[17];
    static {
        for (int i = 0, c = DECIMAL_FORMAT_CACHE.length; i < c; i++) {
            DECIMAL_FORMAT_CACHE[i] = new DecimalFormatThreadLocal(i);
        }
    }

    /**
     * Returns a cached {@link DecimalFormat} instance for the given number of decimal digits.
     * @param dec the number of decimal digits.
     * @return the DecimalFormat instance
     */
    public static DecimalFormat getDecimalFormat(int dec) {
        if (dec < 0 || dec >= DECIMAL_FORMAT_CACHE.length) {
            throw new IllegalArgumentException("Parameter dec must be between 1 and "
                    + (DECIMAL_FORMAT_CACHE.length + 1));
        }
        return (DecimalFormat)DECIMAL_FORMAT_CACHE[dec].get();
    }

}
