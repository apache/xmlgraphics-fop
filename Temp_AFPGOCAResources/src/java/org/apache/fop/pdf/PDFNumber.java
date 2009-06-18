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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * This class represents a simple number object. It also contains contains some
 * utility methods for outputing numbers to PDF.
 */
public class PDFNumber extends PDFObject {

    private Number number;

    /**
     * Returns the number.
     * @return the number
     */
    public Number getNumber() {
        return this.number;
    }

    /**
     * Sets the number.
     * @param number the number
     */
    public void setNumber(Number number) {
        this.number = number;
    }

    /**
     * Output a Double value to a string suitable for PDF.
     *
     * @param doubleDown the Double value
     * @return the value as a string
     */
    public static String doubleOut(Double doubleDown) {
        return doubleOut(doubleDown.doubleValue());
    }

    /**
     * Output a double value to a string suitable for PDF (6 decimal digits).
     *
     * @param doubleDown the double value
     * @return the value as a string
     */
    public static String doubleOut(double doubleDown) {
        return doubleOut(doubleDown, 6);
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
     * Output a double value to a string suitable for PDF.
     * In this method it is possible to set the maximum
     * number of decimal places to output.
     *
     * @param doubleDown the Double value
     * @param dec the number of decimal places to output
     * @return the value as a string
     */
    public static String doubleOut(double doubleDown, int dec) {
        if (dec < 0 || dec >= DECIMAL_FORMAT_CACHE.length) {
            throw new IllegalArgumentException("Parameter dec must be between 1 and "
                    + (DECIMAL_FORMAT_CACHE.length + 1));
        }
        DecimalFormat df = (DecimalFormat)DECIMAL_FORMAT_CACHE[dec].get();
        return df.format(doubleDown);
    }

    /** {@inheritDoc} */
    protected String toPDFString() {
        if (getNumber() == null) {
            throw new IllegalArgumentException(
                "The number of this PDFNumber must not be empty");
        }
        StringBuffer sb = new StringBuffer(64);
        if (hasObjectNumber()) {
            sb.append(getObjectID());
        }
        sb.append(getNumber().toString());
        if (hasObjectNumber()) {
            sb.append("\nendobj\n");
        }
        return sb.toString();
    }

}

