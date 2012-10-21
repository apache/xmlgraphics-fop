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

import org.apache.fop.complexscripts.util.NumberConverter;

// CSOFF: LineLengthCheck

/**
 * <p>This class uses the 'format', 'groupingSeparator', 'groupingSize',
 * and 'letterValue' properties on fo:page-sequence to return a String
 * corresponding to the supplied integer page number.</p>
 *
 * <p>In addition, (now) uses 'language' parameter and new 'fox:page-number-features'
 * parameter to express applicable language and number conversion features.</p>
 *
 * <p>This work was authored by Glenn Adams (gadams@apache.org), based on a
 * rewrite of prior work to use the new <code>NumberConverter</code> utility class.</p>
 * @see NumberConverter
 */
public class PageNumberGenerator {

    private NumberConverter converter;

    /**
     * Main constructor. For further information on the parameters see {@link NumberConverter}.
     * @param format format for the page number (may be null or empty, which is treated as null)
     * @param groupingSeparator grouping separator (if zero, then no grouping separator applies)
     * @param groupingSize grouping size (if zero or negative, then no grouping size applies)
     * @param letterValue letter value
     * @param features features (feature sub-parameters)
     * @param language (may be null or empty, which is treated as null)
     * @param country (may be null or empty, which is treated as null)
     */
    public PageNumberGenerator ( String format, int groupingSeparator, int groupingSize, int letterValue, String features, String language, String country ) {
        this.converter = new NumberConverter ( format, groupingSeparator, groupingSize, letterValue, features, language, country );
    }

    /**
     * Formats a page number.
     * @param number page number to format
     * @return the formatted page number as a String
     */
    public String makeFormattedPageNumber ( int number ) {
        return converter.convert ( number );
    }

}

