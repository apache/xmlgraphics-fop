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

// Java...
import java.util.List;

/**
 * class representing a PDF Function.
 *
 * PDF Functions represent parameterized mathematical formulas and
 * sampled representations with
 * arbitrary resolution. Functions are used in two areas: device-dependent
 * rasterization information for halftoning and transfer
 * functions, and color specification for smooth shading (a PDF 1.3 feature).
 *
 * All PDF Functions have a FunctionType (0,2,3, or 4), a Domain, and a Range.
 */
public class PDFFunction extends PDFObject {
    // Guts common to all function types

    /**
     * Required: The Type of function (0,2,3,4) default is 0.
     */
    protected int functionType = 0;    // Default

    /**
     * Required: 2 * m Array of Double numbers which are possible inputs to the function
     */
    protected List domain = null;

    /**
     * Required: 2 * n Array of Double numbers which are possible outputs to the function
     */
    protected List range = null;

    /* ********************TYPE 0***************************** */
    // FunctionType 0 specific function guts

    /**
     * Required: Array containing the Integer size of the Domain and Range, respectively.
     * Note: This is really more like two seperate integers, sizeDomain, and sizeRange,
     * but since they're expressed as an array in PDF, my implementation reflects that.
     */
    protected List size = null;

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
    protected List encode = null;

    /**
     * Optional for Type 0: A 2 * n array of Doubles which provides
     * a linear mapping of sample values to the range. Defaults to Range.
     */
    protected List decode = null;

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
    protected List filter = null;
    /* *************************TYPE 2************************** */

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=0. Default is [0].
     */
    protected List cZero = null;

    /**
     * Required For Type 2: An Array of n Doubles defining
     * the function result when x=1. Default is [1].
     */
    protected List cOne = null;

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
    protected List functions = null;

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
    protected List bounds = null;
    // See encode above, as it's also part of Type 3 Functions.

