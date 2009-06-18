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

import java.util.*;
/**
 * a TransformData quantity in XSL
 *
 * @author Keiron Liddle <keiron@aftexsw.com>
 */
public class TransformData {
	Vector list = new Vector();

	/**
	 * set the TransformData given a particular String specifying TransformData and units
	 */
	public TransformData (String len) {
		convert(len);
	}

	protected void convert(String len) {
		StringTokenizer st = new StringTokenizer(len, "()");
		// need to check for unbalanced brackets
		while(st.hasMoreTokens()) {
			String str = st.nextToken();
			String type = str.trim();
			String value;
			if(st.hasMoreTokens()) {
				value = st.nextToken().trim();
				SVGTransformImpl transform = new SVGTransformImpl();
				if(type.equals("translate")) {
					float xlen = 0;
					float ylen = 0;
					int pos = value.indexOf(",");
					if(pos != -1) {
						SVGLengthImpl length;
						length = new SVGLengthImpl();
						length.setValueAsString(value.substring(0, pos).trim());
						xlen = length.getValue();
						length = new SVGLengthImpl();
						length.setValueAsString(value.substring(pos + 1, value.length()).trim());
						ylen = length.getValue();
					}
					transform.setTranslate(xlen, ylen);
					list.addElement(transform);
				} else if(type.equals("skewX")) {
//					SVGAngleImpl angle = new SVGAngleImpl();
//					angle.setValueAsString(value);
					float angle = Float.valueOf(value).floatValue();
					transform.setSkewX(angle);
					list.addElement(transform);
				} else if(type.equals("skewY")) {
	//				SVGAngleImpl angle = new SVGAngleImpl();
//					angle.setValueAsString(value);
					float angle = Float.valueOf(value).floatValue();
					transform.setSkewY(angle);
					list.addElement(transform);
				} else if(type.equals("scale")) {
					float xlen = 0;
					float ylen = 0;
					int pos = value.indexOf(",");
					if(pos != -1) {
//						try {
							xlen = Float.valueOf(value.substring(0, pos).trim()).floatValue();
//						} catch(Exception e) {
//						}
//						try {
							ylen = Float.valueOf(value.substring(pos + 1, value.length()).trim()).floatValue();
//						} catch(Exception e) {
//						}
					}
					transform.setScale(xlen, ylen);
					list.addElement(transform);
				} else if(type.equals("rotate")) {
	//				SVGAngleImpl angle = new SVGAngleImpl();
//					angle.setValueAsString(value);
					float angle = Float.valueOf(value).floatValue();
					transform.setRotate(angle, 0, 0);
					list.addElement(transform);
				} else if(type.equals("matrix")) {
					SVGMatrixImpl matrix = new SVGMatrixImpl();
					StringTokenizer mt = new StringTokenizer(value, " ,\r\n-", true);
					// need to handle negatives
					String tok;
					boolean neg = false;
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
						matrix.setA(floatVal);
					}
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
						matrix.setB(floatVal);
					}
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
						matrix.setC(floatVal);
					}
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
						matrix.setD(floatVal);
					}
					SVGLengthImpl length;
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
//						if(neg)
//							tok = "-" + tok;
//						length = new SVGLengthImpl();
//						length.setValueAsString(tok);
						matrix.setE(floatVal);
					}
					if(mt.hasMoreTokens()) {
						tok = mt.nextToken();
						while(tok.equals(" ") || tok.equals(",") || tok.equals("\n") || tok.equals("\r") || tok.equals("-")) {
							if(tok.equals("-")) {
								neg = true;
							}
							if(!mt.hasMoreTokens())
								break;
							tok = mt.nextToken();
						}
						float floatVal = Float.valueOf(tok).floatValue();
						if(neg)
							floatVal = -floatVal;
//						if(neg)
//							tok = "-" + tok;
//						length = new SVGLengthImpl();
//						length.setValueAsString(tok);
						matrix.setF(floatVal);
					}
					transform.setMatrix(matrix);
					list.addElement(transform);
				} else {
					MessageHandler.errorln("WARNING: Unknown Transform type : " + type);
				}
			}
		}
	}

	public Vector oldgetTransform()
	{
		return list;
	}
}
