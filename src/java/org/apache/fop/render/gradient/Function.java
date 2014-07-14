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

package org.apache.fop.render.gradient;

import java.util.Collections;
import java.util.List;

import org.apache.fop.pdf.PDFNumber;

public class Function {

    /**
     * Required: The Type of function (0,2,3,4) default is 0.
     */
    private int functionType;

    /**
     * Required: 2 * m Array of Double numbers which are possible inputs to the function
     */
    private List<Double> domain;

    /**
     * Required: 2 * n Array of Double numbers which are possible outputs to the function
     */
    private List<Double> range;

    /* ********************TYPE 0***************************** */
    // FunctionType 0 specific function guts

    /**
     * Required: Array containing the Integer size of the Domain and Range, respectively.
     * Note: This is really more like two seperate integers, sizeDomain, and sizeRange,
     * but since they're expressed as an array in PDF, my implementation reflects that.
     */
    protected List<Double> size;

    /**
     * Required for Type 0: Number of Bits used to represent each sample value.
     * Limited to 1,2,4,8,12,16,24, or 32
     */
    private int bitsPerSample = 1;

    /**
     * Optional for Type 0: order of interpolation between samples.
     * Limited to linear (1) or cubic (3). Default is 1
     */
    private int order = 1;

    /**
     * Optional for Type 0: A 2 * m array of Doubles which provides a
     * linear mapping of input values to the domain.
     *
     * Required for Type 3: A 2 * k array of Doubles that, taken
     * in pairs, map each subset of the domain defined by Domain
     * and the Bounds array to the domain of the corresponding function.
     * Should be two values per function, usually (0,1),
     * as in [0 1 0 1] for 2 functions.
     */
    private List<Double> encode;

    /**
     * Optional for Type 0: A 2 * n array of Doubles which provides
     * a linear mapping of sample values to the range. Defaults to Range.
     */
    private List<Double> decode;

    /**
     * Optional For Type 0: A stream of sample values
     */

    /**
     * Required For Type 4: Postscript Calculator function
     * composed of arithmetic, boolean, and stack operators + boolean constants
     */
    private StringBuffer functionDataStream;

    /**
     * Required (possibly) For Type 0: A vector of Strings for the
     * various filters to be used to decode the stream.
     * These are how the string is compressed. Flate, LZW, etc.
     */
    private List<String> filter;
    /* *************************TYPE 2************************** */

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=0. Default is [0].
     */
    private float[] cZero;

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=1. Default is [1].
     */
    private float[] cOne;

    /**
     * Required for Type 2: The interpolation exponent.
     * Each value x will return n results.
     * Must be greater than 0.
     */
    private double interpolationExponentN = 1;

    /* *************************TYPE 3************************** */

    /**
     * Required for Type 3: An vector of PDFFunctions which
     * form an array of k single input functions making up
     * the stitching function.
     */
    private List<Function> functions;

    /**
     * Optional for Type 3: An array of (k-1) Doubles that,
     * in combination with Domain, define the intervals to which
     * each function from the Functions array apply. Bounds
     * elements must be in order of increasing magnitude,
     * and each value must be within the value of Domain.
     * k is the number of functions.
     * If you pass null, it will output (1/k) in an array of k-1 elements.
     * This makes each function responsible for an equal amount of the stitching function.
     * It makes the gradient even.
     */
    private List<Double> bounds;

    /**
     * create an complete Function object of Type 2, an Exponential Interpolation function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     * @param functionType The type of the function, which should be 2.
     * @param domain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param range List of Doubles that is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param cZero This is a vector of Double objects which defines the function result
     * when x=0.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param cOne This is a vector of Double objects which defines the function result
     * when x=1.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param interpolationExponentN This is the inerpolation exponent.
     *
     * This attribute is required.
     * PDF Spec page 268
     */
    public Function(int functionType, List<Double> domain, List<Double> range,
                       float[] cZero, float[] cOne, double interpolationExponentN) {
        this.functionType = 2;    // dang well better be 2;

        this.cZero = cZero;
        this.cOne = cOne;
        this.interpolationExponentN = interpolationExponentN;

        this.domain = domain;
        this.range = range;

    }

