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
 * The RTF library of the FOP project consists of voluntary contributions made by
 * many individuals on behalf of the Apache Software Foundation and was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and contributors of
 * the jfor project (www.jfor.org), who agreed to donate jfor to the FOP project.
 * For more information on the Apache Software Foundation, please
 * see <http://www.apache.org/>.
 */
package org.apache.fop.rtf.rtflib.rtfdoc;

import java.io.Writer;
import java.io.IOException;

/**  Model of an RTF list, which can contain RTF list items
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 * @author Christopher Scott, scottc@westinghouse.com
 */

public class RtfList extends RtfContainer {
    private RtfListItem m_item;
    private RtfListTable m_listTable;
    private final boolean m_hasTableParent;
    
    /** list numbering style.
     *  Could add more variables, for now we simply differentiate between bullets and numbering
     */
    private NumberingStyle m_numberingStyle;
    public static class NumberingStyle {
        public boolean isBulletedList = true;
    }
    
    /** Create an RTF list as a child of given container with given attributes */
    RtfList(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent,w,attr);
        m_numberingStyle = new NumberingStyle();
        //create a new list table entry for the list
        m_listTable = (getRtfFile()).startListTable(attr);
        m_listTable.setParentList(this);
        
        // find out if we are nested in a table
        m_hasTableParent = this.getParentOfClass(RtfTable.class) != null;
    }
    
    /** change numbering style */
    public void setNumberingStyle(NumberingStyle ns) {
        m_numberingStyle = ns;
    }
    
    /** overridden to setup the list: start a group with appropriate attributes */
    protected void writeRtfPrefix() throws IOException {
        // pard causes word97 (and sometimes 2000 too) to crash if the list is nested in a table
        if(!m_hasTableParent) writeControlWord("pard");
        
        writeOneAttribute(RtfText.LEFT_INDENT_FIRST,m_attrib.getValue(RtfListTable.LIST_INDENT));
        writeOneAttribute(RtfText.LEFT_INDENT_BODY,m_attrib.getValue(RtfText.LEFT_INDENT_BODY));
        
        // put the whole list in a group
        writeGroupMark(true);
        
        // group for list setup info
        writeGroupMark(true);
        
        writeStarControlWord("pn");
        //Modified by Chris Scott
        //fixes second line indentation
        
        if(m_numberingStyle.isBulletedList) {
            // bulleted list
            writeControlWord("pnlvlblt");
            writeControlWord("ilvl0");
            writeOneAttribute(RtfListTable.LIST_NUMBER,
            (m_listTable.getListNumber()).toString());
            writeOneAttribute("pnindent",
            m_attrib.getValue(RtfListTable.LIST_INDENT));
            writeControlWord("pnf1");
            writeGroupMark(true);
            writeControlWord("pndec");
            writeOneAttribute(RtfListTable.LIST_FONT_TYPE,"2");
            writeControlWord("pntxtb");
            writeControlWord("'b7");
            writeGroupMark(false);
        } else {
            // numbered list
            writeControlWord("pnlvlbody");
            writeControlWord("ilvl0");
            writeOneAttribute(RtfListTable.LIST_NUMBER,
            (m_numberingStyle.isBulletedList) ? "2" : "0");
            writeControlWord("pndec");
            writeOneAttribute("pnstart",
            m_attrib.getValue(RtfListTable.LIST_START_AT));
            writeOneAttribute("pnindent",
            m_attrib.getValue(RtfListTable.LIST_INDENT));
            writeControlWord("pntxta.");
        }
        
        writeGroupMark(false);
        writeOneAttribute(RtfListTable.LIST_NUMBER,
        (m_listTable.getListNumber()).toString());
    }
    
    /** end the list group */
    protected void writeRtfSuffix() throws IOException {
        // close group that encloses the whole list
        writeGroupMark(false);
        
        // reset paragraph defaults to make sure list ends
         // but pard causes word97 (and sometimes 2000 too) to crash if the list is nested in a table
        if(!m_hasTableParent) writeControlWord("pard");
    }
    
    /** close current list item and start a new one */
    public RtfListItem newListItem() throws IOException {
        if(m_item != null) m_item.close();
        m_item = new RtfListItem(this,m_writer);
        return m_item;
    }

    /** true if this is a bulleted list (as opposed to numbered list) */
    public boolean isBulletedList() {
        return m_numberingStyle.isBulletedList;
    }
}