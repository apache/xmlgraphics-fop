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
import org.apache.fop.fo.properties.*;

import org.apache.fop.dom.svg.*;
import org.apache.fop.dom.svg.SVGArea;

import org.w3c.dom.svg.*;
/**
 * class representing svg:Pattern pseudo flow object.
 *
 */
public class Pattern extends SVGObj {

	/**
	 * inner class for making Pattern objects.
	 */
	public static class Maker extends FObj.Maker {

		/**
		 * make a Pattern object.
		 *
		 * @param parent the parent formatting object
		 * @param propertyList the explicit properties of this object
		 *
		 * @return the Pattern object
		 */
		public FObj make(FObj parent, PropertyList propertyList) throws FOPException
		{
			return new Pattern(parent, propertyList);
		}
	}

	/**
	 * returns the maker for this object.
	 *
	 * @return the maker for Pattern objects
	 */
	public static FObj.Maker maker() {
		return new Pattern.Maker();
	}

	/**
	 * constructs a Pattern object (called by Maker).
	 *
	 * @param parent the parent formatting object
	 * @param propertyList the explicit properties of this object
	 */
	protected Pattern(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "svg:pattern";
	}

    public SVGElement createGraphic() {
    	SVGPatternElementImpl pattern = new SVGPatternElementImpl();
        String rf = this.properties.get("xlink:href").getString();
        pattern.setHref(new SVGAnimatedStringImpl(rf));
        SVGLength width =
          ((SVGLengthProperty) this.properties.get("width")).
          getSVGLength();
        SVGLength height =
          ((SVGLengthProperty) this.properties.get("height")).
          getSVGLength();
        SVGLength x =
          ((SVGLengthProperty) this.properties.get("x")).
          getSVGLength();
        SVGLength y = ((SVGLengthProperty) this.properties.get("y")).
                     getSVGLength();
        pattern.setX(x == null ? null : new SVGAnimatedLengthImpl(x));
        pattern.setY(y == null ? null : new SVGAnimatedLengthImpl(y));
        pattern.setWidth(width == null ? null : new SVGAnimatedLengthImpl(width));
        pattern.setHeight(height == null ? null : new SVGAnimatedLengthImpl(height));
        pattern.setPatternTransform(((SVGTransform) this.properties.get("transform")).
          getTransform());
        pattern.setId(this.properties.get("id").getString());
		int numChildren = this.children.size();
		for (int i = 0; i < numChildren; i++) {
			FONode child = (FONode) children.elementAt(i);
			if(child instanceof GraphicsCreator) {
				SVGElement impl = ((GraphicsCreator)child).createGraphic();
				if(impl != null) {
					if(impl instanceof SVGElementImpl)
						((SVGElementImpl)impl).setClassName(new SVGAnimatedStringImpl(((FObj)child).getProperty("class").getString()));
					pattern.appendChild(impl);
				}
			}
		}
        switch ((this.properties.get("gradientUnits")).getEnum()) {
            case GradientUnits.USER_SPACE:
                pattern.setPatternUnits( new SVGAnimatedEnumerationImpl(
                                           SVGUnitTypes.SVG_UNIT_TYPE_USERSPACE));
                break;
            case GradientUnits.USER_SPACE_ON_USE:
                pattern.setPatternUnits( new SVGAnimatedEnumerationImpl(
                                           SVGUnitTypes.SVG_UNIT_TYPE_USERSPACEONUSE));
                break;
            case GradientUnits.OBJECT_BOUNDING_BOX:
                pattern.setPatternUnits( new SVGAnimatedEnumerationImpl(
                                           SVGUnitTypes.SVG_UNIT_TYPE_OBJECTBOUNDINGBOX));
                break;
        }
		return pattern;
    }
}
