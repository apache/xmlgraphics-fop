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

import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;

import java.util.Hashtable;

public class SVGPropertyListMapping implements PropertyListMapping {

    public void addToBuilder(FOTreeBuilder builder) {

	String uri = "http://www.w3.org/TR/2000/WD-SVG-20000629/DTD/svg-20000629.dtd";
	Hashtable propertyTable = new Hashtable();
		propertyTable.put("height",SVGLengthProperty.maker());
		propertyTable.put("width",SVGLengthProperty.maker());

	propertyTable.put("x",SVGLengthProperty.maker());
	propertyTable.put("y",SVGLengthProperty.maker());
	propertyTable.put("x1",SVGLengthProperty.maker());
	propertyTable.put("x2",SVGLengthProperty.maker());
	propertyTable.put("y1",SVGLengthProperty.maker());
	propertyTable.put("y2",SVGLengthProperty.maker());
		propertyTable.put("rx",SVGLengthProperty.maker());
		propertyTable.put("ry",SVGLengthProperty.maker());
		propertyTable.put("dx",SVGLengthProperty.maker());
		propertyTable.put("dy",SVGLengthProperty.maker());
		propertyTable.put("cx",SVGLengthProperty.maker());
		propertyTable.put("cy",SVGLengthProperty.maker());
		propertyTable.put("r",SVGLengthProperty.maker());
		propertyTable.put("fx",SVGLengthProperty.maker());
		propertyTable.put("fy",SVGLengthProperty.maker());
		propertyTable.put("refX",SVGLengthProperty.maker());
		propertyTable.put("refY",SVGLengthProperty.maker());
		propertyTable.put("markerWidth",SVGLengthProperty.maker());
		propertyTable.put("markerHeight",SVGLengthProperty.maker());
		propertyTable.put("offset",SVGLengthProperty.maker());

/*		propertyTable.put("orient",SVGOrient.maker());*/
		propertyTable.put("xlink:href",HRef.maker());
		propertyTable.put("style",SVGStyle.maker());
		propertyTable.put("transform",SVGTransform.maker());
		propertyTable.put("d",SVGD.maker());
		propertyTable.put("points",SVGPoints.maker());
//		propertyTable.put("viewBox",SVGBox.maker());

	propertyTable.put("font-family",FontFamily.maker());
	propertyTable.put("font-style",FontStyle.maker());
	propertyTable.put("font-weight",FontWeight.maker());
	propertyTable.put("font-size",FontSize.maker());
//		propertyTable.put("id", SVGStringProperty.maker());

	propertyTable.put("id",Id.maker());			// attribute for objects

	builder.addPropertyList(uri, propertyTable);
	propertyTable = new Hashtable();
		propertyTable.put("x",SVGLengthListProperty.maker());
		propertyTable.put("y",SVGLengthListProperty.maker());
		propertyTable.put("dx",SVGLengthListProperty.maker());
		propertyTable.put("dy",SVGLengthListProperty.maker());
		builder.addElementPropertyList(uri, "tref", propertyTable);
		builder.addElementPropertyList(uri, "tspan", propertyTable);
    }
}
