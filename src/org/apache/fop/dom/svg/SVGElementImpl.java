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
package org.apache.fop.dom.svg;

import org.apache.fop.datatypes.*;

import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.*;
import org.w3c.dom.*;

import java.util.*;

public abstract class SVGElementImpl extends ElementImpl implements SVGElement {
	String idString = "";
	CSSStyleDeclaration styleDec;

	public String getId()
	{
		return idString;
	}

	public void setId(String id)
	{
		idString = id;
	}

	public SVGSVGElement getOwnerSVGElement( )
	{
		return null;
	}

	public SVGElement getViewportElement( )
	{
		return null;
	}

	public SVGAnimatedString getClassName( )
	{
		return null;
	}

	public void setClassName( SVGAnimatedString className )
	{
	}

	public CSSValue getPresentationAttribute ( String name )
	{
		CSSStyleDeclaration style;
		style = getStyle();
		CSSValue val;
		val = style.getPropertyCSSValue(name);
		if(val == null) {
			// get "style" element style for this
			SVGSVGElement svg = getOwnerSVGElement();
		}
		if(val == null) {
			// get element parents style
			Node par = getParentNode();
			if(par instanceof SVGStylable) {
				val = ((SVGStylable)par).getPresentationAttribute(name);
			}
		}
		return val;
	}

	public CSSValue getAnimatedPresentationAttribute ( String name )
	{
		return getPresentationAttribute(name);
	}

	public CSSStyleDeclaration getStyle( )
	{
		return styleDec;
	}

	public void setStyle(CSSStyleDeclaration dec)
	{
		styleDec = dec;
	}

/*	SVGElement parent = null;
	public SVGElement getGraphicParent()
	{
		return parent;
	}

	public void setParent(SVGElement graph)
	{
		parent = graph;
	}*/

/*	Hashtable style = null;
	public void setStyle(Hashtable st)
	{
		style = st;
	}

	public Hashtable oldgetStyle()
	{
		Hashtable ret = null;
		if(parent != null) {
			ret = parent.oldgetStyle();
			if(ret != null)
				ret = (Hashtable)ret.clone();
		}
		if(ret == null) {
			ret = style;
		} else {
			if(style != null) {
				for(Enumeration e = style.keys(); e.hasMoreElements(); ) {
					String str = (String)e.nextElement();
					ret.put(str, style.get(str));
				}
			}
		}
		return ret;
	}

	Hashtable defs = new Hashtable();
	public void addDefs(Hashtable table)
	{
		for(Enumeration e = table.keys(); e.hasMoreElements(); ) {
			String str = (String)e.nextElement();
			defs.put(str, table.get(str));
		}
	}

	public Hashtable getDefs()
	{
		Hashtable ret = null;
		if(parent != null) {
			ret = parent.getDefs();
			if(ret != null)
				ret = (Hashtable)ret.clone();
		}
		if(ret == null) {
			ret = defs;
		} else {
			if(defs != null) {
				for(Enumeration e = defs.keys(); e.hasMoreElements(); ) {
					String str = (String)e.nextElement();
					ret.put(str, defs.get(str));
				}
			}
		}
		return ret;
	}

	public SVGElement locateDef(String str)
	{
		Object obj = null;
		if(defs != null) {
			obj = defs.get(str);
		}
		if(obj == null) {
			NodeList list = getChildNodes();
			for(int count = 0; count < list.getLength(); count++) {
				Object o = list.item(count);
				if(o instanceof SVGElement) {
					String s;
					s = ((SVGElement)o).getId();
					if(str.equals(s)) {
						obj = o;
						break;
					}
				}
			}
		}
		if(obj == null && parent != null) {
			obj = parent.locateDef(str);
		}
		return (SVGElement)obj;
	}

	Vector trans = null;
	public void setTransform(Vector tr)
	{
		trans = tr;
	}

	public Vector oldgetTransform()
	{
		return trans;
/*		Vector ret = null;
		if(parent != null) {
			ret = parent.oldgetTransform();
			if(ret != null)
				ret = (Vector)ret.clone();
		}
		if(ret == null) {
			ret = trans;
		} else {
			if(trans != null) {
				for(Enumeration e = trans.elements(); e.hasMoreElements(); ) {
					Object o = e.nextElement();
					ret.addElement(o);
				}
			}
		}
		return ret;*
	}
*/
	public SVGAnimatedBoolean getExternalResourcesRequired( )
	{
		return null;
	}

	public void setExternalResourcesRequired( SVGAnimatedBoolean externalResourcesRequired )
	{
	}
}
