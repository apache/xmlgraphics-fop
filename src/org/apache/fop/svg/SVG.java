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
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.inline.*;

import org.apache.batik.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.svg.SVGLength;

import java.io.File;

/**
 * class representing svg:svg pseudo flow object.
 */
public class SVG extends SVGObj implements GraphicsCreator {

		/**
		 * inner class for making SVG objects.
		 */
		public static class Maker extends FObj.Maker {

				/**
				 * make an SVG object.
				 *
				 * @param parent the parent formatting object
				 * @param propertyList the explicit properties of this object
				 *
				 * @return the SVG object
				 */
				public FObj make(FObj parent,
												 PropertyList propertyList) throws FOPException {
						return new SVG(parent, propertyList);
				}
		}

		/**
		 * returns the maker for this object.
		 *
		 * @return the maker for SVG objects
		 */
		public static FObj.Maker maker() {
				return new SVG.Maker();
		}

		FontState fs;

		/**
		 * constructs an SVG object (called by Maker).
		 *
		 * @param parent the parent formatting object
		 * @param propertyList the explicit properties of this object
		 */
		public SVG(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "svg:svg";
	tagName = "svg";
	props = new String[] {"width", "height", "x", "y", "id", "style", "class", "visibility",
        "id", 
        "class", 
    
            
        
        "enable-background", 
    
        
        "flood-color", 
        "flood-opacity", 
    
        
        "fill", 
        "fill-opacity", 
        "fill-rule", 
        "stroke", 
        "stroke-dasharray", 
        "stroke-dashoffset", 
        "stroke-linecap", 
        "stroke-linejoin", 
        "stroke-miterlimit", 
        "stroke-opacity", 
        "stroke-width", 
    
        
        "font-family", 
        "font-size", 
        "font-size-adjust", 
        "font-stretch", 
        "font-style", 
        "font-variant", 
        "font-weight", 
    
        
        "stop-color", 
        "stop-opacity", 
    
        
        "clip-path", 
        "clip-rule", 
        "color", 
        "color-interpolation", 
        "color-rendering", 
        "cursor", 
        "display", 
        "filter", 
        "image-rendering", 
        "mask", 
        "opacity", 
        "pointer-events", 
        "space-rendering", 
        "text-rendering", 
        "visibility", 
    
        
        "color-profile", 
    
        
        "lighting-color", 
    
        
        "marker-start", 
        "marker-mid", 
        "marker-end", 
    
        
        "alignment-baseline", 
        "baseline-shift", 
        "direction", 
        "glyph-orientation-horizontal", 
        "glyph-orientation-vertical", 
        "kerning", 
        "letter-spacing", 
        "text-decoration", 
        "unicode-bidi", 
        "word-spacing", 
    
        
        "writing-mode", 
        "text-anchor", 
        "dominant-baseline", 
    
        
        "clip", 
        "overflow", 
    
    
            "id", 
            "style", 
            "transform", 
            "class", 
	};
		}

/*    public void addGraphic(Document doc, Element parent) {
        Element element = doc.createElement(tagName);
        for(int count = 0; count < props.length; count++) {
            String rf = this.properties.get(props[count]).getString();
            element.setAttribute(props[count], rf);
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
    }*/

/*		public SVGElement createGraphic() {
				SVGSVGElementImpl svgArea = null;
				SVGLength w = ((SVGLengthProperty) this.properties.get("width")).
											getSVGLength();
				SVGLength h = ((SVGLengthProperty) this.properties.get("height")).
											getSVGLength();
				svgArea = new SVGSVGElementImpl();
				SVGAnimatedLengthImpl sal;
				if (w == null)
						w = new SVGLengthImpl();
				sal = new SVGAnimatedLengthImpl(w);
				sal.setBaseVal(w);
				svgArea.setWidth(sal);
				if (h == null)
						h = new SVGLengthImpl();
				sal = new SVGAnimatedLengthImpl(h);
				sal.setBaseVal(h);
				svgArea.setHeight(sal);
				SVGLength lengthProp =
					((SVGLengthProperty) this.properties.get("x")).
					getSVGLength();
				SVGLength x = lengthProp == null ? new SVGLengthImpl() : lengthProp;
				sal = new SVGAnimatedLengthImpl(x);
				sal.setBaseVal(x);
				svgArea.setX(sal);
				lengthProp = ((SVGLengthProperty) this.properties.get("y")).
										 getSVGLength();
				SVGLength y = lengthProp == null ? new SVGLengthImpl() : lengthProp;
				sal = new SVGAnimatedLengthImpl(y);
				sal.setBaseVal(y);
				svgArea.setY(sal);

				svgArea.setStyle(
					((SVGStyle) this.properties.get("style")).getStyle());
				svgArea.setTransform(
					((SVGTransform) this.properties.get("transform")).
					getTransform());
				svgArea.setId(this.properties.get("id").getString());
				int numChildren = this.children.size();
				for (int i = 0; i < numChildren; i++) {
						FONode fo = (FONode) children.elementAt(i);
						if (fo instanceof GraphicsCreator) {
								SVGElement impl = ((GraphicsCreator) fo).createGraphic();
								if (impl != null) {
										if (impl instanceof SVGElementImpl)
												((SVGElementImpl) impl).setClassName(
													new SVGAnimatedStringImpl(
														((FObj) fo).getProperty(
															"class").getString()));
										svgArea.appendChild((org.w3c.dom.Node) impl);
								}
								//			} else if(fo instanceof Defs) {
								//				svgArea.addDefs(((Defs)fo).createDefs());
						}
						Status status;
				}
				return svgArea;
		}*/

