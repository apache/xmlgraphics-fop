/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */


// Author:       Eric SCHAEFFER, Kelly A. Campbell
// Description:  represent a PDF filter object

package org.apache.fop.pdf;

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
    private boolean _applied = false;

    public boolean isApplied() {
        return _applied;
    }

    /**
     * Set the applied attribute to the given value. This attribute is
     * used to determine if this filter is just a placeholder for the
     * decodeparms and dictionary entries, or if the filter needs to
     * actually encode the data. For example if the raw data is copied
     * out of an image file in it's compressed format, then this
     * should be set to true and the filter options should be set to
     * those which the raw data was encoded with.
     */
    public void setApplied(boolean b) {
        _applied = b;
    }


    /**
     * return a PDF string representation of the filter, e.g. /FlateDecode
     */
    public abstract String getName();

    /**
     * return a parameter dictionary for this filter, or null
     */
    public abstract String getDecodeParms();

    /**
     * encode the given data with the filter
     */
    public abstract byte[] encode(byte[] data);



}
