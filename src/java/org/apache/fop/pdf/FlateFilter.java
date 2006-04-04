/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.OutputStream;
import java.io.IOException;

import org.apache.xmlgraphics.util.io.FlateEncodeOutputStream;

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
