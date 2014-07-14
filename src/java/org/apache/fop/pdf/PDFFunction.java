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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.fop.render.gradient.Function;

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

    private final Function function;

    private final List<PDFFunction> pdfFunctions;

    /**
     * create an complete Function object of Type 2, an Exponential Interpolation function.
     *
     * Use null for an optional object parameter if you choose not to use it.
     * For optional int parameters, pass the default.
     *
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
     * @param functionType The type of the function, which should be 2.
     */
    public PDFFunction(int functionType, List<Double> domain, List<Double> range, float[] cZero, float[] cOne,
                       double interpolationExponentN) {
        this(new Function(functionType, domain, range, cZero, cOne, interpolationExponentN));

    }

    @SuppressWarnings("unchecked")
    public PDFFunction(Function function) {
        this(function, Collections.EMPTY_LIST);
    }

    public PDFFunction(Function function, List<PDFFunction> pdfFunctions) {
        this.function = function;
        this.pdfFunctions = pdfFunctions;
    }

    public Function getFunction() {
        return function;
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
        List<String> functionsStrings = new ArrayList<String>(function.getFunctions().size());
        for (PDFFunction f : pdfFunctions) {
            functionsStrings.add(f.referencePDF());
        }
        return encode(function.toWriteableString(functionsStrings));
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
        Function func = ((PDFFunction) obj).function;
        if (function.getFunctionType() != func.getFunctionType()) {
            return false;
        }
        if (function.getBitsPerSample() != func.getBitsPerSample()) {
            return false;
        }
        if (function.getOrder() != func.getOrder()) {
            return false;
        }
        if (function.getInterpolationExponentN() != func.getInterpolationExponentN()) {
            return false;
        }
        if (function.getDomain() != null) {
            if (!function.getDomain().equals(func.getDomain())) {
                return false;
            }
        } else if (func.getDomain() != null) {
            return false;
        }
        if (function.getRange() != null) {
            if (!function.getRange().equals(func.getRange())) {
                return false;
            }
        } else if (func.getRange() != null) {
            return false;
        }
        if (function.getSize() != null) {
            if (!function.getSize().equals(func.getSize())) {
                return false;
            }
        } else if (func.getSize() != null) {
            return false;
        }
        if (function.getEncode() != null) {
            if (!function.getEncode().equals(func.getEncode())) {
                return false;
            }
        } else if (func.getEncode() != null) {
            return false;
        }
        if (function.getDecode() != null) {
            if (!function.getDecode().equals(func.getDecode())) {
                return false;
            }
        } else if (func.getDecode() != null) {
            return false;
        }
        if (function.getDataStream() != null) {
            if (!function.getDataStream().equals(func.getDataStream())) {
                return false;
            }
        } else if (func.getDataStream() != null) {
            return false;
        }
        if (function.getFilter() != null) {
            if (!function.getFilter().equals(func.getFilter())) {
                return false;
            }
        } else if (func.getFilter() != null) {
            return false;
        }
        if (!Arrays.equals(function.getCZero(), func.getCZero())) {
            return false;
        }
        if (!Arrays.equals(function.getCOne(), func.getCOne())) {
            return false;
        }
        if (!pdfFunctions.equals(((PDFFunction) obj).pdfFunctions)) {
            return false;
        }
        if (function.getBounds() != null) {
            if (!function.getBounds().equals(func.getBounds())) {
                return false;
            }
        } else if (func.getBounds() != null) {
            return false;
        }
        return true;
    }

}
