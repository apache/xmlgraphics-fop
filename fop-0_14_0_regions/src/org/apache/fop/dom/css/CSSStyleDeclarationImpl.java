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
 
 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
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

package org.apache.fop.dom.css;

import org.apache.fop.datatypes.*;
import org.apache.fop.messaging.*;
import org.apache.fop.dom.svg.*;

import org.w3c.dom.css.*;
import org.w3c.dom.svg.*;

import java.util.*;

/**
 *
 *
 */
public class CSSStyleDeclarationImpl implements CSSStyleDeclaration {
	Hashtable table = new Hashtable();

	public CSSStyleDeclarationImpl()
	{
	}

    public String removeProperty(String str)
    {
        return null;
    }

    public String getCssText()
    {
        return null;
    }

    public void setCssText(String str)
    {
		try {
		parseStyleText(str);
		} catch(Exception e) {
			e.printStackTrace();
		}
    }

	protected void parseStyleText(String len) {
		StringTokenizer st = new StringTokenizer(len, ";");
		while(st.hasMoreTokens()) {
			String str = st.nextToken();
			int pos;
			pos = str.indexOf(":");
			if(pos != -1) {
				String type = str.substring(0, pos).trim();
				String value = str.substring(pos + 1, str.length()).trim();
				if(type.equals("stroke-width")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("stroke")) {
					CSSPrimitiveValue primval = new RGBColorValue();
					primval.setCssText(value);
				    table.put(type, primval);
/*					if(value.startsWith("url(")) {
						table.put(type, new String(value));
					} else if(!value.equals("none")) {
						table.put(type, new ColorType(value));
					} else if(value.equals("none")) {
						table.put(type, value);
					}*/
				} else if(type.equals("color")) {
					CSSPrimitiveValue primval = new RGBColorValue();
					primval.setCssText(value);
				    table.put(type, primval);
//					if(!value.equals("none"))
//						table.put("stroke", new ColorType(value));//??
//					table.put(type, new ColorType(value));//??
				} else if(type.equals("stroke-linecap")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("stroke-linejoin")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("stroke-miterlimit")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("stroke-dasharray")) {
					// array of space or comma separated numbers
/*					if(!value.equals("none")) {
						Vector list = new Vector();
						StringTokenizer array = new StringTokenizer(value, " ,");
						while(array.hasMoreTokens()) {
							String intstr = array.nextToken();
							list.addElement(new Integer(Integer.parseInt(intstr)));
						}
						table.put(type, list);
					}*/
					// else leave ??
				} else if(type.equals("stroke-dashoffset")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("stroke-opacity")) {
				} else if(type.equals("fill")) {
					CSSPrimitiveValue primval = new RGBColorValue();
					primval.setCssText(value);
				    table.put(type, primval);
/*					if(value.startsWith("url(")) {
						table.put(type, new String(value));
					} else if(!value.equals("none")) {
						table.put(type, new ColorType(value));
					} else {
						table.put(type, value);
					}*/
				} else if(type.equals("fill-rule")) {
					// nonzero
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-size")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-family")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-weight")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-style")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-variant")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-stretch")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("font-size-adjust")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("letter-spacing")) {
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("word-spacing")) {
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("text-decoration")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("mask")) {
//					if(value.startsWith("url(")) {
//						value = value.substring(4, value.length() - 1);
//					}
//					table.put(type, value);
				} else if(type.equals("fill-opacity")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("opacity")) {
					CSSPrimitiveValue primval = new LengthValue();
					primval.setCssText(value);
					table.put(type, primval);
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("filter")) {
//					table.put(type, new Filter(value));
				} else if(type.equals("stop-color")) {
					CSSPrimitiveValue primval = new RGBColorValue();
					primval.setCssText(value);
				    table.put(type, primval);
				} else if(type.equals("marker-start")) {
//					table.put(type, new URLValue(value));
				} else if(type.equals("marker-mid")) {
//					table.put(type, new URLValue(value));
				} else if(type.equals("marker-end")) {
//					table.put(type, new URLValue(value));
				} else if(type.equals("text-antialiasing")) {
//					boolean
				} else if(type.equals("stroke-antialiasing")) {
//					boolean
				} else if(type.equals("writing-mode")) {
				} else if(type.equals("glyph-orientation-vertical")) {
				} else if(type.equals("glyph-orientation-horizontal")) {
				} else if(type.equals("direction")) {
				} else if(type.equals("unicode-bidi")) {
				} else if(type.equals("text-anchor")) {
					CSSPrimitiveValue primval = new StringValue();
					primval.setCssText(value);
					table.put(type, primval);
				} else if(type.equals("dominant-baseline")) {
				} else if(type.equals("baseline-identifier")) {
				} else if(type.equals("baseline-shift")) {
				} else if(type.equals("font-size-adjust")) {
				} else {
					MessageHandler.logln("WARNING: Unknown style element : " + type);
				}
			} else {
				MessageHandler.errorln("ERROR: Invalid style element " + str);
			}
		}
	}

    public String getPropertyValue(String str)
    {
        return getPropertyCSSValue(str).getCssText();
    }

    public void setProperty(String a, String b, String c)
    {
    }

    public String item(int n)
    {
        return null;
    }

    public CSSRule getParentRule()
    {
        return null;
    }

    public String getPropertyPriority(String str)
    {
        return null;
    }

    public CSSValue getPropertyCSSValue(String str)
    {
        return (CSSValue)table.get(str);
    }

    public int getLength()
    {
        return table.size();
    }

    class RGBColorValue extends CSSPrimitiveValueImpl {
        RGBColor col = null;

        public void setCssText(String str)
        {
            super.setCssText(str);
			if(str.startsWith("url(")) {
			    primType = CSS_URI;
			} else if(str.equals("currentColor")) {
                primType = CSS_STRING;
			} else if(!str.equals("none")) {
                float red;
                float green;
                float blue;
                ColorType ct = new ColorType(str);
                red = ct.red();
                green = ct.green();
                blue = ct.blue();
                col = new RGBColorImpl(red, green, blue);
                primType = CSS_RGBCOLOR;
            } else {
                primType = CSS_STRING;
            }
        }

        public RGBColor getRGBColorValue()
        {
            if(primType != CSS_RGBCOLOR) {
                // throw exception
            }
            return col;
        }
    }

    /*
     * This should probalby be moved to dom.svg...
     */
    class LengthValue extends CSSPrimitiveValueImpl {

        public void setCssText(String str)
        {
            super.setCssText(str);
            SVGLength length = new SVGLengthImpl(str);
            floatVal = length.getValue();
            switch(length.getUnitType()) {
                case SVGLength.SVG_LENGTHTYPE_IN:
                    primType = CSS_IN;
                break;
                case SVGLength.SVG_LENGTHTYPE_CM:
                    primType = CSS_CM;
                break;
                case SVGLength.SVG_LENGTHTYPE_MM:
                    primType = CSS_MM;
                break;
                case SVGLength.SVG_LENGTHTYPE_PT:
                    primType = CSS_PT;
                break;
                case SVGLength.SVG_LENGTHTYPE_PC:
                    primType = CSS_PC;
                break;
                case SVGLength.SVG_LENGTHTYPE_EMS:
                    primType = CSS_EMS;
                break;
                case SVGLength.SVG_LENGTHTYPE_PX:
                    primType = CSS_PX;
                break;
                case SVGLength.SVG_LENGTHTYPE_PERCENTAGE:
                    primType = CSS_PERCENTAGE;
                break;
                case SVGLength.SVG_LENGTHTYPE_NUMBER:
                    primType = CSS_NUMBER;
                break;
            }
        }
    }

    class StringValue extends CSSPrimitiveValueImpl {

        public void setCssText(String str)
        {
            super.setCssText(str);
            primType = CSS_STRING;
        }
    }
}
