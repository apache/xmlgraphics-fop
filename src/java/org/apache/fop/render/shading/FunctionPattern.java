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

package org.apache.fop.render.shading;

import java.util.List;

import org.apache.fop.pdf.PDFNumber;

/**
 * A class for writing function objects for different output formats
 */
public class FunctionPattern {

    private Function function;

    /**
     * Constructor
     * @param function The function from which to write the output
     */
    public FunctionPattern(Function function) {
        this.function = function;
    }

    /**
     * Outputs the function to a byte array
     */
    public String toWriteableString(List<String> functionsStrings) {
        int vectorSize = 0;
        int numberOfFunctions = 0;
        int tempInt = 0;
        StringBuffer p = new StringBuffer(256);
        p.append("<< \n/FunctionType " + function.getFunctionType() + " \n");

        // FunctionType 0
        if (this.function.getFunctionType() == 0) {
            if (function.getDomain() != null) {
                // DOMAIN
                p.append("/Domain [ ");
                vectorSize = function.getDomain().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getDomain().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // SIZE
            if (function.getSize() != null) {
                p.append("/Size [ ");
                vectorSize = function.getSize().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getSize().get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }
            // ENCODE
            if (function.getEncode() != null) {
                p.append("/Encode [ ");
                vectorSize = function.getEncode().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getEncode().get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Encode [ ");
                vectorSize = function.getFunctions().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append("0 1 ");
                }
                p.append("] \n");

            }

            // BITSPERSAMPLE
            p.append("/BitsPerSample " + function.getBitsPerSample());

            // ORDER (optional)
            if (function.getOrder() == 1 || function.getOrder() == 3) {
                p.append(" \n/Order " + function.getOrder() + " \n");
            }

            // RANGE
            if (function.getRange() != null) {
                p.append("/Range [ ");
                vectorSize = function.getRange().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getRange().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // DECODE
            if (function.getDecode() != null) {
                p.append("/Decode [ ");
                vectorSize = function.getDecode().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getDecode().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // LENGTH
            if (function.getDataStream() != null) {
                p.append("/Length " + (function.getDataStream().length() + 1)
                         + " \n");
            }

            // FILTER?
            if (function.getFilter() != null) {           // if there's a filter
                vectorSize = function.getFilter().size();
                p.append("/Filter ");
                if (vectorSize == 1) {
                    p.append("/" + (function.getFilter().get(0))
                             + " \n");
                } else {
                    p.append("[ ");
                    for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                        p.append("/" + (function.getFilter().get(0))
                                 + " ");
                    }
                    p.append("] \n");
                }
            }
            p.append(">>");

            // stream representing the function
            if (function.getDataStream() != null) {
                p.append("\nstream\n" + function.getDataStream()
                         + "\nendstream");
            }

            // end of if FunctionType 0

        } else if (function.getFunctionType() == 2) {
            // DOMAIN
            if (function.getDomain() != null) {
                p.append("/Domain [ ");
                vectorSize = function.getDomain().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getDomain().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }


            // RANGE
            if (function.getRange() != null) {
                p.append("/Range [ ");
                vectorSize = function.getRange().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getRange().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // FunctionType, C0, C1, N are required in PDF

            // C0
            if (function.getCZero() != null) {
                p.append("/C0 [ ");
                vectorSize = function.getCZero().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getCZero().get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            // C1
            if (function.getCOne() != null) {
                p.append("/C1 [ ");
                vectorSize = function.getCOne().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getCOne().get(tempInt))
                             + " ");
                }
                p.append("] \n");
            }

            // N: The interpolation Exponent
            p.append("/N "
                     + PDFNumber.doubleOut(Double.valueOf(function.getInterpolationExponentN()))
                     + " \n");

            p.append(">>");

        } else if (function.getFunctionType()
                   == 3) {                       // fix this up when my eyes uncross
            // DOMAIN
            if (function.getDomain() != null) {
                p.append("/Domain [ ");
                vectorSize = function.getDomain().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getDomain().get(tempInt))
                             + " ");
                }
                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // RANGE
            if (function.getRange() != null) {
                p.append("/Range [ ");
                vectorSize = function.getRange().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getRange().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // FUNCTIONS
            if (!function.getFunctions().isEmpty()) {
                p.append("/Functions [ ");
                numberOfFunctions = function.getFunctions().size();
                for (String f : functionsStrings) {
                    p.append(f);
                    p.append(' ');
                }
                p.append("] \n");
            }


            // ENCODE
            if (function.getEncode() != null) {
                p.append("/Encode [ ");
                vectorSize = function.getEncode().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getEncode().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Encode [ ");
                vectorSize = function.getFunctions().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append("0 1 ");
                }
                p.append("] \n");

            }


            // BOUNDS, required, but can be empty
            p.append("/Bounds [ ");
            if (function.getBounds() != null) {

                vectorSize = function.getBounds().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getBounds().get(tempInt))
                             + " ");
                }

            } else {
                if (!function.getFunctions().isEmpty()) {
                    // if there are n functions,
                    // there must be n-1 bounds.
                    // so let each function handle an equal portion
                    // of the whole. e.g. if there are 4, then [ 0.25 0.25 0.25 ]

                    String functionsFraction = PDFNumber.doubleOut(Double.valueOf(1.0
                            / (numberOfFunctions)));

                    for (tempInt = 0; tempInt + 1 < numberOfFunctions;
                            tempInt++) {

                        p.append(functionsFraction + " ");
                    }
                }

            }
            p.append("]\n>>");
        } else if (function.getFunctionType()
                   == 4) {                       // fix this up when my eyes uncross
            // DOMAIN
            if (function.getDomain() != null) {
                p.append("/Domain [ ");
                vectorSize = function.getDomain().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getDomain().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            } else {
                p.append("/Domain [ 0 1 ] \n");
            }

            // RANGE
            if (function.getRange() != null) {
                p.append("/Range [ ");
                vectorSize = function.getRange().size();
                for (tempInt = 0; tempInt < vectorSize; tempInt++) {
                    p.append(PDFNumber.doubleOut(function.getRange().get(tempInt))
                             + " ");
                }

                p.append("] \n");
            }

            // LENGTH
            if (function.getDataStream() != null) {
                p.append("/Length " + (function.getDataStream().length() + 1)
                         + " \n");
            }

            p.append(">>");

            // stream representing the function
            if (function.getDataStream() != null) {
                p.append("\nstream\n{ " + function.getDataStream()
                         + " }\nendstream");
            }
        }
        return p.toString();
    }
}
