/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.fo;

import java.util.ArrayList;

public class MarginShorthandParser implements ShorthandParser {

    protected ArrayList list;    // ArrayList of Property objects

    public MarginShorthandParser(ListProperty listprop) {
        this.list = listprop.getList();
    }

    protected Property getElement(int index) {
        if (list.size() > index)
            return (Property)list.get(index);
        else
            return null;
    }

    protected int count() {
        return list.size();
    }

    // Stores 1 to 4 values for margin-top, -right, -bottom or -left
    public Property getValueForProperty(String propName,
                                        Property.Maker maker,
                                        PropertyList propertyList) {
        Property prop = null;
        // Check for keyword "inherit"
        if (count() == 1) {
            String sval = ((Property)list.get(0)).getString();
            if (sval != null && sval.equals("inherit")) {
                return propertyList.getFromParent(propName);
            }
        }
        return convertValueForProperty(propName, maker, propertyList);
    }


    protected Property convertValueForProperty(String propName,
            Property.Maker maker,
            PropertyList propertyList) {
        Property prop = null;
        int idx = 0;

        switch (count())
        {
        case 1: //
            idx = 0;
            break;
        case 2: // 1st value top/bottom, 2nd value left/right
            if (propName.equals("margin-top") ||
                    propName.equals("margin-bottom"))
                idx = 0;
            else
                idx = 1;
            break;
        case 3: // 1st value top, 2nd left/right, 3rd bottom
            if (propName == "margin-top")
                idx = 0;
            else if (propName.equals("margin-bottom"))
                idx = 2;
            else
                idx = 1;
            break;
        case 4: // top, right, bottom, left
            if (propName.equals("margin-top"))
                idx = 0;
            else if (propName.equals("margin-right"))
                idx = 1;
            else if (propName.equals("margin-bottom"))
                idx = 2;
            else if (propName.equals("margin-left"))
                idx = 3;
            break;
        default:
            // TODO Error Message: Wrong number of args
            return null;
        }

        Property p = getElement(idx);
        prop = maker.convertShorthandProperty(propertyList, p, null);
        return prop;
    }

}

