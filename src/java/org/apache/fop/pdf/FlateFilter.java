/*
 * $Id: FlateFilter.java,v 1.7 2003/03/07 08:25:46 jeremias Exp $
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.pdf;

import org.apache.fop.util.FlateEncodeOutputStream;

import java.io.OutputStream;
import java.io.IOException;

/**
 * A filter to deflate a stream.
 * <p>
 * <b>Note</b> that the attributes for
 * prediction, colors, bitsPerComponent, and columns are not supported
 * when this filter is used to handle the data compression. They are
 * only valid for externally encoded data such as that from a graphics
 * file.
 */
public class FlateFilter extends PDFFilter {
    /**
     * The supported mode when this filter is used for data compression
     */
    public static final int PREDICTION_NONE = 1;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_TIFF2 = 2;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_NONE = 10;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_SUB = 11;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_UP = 12;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_AVG = 13;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_PAETH = 14;

    /**
     * Mode for externally encoded data.
     */
    public static final int PREDICTION_PNG_OPT = 15;


    private int predictor = PREDICTION_NONE;
    private int colors;
    private int bitsPerComponent;
    private int columns;

    /**
     * Get the name of this filter.
     *
     * @return the pdf name of the flate decode filter
     */
    public String getName() {
        return "/FlateDecode";
    }

    /**
     * Get the decode params for this filter.
     *
     * @return a string containing the decode params for this filter
     */
    public String getDecodeParms() {
        if (predictor > PREDICTION_NONE) {
            StringBuffer sb = new StringBuffer();
            sb.append("<< /Predictor ");
            sb.append(predictor);
            if (colors > 0) {
                sb.append(" /Colors " + colors);
            }
            if (bitsPerComponent > 0) {
                sb.append(" /BitsPerComponent " + bitsPerComponent);
            }
            if (columns > 0) {
                sb.append(" /Columns " + columns);
            }
            sb.append(" >> ");
            return sb.toString();
        }
        return null;
    }

    /**
     * Set the predictor for this filter.
     *
     * @param predictor the predictor to use
     * @throws PDFFilterException if there is an error with the predictor
     */
    public void setPredictor(int predictor) throws PDFFilterException {
        this.predictor = predictor;

    }

    /**
     * Get the predictor for this filter.
     *
     * @return the predictor used for this filter
     */
    public int getPredictor() {
        return predictor;
    }

    /**
     * Set the colors for this filter.
     *
     * @param colors the colors to use
     * @throws PDFFilterException if predictor is not PREDICTION_NONE
     */
    public void setColors(int colors) throws PDFFilterException {
        if (predictor != PREDICTION_NONE) {
            this.colors = colors;
        } else {
            throw new PDFFilterException(
                          "Prediction must not be PREDICTION_NONE in"
                          + " order to set Colors");
        }
    }

    /**
     * Get the colors for this filter.
     *
     * @return the colors for this filter
     */
    public int getColors() {
        return colors;
    }

    /**
     * Set the number of bits per component.
     *
     * @param bits the number of bits per component
     * @throws PDFFilterException if predictor is not PREDICTION_NONE
     */
    public void setBitsPerComponent(int bits) throws PDFFilterException {
        if (predictor != PREDICTION_NONE) {
            bitsPerComponent = bits;
        } else {
            throw new PDFFilterException(
                         "Prediction must not be PREDICTION_NONE in order"
                         + " to set bitsPerComponent");
        }
    }

    /**
     * Get the number of bits per component.
     *
     * @return the number of bits per component
     */
    public int getBitsPerComponent() {
        return bitsPerComponent;
    }

    /**
     * Set the number of columns for this filter.
     *
     * @param columns the number of columns to use for the filter
     * @throws PDFFilterException if predictor is not PREDICTION_NONE
     */
    public void setColumns(int columns) throws PDFFilterException {
        if (predictor != PREDICTION_NONE) {
            this.columns = columns;
        } else {
            throw new PDFFilterException(
                      "Prediction must not be PREDICTION_NONE in"
                      + " order to set Columns");
        }
    }

    /**
     * Get the number of columns for this filter.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }


    /**
     * @see org.apache.fop.pdf.PDFFilter#applyFilter(OutputStream)
     */
    public OutputStream applyFilter(OutputStream out) throws IOException {
        return new FlateEncodeOutputStream(out);
    }

}
