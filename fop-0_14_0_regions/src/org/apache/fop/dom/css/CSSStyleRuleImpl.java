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

package org.apache.fop.dom.css;

//import org.apache.fop.dom.svg.*;
import org.w3c.dom.svg.*;

import org.w3c.dom.css.*;
import org.w3c.dom.*;
import org.w3c.dom.stylesheets.*;

import java.util.*;

/**
 *
 *
 */
public class CSSStyleRuleImpl implements CSSStyleRule {
    String seltext;
    String cssText;
    CSSStyleDeclaration style;
    CSSStyleSheet styleSheet;
    Vector selectors = new Vector();

	public CSSStyleRuleImpl(CSSStyleSheet styleSheet)
	{
	    this.styleSheet = styleSheet;
	}

    public String getSelectorText()
    {
        return seltext;
    }

    /**
     * Selectors are comma separated values (except for attributes)
     * of all the selectors for the style.
     */
    public void setSelectorText(String selectorText)
    {
        seltext = selectorText;
        parseSelector();
    }

    protected void parseSelector()
    {
        // need to handle "," in attribute selectors
        StringTokenizer st = new StringTokenizer(seltext, ",");
        while(st.hasMoreTokens()) {
            String sel = st.nextToken().trim();
            StringTokenizer subt = new StringTokenizer(sel, " \t\n\r");
            Selector last = null;
            Selector current = null;
            int compoundstate = 0;
            while(subt.hasMoreTokens()) {
                String str = subt.nextToken().trim();
                current = new Selector();
                boolean compounding = false;
                if(str.equals("*")) {
                    // all
                    current.type = Selector.ALL;
                    current.value = "*";
                } else if(str.equals(">")) {
                    // child of
                    compoundstate = 1;
                    compounding = true;
                } else if(str.equals("+")) {
                    // precedent of
                    compoundstate = 2;
                    compounding = true;
                } else if(str.startsWith("#")) {
                    // id
                    current.type = Selector.ID;
                    current.value = str.substring(1);
                } else if(str.startsWith(".")) {
                    // class
                    current.type = Selector.CLASS;
                    current.value = str.substring(1);
                } else if(str.startsWith("[")) {
                    // attribute
                    current.type = Selector.ATTRIBUTE;
                    current.value = str.substring(1, str.length() - 1);
                } else {
                    // tag
                    current.type = Selector.TAG;
                    if(str.indexOf(":") != -1) {
                        current.type = Selector.TAG_FIRST_CHILD;
                        int pos = str.indexOf(":");
                        current.value = str.substring(0, pos);
                        current.subValue = str.substring(pos + 1);
                    } else if(str.indexOf("#") != -1) {
                        current.type = Selector.TAG_ID;
                        int pos = str.indexOf("#");
                        current.value = str.substring(0, pos);
                        current.subValue = str.substring(pos + 1);
                    } else if(str.indexOf(".") != -1) {
                        current.type = Selector.TAG_CLASS;
                        int pos = str.indexOf(".");
                        current.value = str.substring(0, pos);
                        current.subValue = str.substring(pos + 1);
                    } else {
                        current.value = str;
                    }
                }
                if(!compounding) {
                    switch(compoundstate) {
                        case 0:
                            if(last != null) {
                                Selector compound = new Selector();
                                compound.type = Selector.DESCENDANT;
                                compound.last = last;
                                compound.current = current;
                                current = compound;
                                compoundstate = 0;
                            }
                        break;
                        case 1:
                            {
                                Selector compound = new Selector();
                                compound.type = Selector.CHILD;
                                compound.last = last;
                                compound.current = current;
                                current = compound;
                                compoundstate = 0;
                            }
                        break;
                        case 2:
                            {
                                Selector compound = new Selector();
                                compound.type = Selector.PRECEDENT;
                                compound.last = last;
                                compound.current = current;
                                current = compound;
                                compoundstate = 0;
                            }
                        break;
                    }
                    last = current;
                }
            }
            if(current != null)
                selectors.add(current);
        }
    }

    public CSSStyleDeclaration getStyle()
    {
        return style;
    }

    public String getCssText()
    {
        return cssText;
    }

    public void setCssText(String cssText)
    {
        this.cssText = cssText;
        style = new CSSStyleDeclarationImpl();
        style.setCssText(cssText);
    }

    public short getType()
    {
        return STYLE_RULE;
    }

    public CSSStyleSheet getParentStyleSheet()
    {
        return styleSheet;
    }

    public CSSRule getParentRule()
    {
        return null;
    }

    /**
     * This should probably be elsewhere, in dom.svg
     */
    public boolean matches(SVGElement el)
    {
        for(Enumeration e = selectors.elements(); e.hasMoreElements(); ) {
            Selector sel = (Selector)e.nextElement();
            if(matches(el, sel))
                return true;
        }
        return false;
    }

    protected boolean matches(SVGElement el, Selector sel)
    {
        short type = sel.type;
        if(el == null)
            return false;
        switch(type) {
            case Selector.ALL:
                return true;
//            break;
            case Selector.ID:
                return el.getId().equals(sel.value);
//            break;
            case Selector.CLASS:
                return ((SVGStylable)el).getClassName().getBaseVal().equals(sel.value);
//            break;
            case Selector.ATTRIBUTE:
//System.out.println(sel.value + ":" + el.getAttribute(sel.value));
//                    return el.getAttribute(sel.value);
            break;
            case Selector.TAG:
                return sel.value.equals(el.getTagName());
//            break;
            case Selector.TAG_FIRST_CHILD:
                if(el.getParentNode() != null)
                    return (el.getParentNode().getFirstChild() == el);
            break;
            case Selector.TAG_ID:
                return sel.value.equals(el.getTagName())
                        && sel.subValue.equals(el.getId());
//            break;
            case Selector.TAG_CLASS:
                return sel.value.equals(el.getTagName())
                        && sel.subValue.equals(((SVGStylable)el).getClassName().getBaseVal());
//            break;
            case Selector.DESCENDANT:
                if(el.getParentNode() instanceof SVGElement) {
                    if(!matches(el, sel.current))
                        return false;
                    SVGElement parent = (SVGElement)el.getParentNode();
                    while(parent != null) {
                        if(matches(parent, sel.last))
                            return true;
                        if(parent.getParentNode() instanceof SVGElement) {
                            parent = (SVGElement)parent.getParentNode();
                        } else {
                            return false;
                        }
                    }
                }
                return false;
//            break;
            case Selector.CHILD:
                return matches(el, sel.current)
                        && matches((SVGElement)el.getParentNode(), sel.last);
//            break;
            case Selector.PRECEDENT:
                return matches(el, sel.current)
                        && matches((SVGElement)el.getPreviousSibling(), sel.last);
//            break;
        }
        return false;
    }
}

class Selector {
	final static short NONE = -1;
	final static short ALL = 0;
	final static short ID = 1;
	final static short CLASS = 2;
	final static short ATTRIBUTE = 3;
	final static short TAG = 4;
	final static short DESCENDANT = 5;
	final static short CHILD = 6;
	final static short PRECEDENT = 7;
	final static short TAG_FIRST_CHILD = 8;
	final static short TAG_ID = 9;
	final static short TAG_CLASS = 10;
	short type = NONE;
	String value = "";
	String subValue = "";
	// pre- simple selector in compound selectors
	Selector last = null;
	Selector current = null;
}