    /**
     * create an complete Function object of Type 3, a Stitching function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     * @param functionType This is the function type. It should be 3,
     * for a stitching function.
     * @param domain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param range List objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param functions A List of the PDFFunction objects that the stitching function stitches.
     *
     * This attributed is required.
     * It is described on page 269 of the PDF spec.
     * @param bounds This is a vector of Doubles representing the numbers that,
     * in conjunction with Domain define the intervals to which each function from
     * the 'functions' object applies. It must be in order of increasing magnitude,
     * and each must be within Domain.
     *
     * It basically sets how much of the gradient each function handles.
     *
     * This attributed is required.
     * It's described on page 269 of the PDF 1.3 spec.
     * @param encode List objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is required.
     *
     * See page 270 in the PDF 1.3 spec.
     */
    public Function(int functionType, List<Double> domain, List<Double> range,
                       List<Function> functions, List<Double> bounds,
                       List<Double> encode) {
        this.functionType = 3;    // dang well better be 3;

        this.functions = functions;
        this.bounds = bounds;
        this.encode = encode;
        this.domain = domain;
        this.range = range;

    }

    /**
     * Gets the function type
     */
    public int getFunctionType() {
        return functionType;
    }

    /**
     * Gets the function bounds
     */
    public List<Double> getBounds() {
        return bounds;
    }

    /**
     * The function domain
     */
    public List<Double> getDomain() {
        return domain;
    }

    /**
     * The function size
     */
    public List<Double> getSize() {
        return size;
    }

    /**
     * Gets the function encoding
     */
    public List<Double> getEncode() {
        return encode;
    }

    /**
     * Gets the sub-functions
     */
    public List<Function> getFunctions() {
        if (functions == null) {
            return Collections.emptyList();
        } else {
            return functions;
        }
    }

    /**
     * Gets the function filter
     */
    public List<String> getFilter() {
        return filter;
    }

    /**
     * Gets the bits per sample of the function
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * Gets the interpolation exponent of the function
     */
    public double getInterpolationExponentN() {
        return interpolationExponentN;
    }

    /**
     * Gets the function order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Gets the function range
     */
    public List<Double> getRange() {
        return range;
    }

    /**
     * Gets the function decoding
     */
    public List<Double> getDecode() {
        return decode;
    }

    /**
     * Gets the function data stream
     */
    public StringBuffer getDataStream() {
        return functionDataStream;
    }

    /**
     * Gets the function C0 value (color for gradient)
     */
    public float[] getCZero() {
        return cZero;
    }

    /**
     * Gets the function C1 value (color for gradient)
     */
    public float[] getCOne() {
        return cOne;
    }

