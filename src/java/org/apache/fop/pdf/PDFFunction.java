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

import org.apache.fop.render.shading.Function;
import org.apache.fop.render.shading.FunctionDelegate;
import org.apache.fop.render.shading.FunctionPattern;

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
public class PDFFunction extends PDFObject implements Function {

    private FunctionDelegate delegate;

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
    public PDFFunction(int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Double> theSize, int theBitsPerSample,
                       int theOrder, List<Double> theEncode, List<Double> theDecode,
                       StringBuffer theFunctionDataStream, List<String> theFilter) {
        delegate = new FunctionDelegate(this, theFunctionType, theDomain, theRange,
                theSize, theBitsPerSample, theOrder, theEncode, theDecode,
                theFunctionDataStream, theFilter);
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
    public PDFFunction(int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Double> theCZero, List<Double> theCOne,
                       double theInterpolationExponentN) {
        delegate = new FunctionDelegate(this, theFunctionType, theDomain, theRange,
                theCZero, theCOne, theInterpolationExponentN);

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
    public PDFFunction(int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, List<Function> theFunctions,
                       List<Double> theBounds, List<Double> theEncode) {
        delegate = new FunctionDelegate(this, theFunctionType, theDomain, theRange,
                theFunctions, theBounds, theEncode);

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
    public PDFFunction(int theFunctionType, List<Double> theDomain,
                       List<Double> theRange, StringBuffer theFunctionDataStream) {
        delegate = new FunctionDelegate(this, theFunctionType, theDomain, theRange,
                theFunctionDataStream);
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
        return toByteString();
    }


    public byte[] toByteString() {
        FunctionPattern pattern = new FunctionPattern(this);
        return encode(pattern.toWriteableString());
    }

    /** {@inheritDoc} */
    protected boolean contentEquals(PDFObject obj) {
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
        if (delegate.getFunctionType() != func.getFunctionType()) {
            return false;
        }
        if (delegate.getBitsPerSample() != func.getBitsPerSample()) {
            return false;
        }
        if (delegate.getOrder() != func.getOrder()) {
            return false;
        }
        if (delegate.getInterpolationExponentN() != func.getInterpolationExponentN()) {
            return false;
        }
        if (delegate.getDomain() != null) {
            if (!delegate.getDomain().equals(func.getDomain())) {
                return false;
            }
        } else if (func.getDomain() != null) {
            return false;
        }
        if (delegate.getRange() != null) {
            if (!delegate.getRange().equals(func.getRange())) {
                return false;
            }
        } else if (func.getRange() != null) {
            return false;
        }
        if (delegate.getSize() != null) {
            if (!delegate.getSize().equals(func.getSize())) {
                return false;
            }
        } else if (func.getSize() != null) {
            return false;
        }
        if (delegate.getEncode() != null) {
            if (!delegate.getEncode().equals(func.getEncode())) {
                return false;
            }
        } else if (func.getEncode() != null) {
            return false;
        }
        if (delegate.getDecode() != null) {
            if (!delegate.getDecode().equals(func.getDecode())) {
                return false;
            }
        } else if (func.getDecode() != null) {
            return false;
        }
        if (delegate.getDataStream() != null) {
            if (!delegate.getDataStream().equals(func.getDataStream())) {
                return false;
            }
        } else if (func.getDataStream() != null) {
            return false;
        }
        if (delegate.getFilter() != null) {
            if (!delegate.getFilter().equals(func.getFilter())) {
                return false;
            }
        } else if (func.getFilter() != null) {
            return false;
        }
        if (delegate.getCZero() != null) {
            if (!delegate.getCZero().equals(func.getCZero())) {
                return false;
            }
        } else if (func.getCZero() != null) {
            return false;
        }
        if (delegate.getCOne() != null) {
            if (!delegate.getCOne().equals(func.getCOne())) {
                return false;
            }
        } else if (func.getCOne() != null) {
            return false;
        }
        if (delegate.getFunctions() != null) {
            if (!delegate.getFunctions().equals(func.getFunctions())) {
                return false;
            }
        } else if (func.getFunctions() != null) {
            return false;
        }
        if (delegate.getBounds() != null) {
            if (!delegate.getBounds().equals(func.getBounds())) {
                return false;
            }
        } else if (func.getBounds() != null) {
            return false;
        }
        return true;
    }

    public int getFunctionType() {
        return delegate.getFunctionType();
    }

    public List<Double> getBounds() {
        return delegate.getBounds();
    }

    public List<Double> getDomain() {
        return delegate.getDomain();
    }

    public List<Double> getSize() {
        return delegate.getSize();
    }

    public List<String> getFilter() {
        return delegate.getFilter();
    }

    public List<Double> getEncode() {
        return delegate.getEncode();
    }

    public List<Function> getFunctions() {
        return delegate.getFunctions();
    }

    public int getBitsPerSample() {
        return delegate.getBitsPerSample();
    }

    public double getInterpolationExponentN() {
        return delegate.getInterpolationExponentN();
    }

    public int getOrder() {
        return delegate.getOrder();
    }

    public List<Double> getRange() {
        return delegate.getRange();
    }

    public List<Double> getDecode() {
        return delegate.getDecode();
    }

    public StringBuffer getDataStream() {
        return delegate.getDataStream();
    }

    public List<Double> getCZero() {
        return delegate.getCZero();
    }

    public List<Double> getCOne() {
        return delegate.getCOne();
    }
}
