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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.Writer;
import java.io.IOException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfTextrun;

/**  Model of an RTF list item, which can contain RTF paragraphs
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */
public class RtfListItem
extends RtfContainer
implements IRtfTextrunContainer,
           IRtfListContainer,
           IRtfParagraphContainer {

    private RtfList parentList;
    private RtfParagraph paragraph;
    private RtfListStyle listStyle;
    private int number=0;

    /** special RtfParagraph that writes list item setup code before its content */
    private class RtfListItemParagraph extends RtfParagraph {

        RtfListItemParagraph(RtfListItem rli, RtfAttributes attrs)
        throws IOException {
            super(rli, rli.writer, attrs);
        }

        protected void writeRtfPrefix() throws IOException {
            super.writeRtfPrefix();
            listStyle.writeParagraphPrefix(this);
        }
            }

    public class RtfListItemLabel extends RtfTextrun implements IRtfTextrunContainer {
        RtfListItem rtfListItem;
        
        public RtfListItemLabel(RtfListItem item) throws IOException {
            super(null, item.writer, null); 
            
            rtfListItem=item;
        }

        public RtfTextrun getTextrun() throws IOException {
            return this;
        }
        
        public void addString(String s) throws IOException {
            
            final String label = s.trim();
            if(label.length() > 0 && Character.isDigit(label.charAt(0))) {
                rtfListItem.setRtfListStyle(new RtfListStyleNumber());
            } else {
                rtfListItem.setRtfListStyle(new RtfListStyleText(label));
            }
        }
    }

    /** Create an RTF list item as a child of given container with default attributes */
    RtfListItem(RtfList parent, Writer w) throws IOException {
        super((RtfContainer)parent, w);
        parentList = parent;
    }

    /**
     * Close current paragraph if any and start a new one
     * @param attrs attributes of new paragraph
     * @return new RtfParagraph
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        if (paragraph != null) {
            paragraph.close();
        }
        paragraph = new RtfListItemParagraph(this, attrs);
        return paragraph;
    }

    /**
     * Close current paragraph if any and start a new one with default attributes
     * @return new RtfParagraph
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }

    /** Create an RTF list item as a child of given container with given attributes */
    RtfListItem(RtfList parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent, w, attr);
        parentList = parent;
    }

    public RtfTextrun getTextrun()
    throws IOException {
        RtfTextrun textrun=RtfTextrun.getTextrun(this, writer, null);
        textrun.setRtfListItem(this);
        return textrun;
    }
    
    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrs attributes of new RftList object
     * @return new RtfList
     * @throws IOException for I/O problems
     */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        RtfList list = new RtfList(this, writer, attrs);
        return list;
    }
    
    /**
     * Overridden to setup the list: start a group with appropriate attributes
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
       
        // pard causes word97 (and sometimes 2000 too) to crash if the list is nested in a table
        if (!parentList.getHasTableParent()) {
            writeControlWord("pard");
        }

        writeOneAttribute(RtfText.LEFT_INDENT_FIRST, attrib.getValue(RtfListTable.LIST_INDENT));
        writeOneAttribute(RtfText.LEFT_INDENT_BODY, attrib.getValue(RtfText.LEFT_INDENT_BODY));

        // group for list setup info
        writeGroupMark(true);

        writeStarControlWord("pn");
        //Modified by Chris Scott
        //fixes second line indentation
        getRtfListStyle().writeListPrefix(this);

        writeGroupMark(false);
        writeOneAttribute(RtfListTable.LIST_NUMBER,new Integer(number));
    }
    
    /**
     * End the list group
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        super.writeRtfSuffix();

        /* reset paragraph defaults to make sure list ends
         * but pard causes word97 (and sometimes 2000 too) to crash if the list
         * is nested in a table
         */ 
        if (!parentList.getHasTableParent()) {
            writeControlWord("pard");
        }
        
    }
       
    /**
     * Change list style
     * @param ls ListStyle to set
     */
    public void setRtfListStyle(RtfListStyle ls) {
        listStyle = ls;
        
        listStyle.setRtfListItem(this);
        number = getRtfFile().getListTable().addRtfListStyle(ls);
    }

    /**
     * Get list style
     * @return ListSytle of the List
     */    
    public RtfListStyle getRtfListStyle() {
        if(listStyle==null) {
            return parentList.getRtfListStyle();
        } else {
            return listStyle;
        }
    }
  
    public RtfList getParentList() {
        return parentList;
    }
    
    public int getNumber() {
        return number;
    }
}
