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

import org.w3c.dom.css.*;
import org.w3c.dom.*;
import org.w3c.dom.stylesheets.*;

import java.util.*;

/**
 *
 *
 */
public class CSSStyleSheetImpl implements CSSStyleSheet {
    Vector rules = new Vector();

	public CSSStyleSheetImpl(String sheet)
	{
		parseSheet(sheet);
	}

	protected void parseSheet(String str)
	{
		// get each rule and insert it
		// rule ends with }
		// need to ignore comments
		String newStr = str;
		int pos1;
		int pos2;
		pos1 = newStr.indexOf("/*");
		pos2 = newStr.indexOf("*/");
		while((pos1 != -1) && (pos2 != -1)) {
		    newStr = newStr.substring(0, pos1) + newStr.substring(pos2 + 2, newStr.length());
	    	pos1 = newStr.indexOf("/*");
    		pos2 = newStr.indexOf("*/");
		}
		StringTokenizer st = new StringTokenizer(newStr, "}");
		while(st.hasMoreTokens()) {
		    String rule = st.nextToken() + "}";
		    insertRule(rule, rules.size());
		}
	}

    public CSSRule getOwnerRule()
    {
        // since this is for a style element
        return null;
    }

    public CSSRuleList getCssRules()
    {
        return new CSSRuleListImpl(rules);
    }

    public int insertRule(String rule, 
                          int index)
                          throws DOMException
    {
        CSSRule r = parseRule(rule);
        if(r != null)
            rules.addElement(r);
        return 0;
    }

    public void deleteRule(int index)
                           throws DOMException
    {
        rules.remove(index);
    }

    public String getType()
    {
        return "text/css";
    }

    public boolean getDisabled()
    {
        return false;
    }

    public void setDisabled(boolean disabled)
    {
    }

    public Node getOwnerNode()
    {
        // return the style element
        return null;
    }

    public StyleSheet getParentStyleSheet()
    {
        return null;
    }

    public String getHref()
    {
        return null;
    }

    public String getTitle()
    {
        return null;
    }

    public MediaList getMedia()
    {
        return null;
    }

    protected CSSRule parseRule(String str)
    {
        // a rule is "selectors {style}"
        // a list of selectors followed by the style statement in brackets
        int pos1 = str.indexOf("{");
        int pos2 = str.indexOf("}");
        if(pos1 == -1 || pos2 == -1)
            return null;
        String sel = str.substring(0, pos1);
        String style = str.substring(pos1 + 1, pos2);
        CSSStyleRule rule = new CSSStyleRuleImpl(this);
        rule.setSelectorText(sel);
        rule.setCssText(style);
        return rule;
    }
}

class CSSRuleListImpl implements CSSRuleList {
    Vector rules;
    CSSRuleListImpl(Vector v)
    {
        rules = v;
    }

    public int getLength()
    {
        return rules.size();
    }

    public CSSRule item(int pos)
    {
        return (CSSRule)rules.elementAt(pos);
    }
}
