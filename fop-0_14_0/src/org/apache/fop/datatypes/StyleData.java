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

import java.util.*;
/**
 * a StyleData quantity in XSL
 *
 * @author Keiron Liddle <keiron@aftexsw.com>
 */
public class StyleData {
	Hashtable table = new Hashtable();

	/**
	 * set the StyleData given a particular String specifying StyleData and units
	 */
	public StyleData (String len) {
		try {
		convert(len);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	protected void convert(String len) {
		StringTokenizer st = new StringTokenizer(len, ";");
		while(st.hasMoreTokens()) {
			String str = st.nextToken();
			int pos;
			pos = str.indexOf(":");
			if(pos != -1) {
				String type = str.substring(0, pos).trim();
				String value = str.substring(pos + 1, str.length()).trim();
				if(type.equals("stroke-width")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("stroke")) {
					if(value.startsWith("url(")) {
						table.put(type, new String(value));
					} else if(!value.equals("none")) {
						table.put(type, new ColorType(value));
					} else if(value.equals("none")) {
						table.put(type, value);
					}
				} else if(type.equals("color")) {
//					if(!value.equals("none"))
//						table.put("stroke", new ColorType(value));//??
					table.put(type, new ColorType(value));//??
				} else if(type.equals("stroke-linecap")) {
					table.put(type, value);
				} else if(type.equals("stroke-linejoin")) {
					table.put(type, value);
				} else if(type.equals("stroke-miterlimit")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("stroke-dasharray")) {
					// array of space or comma separated numbers
					if(!value.equals("none")) {
						Vector list = new Vector();
						StringTokenizer array = new StringTokenizer(value, " ,");
						while(array.hasMoreTokens()) {
							String intstr = array.nextToken();
							list.addElement(new Integer(Integer.parseInt(intstr)));
						}
						table.put(type, list);
					}
					// else leave ??
				} else if(type.equals("stroke-dashoffset")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("stroke-opacity")) {
				} else if(type.equals("fill")) {
					if(value.startsWith("url(")) {
						table.put(type, new String(value));
					} else if(!value.equals("none")) {
						table.put(type, new ColorType(value));
					} else {
						table.put(type, value);
					}
				} else if(type.equals("fill-rule")) {
					// nonzero
					table.put(type, value);
				} else if(type.equals("font")) {
					table.put(type, value);
				} else if(type.equals("font-size")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("font-family")) {
					table.put(type, value);
				} else if(type.equals("font-weight")) {
					table.put(type, value);
				} else if(type.equals("font-style")) {
					table.put(type, value);
				} else if(type.equals("font-variant")) {
					table.put(type, value);
				} else if(type.equals("font-stretch")) {
					table.put(type, value);
				} else if(type.equals("font-size-adjust")) {
					table.put(type, value);
				} else if(type.equals("letter-spacing")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("word-spacing")) {
					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("text-decoration")) {
					table.put(type, value);
				} else if(type.equals("mask")) {
					if(value.startsWith("url(")) {
						value = value.substring(4, value.length() - 1);
					}
					table.put(type, value);
				} else if(type.equals("fill-opacity")) {
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("opacity")) {
//					table.put(type, new SVGLengthImpl(value));
				} else if(type.equals("filter")) {
//					table.put(type, new Filter(value));
				} else if(type.equals("stop-color")) {
					table.put(type, new ColorType(value));
				} else if(type.equals("marker-start")) {
//					table.put(type, new URLType(value));
				} else if(type.equals("marker-mid")) {
//					table.put(type, new URLType(value));
				} else if(type.equals("marker-end")) {
//					table.put(type, new URLType(value));
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
					table.put(type, value);
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

	public Hashtable getStyle()
	{
		return table;
	}
}
