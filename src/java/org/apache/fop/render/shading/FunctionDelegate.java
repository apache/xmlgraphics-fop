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

package org.apache.fop.render.shading;

import java.util.List;

public class FunctionDelegate implements Function {

    private Function parentFunction;

    /**
     * Required: The Type of function (0,2,3,4) default is 0.
     */
    protected int functionType = 0;    // Default

    /**
     * Required: 2 * m Array of Double numbers which are possible inputs to the function
     */
    protected List<Double> domain = null;

    /**
     * Required: 2 * n Array of Double numbers which are possible outputs to the function
     */
    protected List<Double> range = null;

    /* ********************TYPE 0***************************** */
    // FunctionType 0 specific function guts

    /**
     * Required: Array containing the Integer size of the Domain and Range, respectively.
     * Note: This is really more like two seperate integers, sizeDomain, and sizeRange,
     * but since they're expressed as an array in PDF, my implementation reflects that.
     */
    protected List<Double> size = null;

    /**
     * Required for Type 0: Number of Bits used to represent each sample value.
     * Limited to 1,2,4,8,12,16,24, or 32
     */
    protected int bitsPerSample = 1;

    /**
     * Optional for Type 0: order of interpolation between samples.
     * Limited to linear (1) or cubic (3). Default is 1
     */
    protected int order = 1;

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
    protected List<Double> encode = null;

    /**
     * Optional for Type 0: A 2 * n array of Doubles which provides
     * a linear mapping of sample values to the range. Defaults to Range.
     */
    protected List<Double> decode = null;

    /**
     * Optional For Type 0: A stream of sample values
     */

    /**
     * Required For Type 4: Postscript Calculator function
     * composed of arithmetic, boolean, and stack operators + boolean constants
     */
    protected StringBuffer functionDataStream = null;

    /**
     * Required (possibly) For Type 0: A vector of Strings for the
     * various filters to be used to decode the stream.
     * These are how the string is compressed. Flate, LZW, etc.
     */
    protected List<String> filter = null;
    /* *************************TYPE 2************************** */

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=0. Default is [0].
     */
    protected List<Double> cZero = null;

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=1. Default is [1].
     */
    protected List<Double> cOne = null;

    /**
     * Required for Type 2: The interpolation exponent.
     * Each value x will return n results.
     * Must be greater than 0.
     */
    protected double interpolationExponentN = 1;

    /* *************************TYPE 3************************** */

    /**
     * Required for Type 3: An vector of PDFFunctions which
     * form an array of k single input functions making up
     * the stitching function.
     */
    protected List<Function> functions = null;

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
    protected List<Double> bounds = null;

    /**
     * create an complete Function object of Type 0, A Sampled function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theSize A List object of Integer objects.
     * This is the number of samples in each input dimension.
     * I can't imagine there being more or less than two input dimensions,
     * so maybe this should be an array of length 2.
     *
     * See page 265 of the PDF 1.3 Spec.
     * @param theBitsPerSample An int specifying the number of bits
                               used to represent each sample value.
     * Limited to 1,2,4,8,12,16,24 or 32.
     * See page 265 of the 1.3 PDF Spec.
     * @param theOrder The order of interpolation between samples. Default is 1 (one). Limited
     * to 1 (one) or 3, which means linear or cubic-spline interpolation.
     *
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theEncode List objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is optional.
     *
     * See page 265 in the PDF 1.3 spec.
     * @param theDecode List objects of Double objects.
     * This is a linear mapping of sample values into the range.
     * The default is just the range.
     *
     * This attribute is optional.
     * Read about it on page 265 of the PDF 1.3 spec.
     * @param theFunctionDataStream The sample values that specify
     *                     the function are provided in a stream.
     *
     * This is optional, but is almost always used.
     *
     * Page 265 of the PDF 1.3 spec has more.
     * @param theFilter This is a vector of String objects which are the various filters that
     * have are to be applied to the stream to make sense of it. Order matters,
     * so watch out.
     *
     * This is not documented in the Function section of the PDF 1.3 spec,
     * it was deduced from samples that this is sometimes used, even if we may never
     * use it in FOP. It is added for completeness sake.
     * @param theFunctionType This is the type of function (0,2,3, or 4).
     * It should be 0 as this is the constructor for sampled functions.
     */
    public FunctionDelegate(Function parentFunction, int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Double> theSize, int theBitsPerSample,
                       int theOrder, List<Double> theEncode, List<Double> theDecode,
                       StringBuffer theFunctionDataStream, List<String> theFilter) {
        this.parentFunction = parentFunction;
        this.functionType = 0;      // dang well better be 0;
        this.size = theSize;
        this.bitsPerSample = theBitsPerSample;
        this.order = theOrder;      // int
        this.encode = theEncode;    // vector of int
        this.decode = theDecode;    // vector of int
        this.functionDataStream = theFunctionDataStream;
        this.filter = theFilter;    // vector of Strings

        // the domain and range are actually two dimensional arrays.
        // so if there's not an even number of items, bad stuff
        // happens.
        this.domain = theDomain;
        this.range = theRange;
    }

