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

import org.apache.fop.dom.stylesheets.*;
import org.apache.fop.dom.css.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.dom.ElementImpl;

import org.w3c.dom.css.*;
import org.w3c.dom.svg.*;
import org.w3c.dom.*;
import org.w3c.dom.stylesheets.*;

import java.util.*;

public abstract class SVGElementImpl extends ElementImpl implements SVGElement {
	String idString = "";
	CSSStyleDeclaration styleDec;
	SVGSVGElement ownerSvg;
	SVGAnimatedString className = new SVGAnimatedStringImpl("");

	public String getId()
	{
		return idString;
	}

    public String getNamespaceURI()
	{
		return SVGDocumentImpl.namespaceURI;
	}

	public void setId(String id)
	{
		idString = id;
	}

	public SVGSVGElement getOwnerSVGElement( )
	{
		return ownerSvg;
	}

	public SVGElement getViewportElement( )
	{
		return null;
	}

	public SVGAnimatedString getClassName( )
	{
		return className;
	}

	public void setClassName( SVGAnimatedString className )
	{
		this.className = className;
	}

	public CSSValue getPresentationAttribute ( String name )
	{
		CSSStyleDeclaration style;
		CSSValue val = null;

		style = getStyle();
		if(style != null) {
			val = style.getPropertyCSSValue(name);
		}

		if(val == null) {
			// this checks for the style selector matching
			// everytime a property is requested, this is bad, slow

			// get "style" element style for this
			SVGSVGElement svg = getOwnerSVGElement();
			// maybe
			// val = svg.getComputedStyle(this, name);
			StyleSheetList list = svg.getStyleSheets();
			for(int count = 0; count < list.getLength(); count++) {
				CSSRuleList rlist = ((CSSStyleSheet)list.item(count)).getCssRules();
				for(int c = 0; c < rlist.getLength(); c++) {
					CSSRule rule = rlist.item(c);
					if(rule.getType() == CSSRule.STYLE_RULE) {
						if(((CSSStyleRuleImpl)rule).matches(this)) {
							style = ((CSSStyleRule)rule).getStyle();
							val = style.getPropertyCSSValue(name);
//							break;
						}
					}
				}
//				if(val != null) {
//					break;
//				}
			}
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

	public SVGAnimatedBoolean getExternalResourcesRequired( )
	{
		return null;
	}

	public void setExternalResourcesRequired( SVGAnimatedBoolean externalResourcesRequired )
	{
	}

    public Node appendChild(Node newChild)
                                    throws DOMException
	{
		Node nChild = super.appendChild(newChild);
		if(newChild instanceof SVGElementImpl) {
			SVGElementImpl ele = (SVGElementImpl)newChild;
			if(ownerSvg != null)
				ele.setOwnerSVG(ownerSvg);
		}
		return nChild;
	}

    public void setOwnerSVG(SVGSVGElement owner)
	{
		ownerSvg = owner;
		NodeList nl = getChildNodes();
		for(int count = 0; count < nl.getLength(); count++) {
			Node n = nl.item(count);
			if(n instanceof SVGElementImpl) {
				((SVGElementImpl)n).setOwnerSVG(owner);
			}
		}
	}
}
