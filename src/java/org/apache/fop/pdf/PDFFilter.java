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

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>PDF Filter class.
 * This class represents a PDF filter object.
 * Filter implementations should extend this class.</p>
 *
 * <p>This work was authored by Eric Schaeffer and Kelly A. Campbell.</p>
 */
public abstract class PDFFilter {
    /*
     * These are no longer needed, but are here as a reminder about what
     * filters pdf supports.
     * public static final int ASCII_HEX_DECODE = 1;
     * public static final int ASCII_85_DECODE = 2;
     * public static final int LZW_DECODE = 3;
     * public static final int RUN_LENGTH_DECODE = 4;
     * public static final int CCITT_FAX_DECODE = 5;
     * public static final int DCT_DECODE = 6;
     * public static final int FLATE_DECODE = 7;
     */

    /**
     * Marker to know if this filter has already been applied to the data
     */
    private boolean applied;

    /**
     * Check if this filter has been applied.
     *
     * @return true if this filter has been applied
     */
    public boolean isApplied() {
        return applied;
    }

    /**
     * Set the applied attribute to the given value. This attribute is
     * used to determine if this filter is just a placeholder for the
     * decodeparms and dictionary entries, or if the filter needs to
     * actually encode the data. For example if the raw data is copied
     * out of an image file in it's compressed format, then this
     * should be set to true and the filter options should be set to
     * those which the raw data was encoded with.
     *
     * @param b set the applied value to this
     */
    public void setApplied(boolean b) {
        applied = b;
    }

    /**
     * return a PDF string representation of the filter, e.g. /FlateDecode
     *
     * @return the filter PDF name
     */
    public abstract String getName();

    /**
     * Returns true if the filter is an ASCII filter that isn't necessary
     * when encryption is active.
     * @return boolean True if this filter is an ASCII filter
     */
    public boolean isASCIIFilter() {
        return false;
    }

    /**
     * return a parameter dictionary for this filter, or null
     *
     * @return the decode params for the filter
     */
    public abstract PDFObject getDecodeParms();

    /**
     * Applies a filter to an OutputStream.
     * @param out contents to be filtered
     * @return OutputStream filtered contents
     * @throws IOException In case of an I/O problem
     */
    public abstract OutputStream applyFilter(OutputStream out) throws IOException;

}
