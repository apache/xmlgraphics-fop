/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 * A filter to deflate a stream. Note that the attributes for
 * prediction, colors, bitsPerComponent, and columns are not supported
 * when this filter is used to handle the data compression. They are
 * only valid for externally encoded data such as that from a graphics
 * file.
 */
public class FlateFilter extends PDFFilter {

    public static final int PREDICTION_NONE = 1;
    public static final int PREDICTION_TIFF2 = 2;
    public static final int PREDICTION_PNG_NONE = 10;
    public static final int PREDICTION_PNG_SUB = 11;
    public static final int PREDICTION_PNG_UP = 12;
    public static final int PREDICTION_PNG_AVG = 13;
    public static final int PREDICTION_PNG_PAETH = 14;
    public static final int PREDICTION_PNG_OPT = 15;


    private int _predictor = PREDICTION_NONE;
    private int _colors;
    private int _bitsPerComponent;
    private int _columns;

    public String getName() {
        return "/FlateDecode";
    }

    public String getDecodeParms() {
        if (_predictor > PREDICTION_NONE) {
            StringBuffer sb = new StringBuffer();
            sb.append("<< /Predictor ");
            sb.append(_predictor);
            if (_colors > 0) {
                sb.append(" /Colors " + _colors);
            }
            if (_bitsPerComponent > 0) {
                sb.append(" /BitsPerComponent " + _bitsPerComponent);
            }
            if (_columns > 0) {
                sb.append(" /Columns " + _columns);
            }
            sb.append(" >> ");
            return sb.toString();
        }
        return null;
    }


    /**
     * Encode the given data and return it. Note: a side effect of
     * this method is that it resets the prediction to the default
     * because these attributes are not supported. So the DecodeParms
     * should be retrieved after calling this method.
     */
    public byte[] encode(byte[] data) {
        ByteArrayOutputStream outArrayStream = new ByteArrayOutputStream();
        _predictor = PREDICTION_NONE;
        try {
            DeflaterOutputStream compressedStream =
                new DeflaterOutputStream(outArrayStream);
            compressedStream.write(data, 0, data.length);
            compressedStream.flush();
            compressedStream.close();
        } catch (IOException e) {
            //log.error("Fatal error: "
            //        + e.getMessage(), e);
        }

        return outArrayStream.toByteArray();
    }

    public void setPredictor(int predictor) throws PDFFilterException {
        _predictor = predictor;

    }

    public int getPredictor() {
        return _predictor;
    }


    public void setColors(int colors) throws PDFFilterException {
        if (_predictor != PREDICTION_NONE) {
            _colors = colors;
        } else {
            throw new PDFFilterException("Prediction must not be PREDICTION_NONE in order to set Colors");
        }
    }

    public int getColors() {
        return _colors;
    }


    public void setBitsPerComponent(int bits) throws PDFFilterException {
        if (_predictor != PREDICTION_NONE) {
            _bitsPerComponent = bits;
        } else {
            throw new PDFFilterException("Prediction must not be PREDICTION_NONE in order to set bitsPerComponent");
        }
    }

    public int getBitsPerComponent() {
        return _bitsPerComponent;
    }


    public void setColumns(int columns) throws PDFFilterException {
        if (_predictor != PREDICTION_NONE) {
            _columns = columns;
        } else {
            throw new PDFFilterException("Prediction must not be PREDICTION_NONE in order to set Columns");
        }
    }

    public int getColumns() {
        return _columns;
    }


}
