/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.fop.datatypes;

import org.apache.fop.fo.Property;
import org.apache.fop.messaging.MessageHandler;

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGTransformImpl;

import org.w3c.dom.svg.*;

import java.util.*;
/**
 * a TransformData quantity in XSL
 *
 * @author Keiron Liddle <keiron@aftexsw.com>
 * modified Nov 14,2000 Mike Crowe <crowe@psilongbeach.com>
 */
public class TransformData {
    SVGAnimatedTransformListImpl trans;

    /**
     * set the TransformData given a particular String specifying TransformData and units
     */
    public TransformData (String len) {
        convert(len);
    }


    /**
     * The following is taken from CR-SVG-20000802 7.6, Transform Attribute
     * The value of the transform attribute is a <transform-list>, which is
     * defined as a list of transform definitions, which are applied in the
     * order provided. The individual transform definitions are separated by
     * whitespace and/or a comma. The available types of transform
     * definitions include:
     *
     * matrix(<a> <b> <c> <d> <e> <f>), which specifies a transformation in the
     * 	form of a transformation matrix of six values.
     *	matrix(a,b,c,d,e,f) is equivalent to applying the transformation matrix
     *	[a b c d e f].
     *
     * translate(<tx> [<ty>]), which specifies a translation by tx and ty.
     *
     * scale(<sx> [<sy>]), which specifies a scale operation by sx and sy. If <sy>
     *	is not provided, it is assumed to be equal to <sx>.
     *
     * rotate(<rotate-angle> [<cx> <cy>]), which specifies a rotation by
     *	<rotate-angle> degrees about a given point.
     *	If optional parameters <cx> and <cy> are not supplied, the rotate is about
     *	the origin of the current user coordinate system. The
     *	operation corresponds to the matrix [cos(a) sin(a) -sin(a) cos(a) 0 0].
     *	If optional parameters <cx> and <cy> are supplied, the rotate is about the
     *	point (<cx>, <cy>). The operation represents the
     *	equivalent of the following specification: translate(<cx>, <cy>)
     *	rotate(<rotate-angle>) translate(-<cx>, -<cy>).
     *
     * skewX(<skew-angle>), which specifies a skew transformation along the
     *	x-axis.
     * skewY(<skew-angle>), which specifies a skew transformation along the
     *	y-axis.
     */
    protected void convert(String len) {
        Vector list = new Vector();
        StringTokenizer st = new StringTokenizer(len, "()");
        // need to check for unbalanced brackets
        while (st.hasMoreTokens()) {
            String str = st.nextToken();
            String type = str.trim();
            String value;
            if (st.hasMoreTokens()) {
                value = st.nextToken().trim();
                SVGTransform transform = new SVGTransformImpl();

                if (type.equals("matrix")) {
                    SVGMatrix matrix = new SVGMatrixImpl();
                    StringTokenizer mt =
                      new StringTokenizer(value, " ,\r\n-", true);
                    if (mt.hasMoreTokens())
                        matrix.setA(extractValue(mt));
                    if (mt.hasMoreTokens())
                        matrix.setB(extractValue(mt));
                    if (mt.hasMoreTokens())
                        matrix.setC(extractValue(mt));
                    if (mt.hasMoreTokens())
                        matrix.setD(extractValue(mt));
                    if (mt.hasMoreTokens())
                        matrix.setE(extractValue(mt));
                    if (mt.hasMoreTokens())
                        matrix.setF(extractValue(mt));
                    transform.setMatrix(matrix);
                    list.addElement(transform);
                } else if (type.equals("translate")) {
                    float xlen = 0;
                    float ylen = 0;
                    StringTokenizer mt =
                      new StringTokenizer(value, " ,\r\n-", true);
                    if (mt.hasMoreTokens())
                        xlen = extractValue(mt);
                    if (mt.hasMoreTokens())
                        ylen = extractValue(mt);

                    transform.setTranslate(xlen, ylen);
                    list.addElement(transform);
                } else if (type.equals("scale")) {
                    float xlen = 0;
                    float ylen = 0;
                    StringTokenizer mt =
                      new StringTokenizer(value, " ,\r\n-", true);

                    if (mt.hasMoreTokens())
                        xlen = extractValue(mt);
                    if (mt.hasMoreTokens())
                        ylen = extractValue(mt);
                    else
                        ylen = xlen;

                    transform.setScale(xlen, ylen);
                    list.addElement(transform);
                } else if (type.equals("rotate")) {
                    SVGAngleImpl angle = new SVGAngleImpl();
                    angle.setValueAsString(value);

                    transform.setRotate(angle.getValue(), 0, 0);
                    list.addElement(transform);
                } else if (type.equals("skewX")) {
                    SVGAngleImpl angle = new SVGAngleImpl();
                    angle.setValueAsString(value);

                    transform.setSkewX(angle.getValue());
                    list.addElement(transform);
                } else if (type.equals("skewY")) {
                    SVGAngleImpl angle = new SVGAngleImpl();
                    angle.setValueAsString(value);

                    transform.setSkewY(angle.getValue());
                    list.addElement(transform);

                } else {
                    MessageHandler.errorln(
                      "WARNING: Unknown Transform type : " + type);
                }

            }
        }
        if (list != null) {
            SVGTransformList stl = new SVGTransformListImpl();
            for (Enumeration e = list.elements(); e.hasMoreElements();) {
                stl.appendItem((SVGTransform) e.nextElement());
            }
            trans = new SVGAnimatedTransformListImpl();
            trans.setBaseVal(stl);
        }
    }

    private float extractValue(StringTokenizer mt) {
        String tok = mt.nextToken();
        boolean neg = false;
        while (tok.equals(" ") || tok.equals(",") || tok.equals("\n") ||
                tok.equals("\r") || tok.equals("-")) {
            if (tok.equals("-")) {
                neg = true;
            }
            if (!mt.hasMoreTokens())
                break;
            tok = mt.nextToken();
        }
        if (neg)
            tok = "-" + tok;
        SVGLengthImpl length = new SVGLengthImpl();
        length.setValueAsString(tok);
        return(length.getValue());
    }


    public SVGAnimatedTransformList getTransform() {
        return trans;
    }
}
