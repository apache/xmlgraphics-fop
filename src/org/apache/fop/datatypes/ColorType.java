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
package org.apache.fop.datatypes;

import java.util.*;

/**
 * a colour quantity in XSL
 */
public class ColorType {

    /** the red component */
    protected float red;

    /** the green component */
    protected float green;

    /** the blue component */
    protected float blue;

    /** the alpha component */
    protected float alpha = 0;

    /**
     * set the colour given a particular String specifying either a
     * colour name or #RGB or #RRGGBB 
     */
    public ColorType (String value) {
	if (value.startsWith("#")) {
	    try {
		if (value.length()==4) {
		    // note: divide by 15 so F = FF = 1 and so on
		    this.red = Integer.parseInt(value.substring(1,2),16)/15f;
		    this.green = Integer.parseInt(value.substring(2,3),16)/15f;
		    this.blue = Integer.parseInt(value.substring(3),16)/15f;
		} else if (value.length()==7) {
		    // note: divide by 255 so FF = 1
		    this.red = Integer.parseInt(value.substring(1,3),16)/255f;
		    this.green = Integer.parseInt(value.substring(3,5),16)/255f;
		    this.blue = Integer.parseInt(value.substring(5),16)/255f;
		} else {
		    this.red = 0;
		    this.green = 0;
		    this.blue = 0;
		    System.err.println("ERROR: unknown colour format. Must be #RGB or #RRGGBB");
		}
	    } catch (Exception e) {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		System.err.println("ERROR: unknown colour format. Must be #RGB or #RRGGBB");
	    }
	} else if (value.startsWith("rgb(")) {
		int poss = value.indexOf("(");
		int pose = value.indexOf(")");
		if(poss != -1 && pose != -1) {
			value = value.substring(poss + 1, pose);
			StringTokenizer st = new StringTokenizer(value, ",");
			try {
				if(st.hasMoreTokens()) {
					String str = st.nextToken().trim();
					if(str.endsWith("%")) {
						this.red = Integer.parseInt(str.substring(0, str.length() - 1)) * 2.55f;
					} else {
					    this.red = Integer.parseInt(str)/255f;
				    }
				}
				if(st.hasMoreTokens()) {
					String str = st.nextToken().trim();
					if(str.endsWith("%")) {
						this.green = Integer.parseInt(str.substring(0, str.length() - 1)) * 2.55f;
					} else {
					    this.green = Integer.parseInt(str)/255f;
				    }
				}
				if(st.hasMoreTokens()) {
					String str = st.nextToken().trim();
					if(str.endsWith("%")) {
						this.blue = Integer.parseInt(str.substring(0, str.length() - 1)) * 2.55f;
					} else {
					    this.blue = Integer.parseInt(str)/255f;
				    }
				}
		    } catch (Exception e) {
				this.red = 0;
				this.green = 0;
				this.blue = 0;
				System.err.println("ERROR: unknown colour format. Must be #RGB or #RRGGBB");
		    }
	    }
	} else if (value.startsWith("url(")) {
		// refers to a gradient
	} else {
	    if (value.toLowerCase().equals("black")) {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("green")) {
		this.red = 0;
		this.green = 0.5f;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("silver")) {
		this.red = 0.75f;
		this.green = 0.75f;
		this.blue = 0.75f;
	    } else if (value.toLowerCase().equals("lime")) {
		this.red = 0;
		this.green = 1;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("gray")) {
		this.red = 0.5f;
		this.green = 0.5f;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("olive")) {
		this.red = 0.5f;
		this.green = 0.5f;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("white")) {
		this.red = 1;
		this.green = 1;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("yellow")) {
		this.red = 1;
		this.green = 1;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("maroon")) {
		this.red = 0.5f;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("navy")) {
		this.red = 0;
		this.green = 0;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("red")) {
		this.red = 1;
		this.green = 0;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("blue")) {
		this.red = 0;
		this.green = 0;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("purple")) {
		this.red = 0.5f;
		this.green = 0;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("teal")) {
		this.red = 0;
		this.green = 0.5f;
		this.blue = 0.5f;
	    } else if (value.toLowerCase().equals("fuchsia")) {
		this.red = 1;
		this.green = 0;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("aqua")) {
		this.red = 0;
		this.green = 1;
		this.blue = 1;
	    } else if (value.toLowerCase().equals("orange")) {
		// for compatibility with passiveTex
		this.red = 0.7f;
		this.green = 0.5f;
		this.blue = 0;
	    } else if (value.toLowerCase().equals("transparent")) {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		this.alpha = 1;
	    } else {
		this.red = 0;
		this.green = 0;
		this.blue = 0;
		System.err.println("ERROR: unknown colour name: " + value);
	    }
	}
    }
	
    public float blue() {
	return this.blue;
    }
	
    public float green() {
	return this.green;
    }
	
    public float red() {
	return this.red;
    }

    public float alpha() {
	return this.alpha;
    }
}
