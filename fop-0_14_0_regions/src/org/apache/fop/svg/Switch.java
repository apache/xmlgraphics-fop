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

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGArea;

import org.w3c.dom.svg.*;

/**
 *
 */
public class Switch extends SVGObj {

	/**
	 * inner class for making Line objects.
	 */
	public static class Maker extends FObj.Maker {

		/**
		 * make a Line object.
		 *
		 * @param parent the parent formatting object
		 * @param propertyList the explicit properties of this object
		 *
		 * @return the Line object
		 */
		public FObj make(FObj parent, PropertyList propertyList) throws FOPException
		{
			return new Switch(parent, propertyList);
		}
	}

	/**
	 * returns the maker for this object.
	 *
	 * @return the maker for Switch objects
	 */
	public static FObj.Maker maker() {
		return new Switch.Maker();
	}

	/**
	 * constructs a Switch object (called by Maker).
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	protected Switch(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "svg:switch";
	}

	public SVGElement createGraphic()
	{
		/*
		 * There are two options
		 * 1) add all children and select the correct one when rendering
		 * 2) select the correct one now and return it rather than a switch element
		 * Since renderers may have different ideas, leave it up to the renderer
		 * to select the correct one.
		 */
		String rf = this.properties.get("requiredFeatures").getString();
		String re = this.properties.get("requiredExtensions").getString();
		String sl = this.properties.get("systemLanguage").getString();
		SVGList strlist;
		GraphicElement graphic;
		graphic = new SVGSwitchElementImpl();
		if(!rf.equals("notpresent")) {
    		strlist = new SVGStringList(rf);
			graphic.setRequiredFeatures(strlist);
    	}
		if(!re.equals("notpresent")) {
			strlist = new SVGStringList(re);
			graphic.setRequiredExtensions(strlist);
		}
		if(!sl.equals("notpresent")) {
			strlist = new SVGStringList(sl);
			graphic.setSystemLanguage(strlist);
		}

		int numChildren = this.children.size();
		for (int i = 0; i < numChildren; i++) {
			FONode child = (FONode) children.elementAt(i);
			if(child instanceof GraphicsCreator) {
				SVGElement impl = ((GraphicsCreator)child).createGraphic();
				if(impl instanceof SVGTests) {
					SVGTests testable = (SVGTests)impl;
					rf = child.getProperty("requiredFeatures").getString();
					re = child.getProperty("requiredExtensions").getString();
					sl = child.getProperty("systemLanguage").getString();
					if(!rf.equals("notpresent")) {
						strlist = new SVGStringList(rf);
						testable.setRequiredFeatures(strlist);
					}
					if(!re.equals("notpresent")) {
						strlist = new SVGStringList(re);
						testable.setRequiredExtensions(strlist);
					}
					if(!sl.equals("notpresent")) {
						strlist = new SVGStringList(sl);
						testable.setSystemLanguage(strlist);
					}
					graphic.appendChild((GraphicElement)impl);
				}
			} else if(child instanceof Defs) {
			}
		}

		return graphic;
	}
}