    /**
     * create an complete Function object of Type 2, an Exponential Interpolation function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List of Doubles that is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theCZero This is a vector of Double objects which defines the function result
     * when x=0.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param theCOne This is a vector of Double objects which defines the function result
     * when x=1.
     *
     * This attribute is optional.
     * It's described on page 268 of the PDF 1.3 spec.
     * @param theInterpolationExponentN This is the inerpolation exponent.
     *
     * This attribute is required.
     * PDF Spec page 268
     * @param theFunctionType The type of the function, which should be 2.
     */
    public FunctionDelegate(Function parentFunction, int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
                       double theInterpolationExponentN) {
        this.parentFunction = parentFunction;
        this.functionType = 2;    // dang well better be 2;

        this.cZero = theCZero;
        this.cOne = theCOne;
        this.interpolationExponentN = theInterpolationExponentN;

        this.domain = theDomain;
        this.range = theRange;

    }

    /**
     * create an complete Function object of Type 3, a Stitching function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     *
     * @param theDomain List objects of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List objects of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theFunctions A List of the PDFFunction objects that the stitching function stitches.
     *
     * This attributed is required.
     * It is described on page 269 of the PDF spec.
     * @param theBounds This is a vector of Doubles representing the numbers that,
     * in conjunction with Domain define the intervals to which each function from
     * the 'functions' object applies. It must be in order of increasing magnitude,
     * and each must be within Domain.
     *
     * It basically sets how much of the gradient each function handles.
     *
     * This attributed is required.
     * It's described on page 269 of the PDF 1.3 spec.
     * @param theEncode List objects of Double objects.
     * This is the linear mapping of input values intop the domain
     * of the function's sample table. Default is hard to represent in
     * ascii, but basically [0 (Size0 1) 0 (Size1 1)...].
     * This attribute is required.
     *
     * See page 270 in the PDF 1.3 spec.
     * @param theFunctionType This is the function type. It should be 3,
     * for a stitching function.
     */
    public FunctionDelegate(Function parentFunction, int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Function> theFunctions,
                       List<Double> theBounds, List<Double> theEncode) {
        this.parentFunction = parentFunction;
        this.functionType = 3;    // dang well better be 3;

        this.functions = theFunctions;
        this.bounds = theBounds;
        this.encode = theEncode;
        this.domain = theDomain;
        this.range = theRange;

    }

    /**
     * create an complete Function object of Type 4, a postscript calculator function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     *
     * @param theDomain List object of Double objects.
     * This is the domain of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theRange List object of Double objects.
     * This is the Range of the function.
     * See page 264 of the PDF 1.3 Spec.
     * @param theFunctionDataStream This is a stream of arithmetic,
     *            boolean, and stack operators and boolean constants.
     * I end up enclosing it in the '{' and '}' braces for you, so don't do it
     * yourself.
     *
     * This attribute is required.
     * It's described on page 269 of the PDF 1.3 spec.
     * @param theFunctionType The type of function which should be 4, as this is
     * a Postscript calculator function
     */
    public FunctionDelegate(Function parentFunction, int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, StringBuffer theFunctionDataStream) {
        this.parentFunction = parentFunction;
        this.functionType = 4;    // dang well better be 4;
        this.functionDataStream = theFunctionDataStream;

        this.domain = theDomain;

        this.range = theRange;

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
        return functions;
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
    public List<Double> getCZero() {
        return cZero;
    }

    /**
     * Gets the function C1 value (color for gradient)
     */
    public List<Double> getCOne() {
        return cOne;
    }

    public byte[] toByteString() {
        return parentFunction.toByteString();
    }
}