		/**
		 * layout this formatting object.
		 *
		 * @param area the area to layout the object into
		 *
		 * @return the status of the layout
		 */
		public Status layout(Area area) throws FOPException {

				if (!(area instanceof ForeignObjectArea)) {
						// this is an error
						throw new FOPException("SVG not in fo:instream-foreign-object");
				}

				if (this.marker == BREAK_AFTER) {
						return new Status(Status.OK);
				}

				if (this.marker == START) {
						/* retrieve properties */
						String id = this.properties.get("id").getString();
						String fontFamily =
							this.properties.get("font-family").getString();
						String fontStyle =
							this.properties.get("font-style").getString();
						String fontWeight =
							this.properties.get("font-weight").getString();
						String fontSz = this.properties.get("font-size").getString();
						int fontSize = area.getFontState().getFontSize();
						try {
						    fontSize = Integer.parseInt(fontSz);
						} catch(Exception e) {
						}

						// FIX-ME: should get the font-variant property
						this.fs = new FontState(area.getFontInfo(), fontFamily,
																		fontStyle, fontWeight, fontSize, FontVariant.NORMAL);

//						this.width = this.properties.get("width").getString();
//						this.height = this.properties.get("height").getString();

						this.marker = 0;
				}

				/* create an SVG area */
				/* if width and height are zero, may want to get the bounds of the content. */
				SVGOMDocument doc = new SVGOMDocument(null, SVGDOMImplementation.getDOMImplementation());
                                try {
                                    doc.setURLObject(new File(".").toURL());
                                } catch(Exception e) {
                                }

                DefaultSVGContext dc = new DefaultSVGContext() {
                        public float getPixelToMM() {
                            return 0.264583333333333333333f; // 72 dpi
                        }
                        public float getViewportWidth() {
                            return 100;
                        }
                        public float getViewportHeight() {
                            return 100;
                        }
                    };
                doc.setSVGContext(dc);

                Element topLevel = doc.createElementNS("http://www.w3.org/2000/svg", tagName);
                for(int count = 0; count < props.length; count++) {
                    if(this.properties.get(props[count]) != null) {
                        String rf = this.properties.get(props[count]).getString();
                        if(rf != null)
                            topLevel.setAttribute(props[count], rf);
                    }
                }
				doc.appendChild(topLevel);
                int numChildren = this.children.size();
                for (int i = 0; i < numChildren; i++) {
                    Object child = children.elementAt(i);
                    if (child instanceof GraphicsCreator) {
                        ((GraphicsCreator)child).addGraphic(doc, topLevel);
                    } else if (child instanceof String) {
                        org.w3c.dom.Text text = doc.createTextNode((String)child);
                        topLevel.appendChild(text);
                    }
                }

		        float width = ((SVGSVGElement)topLevel).getWidth().getBaseVal().getValue();
        		float height = ((SVGSVGElement)topLevel).getHeight().getBaseVal().getValue();
				SVGArea svg = new SVGArea(fs, width, height);
				svg.setSVGDocument(doc);
				svg.start();

				/* finish off the SVG area */
				svg.end();

				/* add the SVG area to the containing area */
				ForeignObjectArea foa = (ForeignObjectArea) area;
				foa.setObject(svg);
				foa.setIntrinsicWidth(svg.getWidth());
				foa.setIntrinsicHeight(svg.getHeight());

				/* return status */
				return new Status(Status.OK);
		}
}
