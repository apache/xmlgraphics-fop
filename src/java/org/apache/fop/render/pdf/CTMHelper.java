/*
 * $Id: CTMHelper.java,v 1.2 2003/03/07 09:46:32 jeremias Exp $
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
package org.apache.fop.render.pdf;

import org.apache.fop.area.CTM;

/**
 * CTMHelper converts FOP transformation matrixis to those
 * suitable for use by the PDFRender. The e and f elements
 * of the matrix will be divided by 1000 as FOP uses millipoints
 * as it's default user space and PDF uses points.
 *
 * @see org.apache.fop.area.CTM
 *
 * @author <a href="kevin@rocketred.com>Kevin O'Neill</a>
 */
public final class CTMHelper {
    /**
     * <p>Converts the sourceMatrix to a string for use in the PDFRender cm operations.</p>
     * <p>For example:
     * <pre>
     *    org.apache.fop.area.CTM ctm = 
     *          new org.apache.fop.area.CTM(1.0, 0.0, 0.0, 1.0, 1000.0, 1000.0);
     *    String pdfMatrix =  org.apache.fop.render.pdf.CTMHelper.toPDFString(ctm);
     * </pre>
     * will return the string "<code>1.0 0.0 0.0 1.0 1.0 1.0</code>".
     *
     * @param sourceMatrix - The matrix to convert.
     *
     * @return  a space seperated string containing the matrix elements.
     */
    public static String toPDFString(CTM sourceMatrix) {
        if (null == sourceMatrix) {
            throw new NullPointerException("sourceMatrix must not be null");
        }

        final double matrix[] = toPDFArray(sourceMatrix);

        return matrix[0] + " " + matrix[1] + " " 
             + matrix[2] + " " + matrix[3] + " " 
             + matrix[4] + " " + matrix[5];
    }

    /**
     * <p>Creates a new CTM based in the sourceMatrix.</p>
     * <p>For example:
     * <pre>
     *    org.apache.fop.area.CTM inCTM = 
     *          new org.apache.fop.area.CTM(1.0, 0.0, 0.0, 1.0, 1000.0, 1000.0);
     *    org.apache.fop.area.CTM outCTM = 
     *          org.apache.fop.render.pdf.CTMHelper.toPDFCTM(ctm);
     * </pre>
     * will return a new CTM where a == 1.0, b == 0.0, c == 0.0, d == 1.0, e == 1.0 and f == 1.0.
     *
     * @param sourceMatrix - The matrix to convert.
     *
     * @return  a new converted matrix.
     */
    public static CTM toPDFCTM(CTM sourceMatrix) {
        if (null == sourceMatrix) {
            throw new NullPointerException("sourceMatrix must not be null");
        }

        final double matrix[] = toPDFArray(sourceMatrix);

        return new CTM(matrix[0], matrix[1], matrix[2], matrix[3],
                       matrix[4], matrix[5]);
    }

    /**
     * <p>Creates an array of six doubles from the source CTM.</p>
     * <p>For example:
     * <pre>
     *    org.apache.fop.area.CTM inCTM = 
     *          new org.apache.fop.area.CTM(1.0, 0.0, 0.0, 1.0, 1000.0, 1000.0);
     *    double matrix[] = org.apache.fop.render.pdf.CTMHelper.toPDFArray(ctm);
     * </pre>
     * will return a new array where matrix[0] == 1.0, matrix[1] == 0.0,
     * matrix[2] == 0.0, matrix[3] == 1.0,
     * matrix[4] == 1.0 and matrix[5] == 1.0.
     *
     * @param sourceMatrix - The matrix to convert.
     * @return  an array of doubles containing the converted matrix.
     */
    public static double[] toPDFArray(CTM sourceMatrix) {
        if (null == sourceMatrix) {
            throw new NullPointerException("sourceMatrix must not be null");
        }

        final double matrix[] = sourceMatrix.toArray();

        return new double[]{matrix[0], matrix[1], matrix[2], matrix[3],
                            matrix[4] / 1000.0, matrix[5] / 1000.0};
    }

}