    /* *************************TYPE 4************************** */
    // See 'data' above.

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
    public PDFFunction(int theFunctionType, List theDomain,
                       List theRange, List theSize, int theBitsPerSample,
                       int theOrder, List theEncode, List theDecode,
                       StringBuffer theFunctionDataStream, List theFilter) {
        super();

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
    public PDFFunction(int theFunctionType, List theDomain,
                       List theRange, List theCZero, List theCOne,
                       double theInterpolationExponentN) {
        super();

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
    public PDFFunction(int theFunctionType, List theDomain,
                       List theRange, List theFunctions,
                       List theBounds, List theEncode) {
        super();

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
    public PDFFunction(int theFunctionType, List theDomain,
                       List theRange, StringBuffer theFunctionDataStream) {
        super();

        this.functionType = 4;    // dang well better be 4;
        this.functionDataStream = theFunctionDataStream;

        this.domain = theDomain;

        this.range = theRange;

    }


    /**
     * represent as PDF. Whatever the FunctionType is, the correct
     * representation spits out. The sets of required and optional
     * attributes are different for each type, but if a required
     * attribute's object was constructed as null, then no error
     * is raised. Instead, the malformed PDF that was requested
     * by the construction is dutifully output.
     * This policy should be reviewed.
     *
     * @return the PDF string.
     */
    public byte[] toPDF() {
        int vectorSize = 0;
        int numberOfFunctions = 0;
        int tempInt = 0;
        StringBuffer p = new StringBuffer(256);
        p.append(getObjectID()
                + "<< \n/FunctionType " + this.functionType + " \n");

        // FunctionType 0
        if (this.functionType == 0) {
            if (this.domain != null) {
                // DOMAIN
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // SIZE
            if (this.size != null) {
                p.append("/Size [ ");
                vectorSize = this.size.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.size.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }
            // ENCODE
            if (this.encode != null) {
                p.append("/Encode [ ");
                vectorSize = this.encode.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.encode.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Encode [ ");
                vectorSize = this.functions.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append("0 1 ");
                }
                p.append("] \n");

            }

            // BITSPERSAMPLE
            p.append("/BitsPerSample " + this.bitsPerSample);

            // ORDER (optional)
            if (this.order == 1 || this.order == 3) {
                p.append(" \n/Order " + this.order + " \n");
            }

            // RANGE
            if (this.range != null) {
                p.append("/Range [ ");
                vectorSize = this.range.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.range.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // DECODE
            if (this.decode != null) {
                p.append("/Decode [ ");
                vectorSize = this.decode.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.decode.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // LENGTH
            if (this.functionDataStream != null) {
                p.append("/Length " + (this.functionDataStream.length() + 1)
                         + " \n");
            }

            // FILTER?
            if (this.filter != null) {           // if there's a filter
                vectorSize = this.filter.size();
                p.append("/Filter ");
                if (vectorSize == 1) {
                    p.append("/" + ((String)this.filter.get(0))
                             + " \n");
                } else {
                    p.append("[ ");
                    for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                        p.append("/" + ((String)this.filter.get(0))
                                 + " ");
                    }
                    p.append("] \n");
                }
            }
            p.append(">> \n");

            // stream representing the function
            if (this.functionDataStream != null) {
                p.append("stream\n" + this.functionDataStream
                         + "\nendstream\n");
            }

            p.append("endobj\n");
            // end of if FunctionType 0

        } else if (this.functionType == 2) {
            // DOMAIN
            if (this.domain != null) {
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }


            // RANGE
            if (this.range != null) {
                p.append("/Range [ ");
                vectorSize = this.range.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.range.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // FunctionType, C0, C1, N are required in PDF

            // C0
            if (this.cZero != null) {
                p.append("/C0 [ ");
                vectorSize = this.cZero.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.cZero.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            // C1
            if (this.cOne != null) {
                p.append("/C1 [ ");
                vectorSize = this.cOne.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.cOne.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            // N: The interpolation Exponent
            p.append("/N "
                     + PDFNumber.doubleOut(new Double(this.interpolationExponentN))
                     + " \n");

            p.append(">> \nendobj\n");

        } else if (this.functionType
                   == 3) {                       // fix this up when my eyes uncross
            // DOMAIN
            if (this.domain != null) {
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // RANGE
            if (this.range != null) {
                p.append("/Range [ ");
                vectorSize = this.range.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.range.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // FUNCTIONS
            if (this.functions != null) {
                p.append("/Functions [ ");
                numberOfFunctions = this.functions.size();
                for (tempInt = 0; tempInt < numberOfFunctions; tempInt++) {
                    p.append(((PDFFunction)this.functions.get(tempInt)).referencePDF()
                             + " ");

                }
                p.append("] \n");
            }


            // ENCODE
            if (this.encode != null) {
                p.append("/Encode [ ");
                vectorSize = this.encode.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.encode.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Encode [ ");
                vectorSize = this.functions.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append("0 1 ");
                }
                p.append("] \n");

            }


            // BOUNDS, required, but can be empty
            p.append("/Bounds [ ");
            if (this.bounds != null) {

                vectorSize = this.bounds.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.bounds.get(tempInt))
                             + " ");
                }

            } else {
                if (this.functions != null) {
                    // if there are n functions,
                    // there must be n-1 bounds.
                    // so let each function handle an equal portion
                    // of the whole. e.g. if there are 4, then [ 0.25 0.25 0.25 ]

                    String functionsFraction = PDFNumber.doubleOut(new Double(1.0
                            / ((double)numberOfFunctions)));

                    for (tempInt = 0; tempInt + 1 < numberOfFunctions;
                            tempInt++) {

                        p.append(functionsFraction + " ");
                    }
                    functionsFraction = null;    // clean reference.

                }

            }
            p.append("] \n");


            p.append(">> \nendobj\n");
        } else if (this.functionType
                   == 4) {                       // fix this up when my eyes uncross
            // DOMAIN
            if (this.domain != null) {
                p.append("/Domain [ ");
                vectorSize = this.domain.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.domain.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // RANGE
            if (this.range != null) {
                p.append("/Range [ ");
                vectorSize = this.range.size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut((Double)this.range.get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // LENGTH
            if (this.functionDataStream != null) {
                p.append("/Length " + (this.functionDataStream.length() + 1)
                         + " \n");
            }

            p.append(">> \n");

            // stream representing the function
            if (this.functionDataStream != null) {
                p.append("stream\n{ " + this.functionDataStream
                         + " } \nendstream\n");
            }

            p.append("endobj\n");

        }

        return encode(p.toString());

    }

    /**
     * Check if this function is equal to another object.
     * This is used to find if a particular function already exists
     * in a document.
     *
     * @param obj the obj to compare
     * @return true if the functions are equal
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PDFFunction)) {
            return false;
        }
        PDFFunction func = (PDFFunction)obj;
        if (functionType != func.functionType) {
            return false;
        }
        if (bitsPerSample != func.bitsPerSample) {
            return false;
        }
        if (order != func.order) {
            return false;
        }
        if (interpolationExponentN != func.interpolationExponentN) {
            return false;
        }
        if (domain != null) {
            if (!domain.equals(func.domain)) {
                return false;
            }
        } else if (func.domain != null) {
            return false;
        }
        if (range != null) {
            if (!range.equals(func.range)) {
                return false;
            }
        } else if (func.range != null) {
            return false;
        }
        if (size != null) {
            if (!size.equals(func.size)) {
                return false;
            }
        } else if (func.size != null) {
            return false;
        }
        if (encode != null) {
            if (!encode.equals(func.encode)) {
                return false;
            }
        } else if (func.encode != null) {
            return false;
        }
        if (decode != null) {
            if (!decode.equals(func.decode)) {
                return false;
            }
        } else if (func.decode != null) {
            return false;
        }
        if (functionDataStream != null) {
            if (!functionDataStream.equals(func.functionDataStream)) {
                return false;
            }
        } else if (func.functionDataStream != null) {
            return false;
        }
        if (filter != null) {
            if (!filter.equals(func.filter)) {
                return false;
            }
        } else if (func.filter != null) {
            return false;
        }
        if (cZero != null) {
            if (!cZero.equals(func.cZero)) {
                return false;
            }
        } else if (func.cZero != null) {
            return false;
        }
        if (cOne != null) {
            if (!cOne.equals(func.cOne)) {
                return false;
            }
        } else if (func.cOne != null) {
            return false;
        }
        if (functions != null) {
            if (!functions.equals(func.functions)) {
                return false;
            }
        } else if (func.functions != null) {
            return false;
        }
        if (bounds != null) {
            if (!bounds.equals(func.bounds)) {
                return false;
            }
        } else if (func.bounds != null) {
            return false;
        }
        return true;
    }

}
