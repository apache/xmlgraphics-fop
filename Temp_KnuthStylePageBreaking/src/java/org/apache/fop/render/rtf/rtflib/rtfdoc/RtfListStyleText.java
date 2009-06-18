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


/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */
package org.apache.fop.render.rtf.rtflib.rtfdoc;

//Java
import java.io.IOException;

//FOP
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfElement;

public class RtfListStyleText extends RtfListStyle {
    private String text;
    
    public RtfListStyleText(String s) {
        text = s;    
    }
    
    /**
     * Gets call before a RtfListItem has to be written.
     * 
     * @see org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListStyle#writeListPrefix(RtfListItem)
     */
    public void writeListPrefix(RtfListItem item)
    throws IOException {
        // bulleted list
        item.writeControlWord("pnlvlblt");
        item.writeControlWord("ilvl0");
        item.writeOneAttribute(RtfListTable.LIST_NUMBER, new Integer(item.getNumber()));
        item.writeOneAttribute("pnindent",
                item.getParentList().attrib.getValue(RtfListTable.LIST_INDENT));
        item.writeControlWord("pnf1");
        item.writeGroupMark(true);
        item.writeControlWord("pndec");
        item.writeOneAttribute(RtfListTable.LIST_FONT_TYPE, "2");
        item.writeControlWord("pntxtb " + text);
        item.writeGroupMark(false);
    }
    
    /**
     * Gets call before a paragraph, which is contained by a RtfListItem has to be written.
     * 
     * @see org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListStyle#writeParagraphPrefix(RtfElement)
     */
    public void writeParagraphPrefix(RtfElement element)
    throws IOException {
        element.writeGroupMark(true);
        element.writeControlWord("pntext");
        element.writeGroupMark(false);
    }
    
    /**
     * Gets call when the list table has to be written.
     * 
     * @see org.apache.fop.render.rtf.rtflib.rtfdoc.RtfListStyle#writeLevelGroup(RtfElement)
     */
    public void writeLevelGroup(RtfElement element)
    throws IOException {
        element.attrib.set(RtfListTable.LIST_NUMBER_TYPE, 23);
        element.writeGroupMark(true);
        
        String sCount;
        if (text.length() < 10) {
            sCount = "0" + String.valueOf(text.length());
        } else {
            sCount = String.valueOf(text.length());
        }
        element.writeOneAttributeNS(
                RtfListTable.LIST_TEXT_FORM, "\\'" + sCount + text);
        element.writeGroupMark(false);
            
        element.writeGroupMark(true);
        element.writeOneAttributeNS(RtfListTable.LIST_NUM_POSITION, null);
        element.writeGroupMark(false);
            
        element.attrib.set(RtfListTable.LIST_FONT_TYPE, 2);
    }
}
