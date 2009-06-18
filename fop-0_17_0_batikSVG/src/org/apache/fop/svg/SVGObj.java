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

package org.apache.fop.svg;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;

import org.apache.batik.dom.svg.*;

import org.w3c.dom.svg.*;
import org.w3c.dom.*;

import java.util.*;

/**
 * Since SVG objects are not layed out then this class checks
 * that this element is not being layed out inside some incorrect
 * element.
 */
public abstract class SVGObj extends FObj implements GraphicsCreator {

	String tagName = "";
	String[] props = {};
	/**
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	public SVGObj(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
	}

    protected static Hashtable ns = new Hashtable();

    public void addGraphic(Document doc, Element parent) {
        Element element = doc.createElementNS("http://www.w3.org/2000/svg", tagName);
//        Element element = doc.createElement(tagName);
        for(int count = 0; count < props.length; count++) {
            if(this.properties.get(props[count]) != null) {
                String rf = this.properties.get(props[count]).getString();
                if(rf != null) {
                    if(props[count].indexOf(":") == -1) {
                        element.setAttribute(props[count], rf);
                    } else {
                        String pref = props[count].substring(0, props[count].indexOf(":"));
System.out.println(pref);
                        if(pref.equals("xmlns")) {
                            ns.put(props[count].substring(props[count].indexOf(":") + 1), rf);
System.out.println(ns);
                        }
                        ns.put("xlink", "http://www.w3.org/1999/xlink");
                        element.setAttributeNS((String)ns.get(pref), props[count], rf);
                    }
                }
            }
        }
        parent.appendChild(element);
        int numChildren = this.children.size();
        for (int i = 0; i < numChildren; i++) {
            Object child = children.elementAt(i);
            if (child instanceof GraphicsCreator) {
                ((GraphicsCreator)child).addGraphic(doc, element);
            } else if (child instanceof String) {
                org.w3c.dom.Text text = doc.createTextNode((String)child);
                element.appendChild(text);
            }
        }
    }

	/**
	 * layout this formatting object.
	 *
	 * @param area the area to layout the object into
	 * @return the status of the layout
	 */
	public Status layout(Area area) throws FOPException
	{
//		if (area instanceof SVGArea) {
//		} else {
			/* otherwise generate a warning */
			System.err.println("WARNING: " + this.name + " outside svg:svg");
//		}

		/* return status */
		return new Status(Status.OK);
	}
}