    public String toWriteableString(List<String> functionsStrings) {
        StringBuilder out = new StringBuilder(256);
        out.append("<<\n/FunctionType " + functionType + "\n");
        outputDomain(out);
        if (this.functionType == 0) {
            outputSize(out);
            outputEncode(out);
            outputBitsPerSample(out);
            outputOrder(out);
            outputRange(out);
            outputDecode(out);
            if (functionDataStream != null) {
                out.append("/Length " + (functionDataStream.length() + 1) + "\n");
            }
            outputFilter(out);
            out.append(">>");
            if (functionDataStream != null) {
                out.append("\nstream\n" + functionDataStream + "\nendstream");
            }
        } else if (functionType == 2) {
            outputRange(out);
            outputCZero(out);
            outputCOne(out);
            outputInterpolationExponentN(out);
            out.append(">>");
        } else if (functionType == 3) {
            outputRange(out);
            if (!functions.isEmpty()) {
                out.append("/Functions [ ");
                for (String f : functionsStrings) {
                    out.append(f);
                    out.append(' ');
                }
                out.append("]\n");
            }
            outputEncode(out);
            out.append("/Bounds ");
            if (bounds != null) {
                outputDoubles(out, bounds);
            } else if (!functions.isEmpty()) {
                // if there are n functions,
                // there must be n-1 bounds.
                // so let each function handle an equal portion
                // of the whole. e.g. if there are 4, then [ 0.25 0.25 0.25 ]
                int numberOfFunctions = functions.size();
                String functionsFraction = PDFNumber.doubleOut(Double.valueOf(1.0 / (numberOfFunctions)));
                out.append("[ ");
                for (int i = 0; i + 1 < numberOfFunctions; i++) {
                    out.append(functionsFraction);
                    out.append(" ");
                }
                out.append("]");
            }
            out.append("\n>>");
        } else if (functionType == 4) {
            outputRange(out);
            if (functionDataStream != null) {
                out.append("/Length " + (functionDataStream.length() + 1) + "\n");
            }
            out.append(">>");
            if (functionDataStream != null) {
                out.append("\nstream\n{ " + functionDataStream + " }\nendstream");
            }
        }
        return out.toString();
    }

    private void outputDomain(StringBuilder p) {
        if (domain != null) {
            p.append("/Domain ");
            outputDoubles(p, domain);
            p.append("\n");
        } else {
            p.append("/Domain [ 0 1 ]\n");
        }
    }

    private void outputSize(StringBuilder out) {
        if (size != null) {
            out.append("/Size ");
            outputDoubles(out, size);
            out.append("\n");
        }
    }

    private void outputBitsPerSample(StringBuilder out) {
        out.append("/BitsPerSample " + bitsPerSample + "\n");
    }

    private void outputOrder(StringBuilder out) {
        if (order == 1 || order == 3) {
            out.append("\n/Order " + order + "\n");
        }
    }

    private void outputRange(StringBuilder out) {
        if (range != null) {
            out.append("/Range ");
            outputDoubles(out, range);
            out.append("\n");
        }
    }

    private void outputEncode(StringBuilder out) {
        if (encode != null) {
            out.append("/Encode ");
            outputDoubles(out, encode);
            out.append("\n");
        } else {
            out.append("/Encode [ ");
            int size = functions.size();
            for (int i = 0; i < size; i++) {
                out.append("0 1 ");
            }
            out.append("]\n");
        }
    }

    private void outputDecode(StringBuilder out) {
        if (decode != null) {
            out.append("/Decode ");
            outputDoubles(out, decode);
            out.append("\n");
        }
    }

    private void outputFilter(StringBuilder out) {
        if (filter != null) {
            int size = filter.size();
            out.append("/Filter ");
            if (size == 1) {
                out.append("/" + filter.get(0) + "\n");
            } else {
                out.append("[ ");
                for (int i = 0; i < size; i++) {
                    out.append("/" + filter.get(0) + " ");
                }
                out.append("]\n");
            }
        }
    }

    private void outputCZero(StringBuilder out) {
        if (cZero != null) {
            out.append("/C0 [ ");
            for (float c : cZero) {
                out.append(PDFNumber.doubleOut(c));
                out.append(" ");
            }
            out.append("]\n");
        }
    }

    private void outputCOne(StringBuilder out) {
        if (cOne != null) {
            out.append("/C1 [ ");
            for (float c : cOne) {
                out.append(PDFNumber.doubleOut(c));
                out.append(" ");
            }
            out.append("]\n");
        }
    }

    private void outputInterpolationExponentN(StringBuilder out) {
        out.append("/N ");
        out.append(PDFNumber.doubleOut(Double.valueOf(interpolationExponentN)));
        out.append("\n");
    }

    private void outputDoubles(StringBuilder out, List<Double> doubles) {
        out.append("[ ");
        for (Double d : doubles) {
            out.append(PDFNumber.doubleOut(d));
            out.append(" ");
        }
        out.append("]");
    }

}
