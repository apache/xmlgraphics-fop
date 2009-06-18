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
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.XMLSpace;

import java.util.*;

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGTextElementImpl;
import org.apache.fop.dom.svg.SVGArea;
import org.apache.fop.dom.CharacterDataImpl;

import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.CharacterData;

/**
 * class representing svg:text pseudo flow object.
 *
 */
public class Text extends SVGObj {

	/**
	 * inner class for making SVG Text objects.
	 */
	public static class Maker extends FObj.Maker {

	/**
	 * make an SVG Text object.
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 *
	 * @return the SVG Text object
	 */
	public FObj make(FObj parent, PropertyList propertyList)
		throws FOPException {
		return new Text(parent, propertyList);
	}
	}

	/**
	 * returns the maker for this object.
	 *
	 * @return the maker for SVG Text objects
	 */
	public static FObj.Maker maker() {
	return new Text.Maker();
	}

	/**
	 * the string of text to display
	 */
	Vector textList = new Vector();

	/**
	 * constructs an SVG Text object (called by Maker).
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	protected Text(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "svg:text";
	}

	SVGTextElementImpl textGraph = new SVGTextElementImpl();

	/**
	 * add characters to the string to display.
	 *
	 * @param data array of characters
	 * @param start start offset in character array
	 * @param length number of characters to add
	 */
	protected void addCharacters(char data[], int start, int length)
	{
		textList.addElement(new String(data, start, length - start));
	}

	/**
	 * The children need to be added in order so that the text data
	 * is also added in order.
	 */
	protected void addChild(FONode child) {
		super.addChild(child);
		if(child instanceof TextElement) {
			TextElement te = (TextElement)child;
			SVGElement graph = te.createTextElement();
			textList.addElement(graph);
		} else {
			// error
		}
	}

	public SVGElement createGraphic()
	{
		int numChildren = this.textList.size();
		for (int i = 0; i < numChildren; i++) {
			Object obj = textList.elementAt(i);
			if(obj instanceof SVGElement) {
				SVGElement child = (SVGElement)obj;
				textGraph.appendChild(child);
			} else if(obj instanceof String) {
				String str = (String)obj;
				CharacterData cd = new CharacterDataImpl();
				cd.setData(str);
				textGraph.appendChild(cd);
			}
//			System.out.println(child);
		}
		/* retrieve properties */
		textGraph.x = ((SVGLengthProperty)this.properties.get("x")).getSVGLength().getValue();
		textGraph.y = ((SVGLengthProperty)this.properties.get("y")).getSVGLength().getValue();
//		textGraph.textList = textList;
		textGraph.setStyle(((SVGStyle)this.properties.get("style")).getStyle());
		textGraph.setTransform(((SVGTransform)this.properties.get("transform")).getTransform());
		textGraph.setId(this.properties.get("id").getString());
		switch((this.properties.get("xml:space")).getEnum()) {
			case XMLSpace.DEFAULT:
				textGraph.setXMLspace("default");
			break;
			case XMLSpace.PRESERVE:
				textGraph.setXMLspace("preserve");
			break;
		}
		return textGraph;
	}
}
