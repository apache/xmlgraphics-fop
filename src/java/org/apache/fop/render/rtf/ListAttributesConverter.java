/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.rtf;

//FOP
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.properties.LengthProperty;
import org.apache.fop.fo.properties.Property;

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
    
    
    static RtfAttributes convertAttributes(FObj fobj)
    throws FOPException {
        
        RtfAttributes attrib = new RtfAttributes();
        
        Property prop = null;
        int iStartIndentInTwips = 0;
        
        //start-indent
        if ((prop = fobj.getProperty(Constants.PR_START_INDENT)) != null) {
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
        if ((prop = fobj.getProperty(Constants.PR_END_INDENT)) != null) {
            LengthProperty lengthprop = (LengthProperty)prop;

            Float f = new Float(lengthprop.getLength().getValue() / 1000f);
            String sValue = f.toString() + "pt";

            attrib.set(RtfText.LEFT_INDENT_BODY,
                    (int) FoUnitsConverter.getInstance().convertToTwips(sValue));
        } else {
            if (iStartIndentInTwips >= 360) {
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