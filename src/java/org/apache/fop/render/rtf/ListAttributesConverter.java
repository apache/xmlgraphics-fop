/*
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
package org.apache.fop.render.rtf;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Property;
import org.apache.fop.fo.LengthProperty;
import org.apache.fop.fo.PropertyList;

//RTF
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfAttributes;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListTable;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfText;

 /**
  * @autor bdelacretaz, bdelacretaz@codeconsult.ch
  * @autor Christopher Scott, scottc@westinghouse.com
  * Portions created by Christopher Scott are Coypright (C) 2001
  * Westinghouse Electric Company. All Rights Reserved.
  * @autor Peter Herweg, pherweg@web.de
  */

/**
 * Provides methods to convert list attributes to RtfAttributes.
 */
public class ListAttributesConverter {
    
    
    static RtfAttributes convertAttributes(PropertyList properties)
    throws FOPException{
        
        RtfAttributes attrib = new RtfAttributes();
        
        Property prop=null;
        int iStartIndentInTwips=0;
        
        //start-indent
        if ((prop = properties.get("start-indent")) != null) {
            LengthProperty lengthprop = (LengthProperty)prop;

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";
            
            iStartIndentInTwips = (int) FoUnitsConverter.getInstance().convertToTwips(sValue);
        } else {
            //set default 
            iStartIndentInTwips = 360;
        }
        attrib.set(RtfListTable.LIST_INDENT, iStartIndentInTwips);
        
        //end-indent
        if ((prop = properties.get("end-indent")) != null) {
            LengthProperty lengthprop = (LengthProperty)prop;

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(RtfText.LEFT_INDENT_BODY,
                    (int) FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else {
            if(iStartIndentInTwips >= 360) {
                //if the start indent is greater than default, set to the start indent
                attrib.set(RtfText.LEFT_INDENT_BODY, iStartIndentInTwips);
            } else {
                //else set to default 
                attrib.set(RtfText.LEFT_INDENT_BODY, 360);
            }
        }
        
        /*
         * set list table defaults
         */

        //set a simple list type
        attrib.set(RtfListTable.LIST, "simple");
        //set following char as tab
        attrib.set(RtfListTable.LIST_FOLLOWING_CHAR, 0);
        
        return attrib;
    }
}