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

import java.util.LinkedList;
import java.util.Iterator;
import java.util.LinkedList;
import java.io.Writer;
import java.io.IOException;
//import org.apache.fop.render.rtf.rtflib.jfor.main.JForVersionInfo;

/**
 * RtfListTable: used to make the list table in the header section of the RtfFile.
 * This is the method that Word uses to make lists in RTF and the way most RTF readers,
 * esp. Adobe FrameMaker read lists from RTF.
 * @author Christopher Scott, scottc@westinghouse.com
 */
public class RtfListTable extends RtfContainer {
    private LinkedList lists;
    private LinkedList styles;

//static data members
    /** constant for a list table */
    public static final String LIST_TABLE = "listtable";
    /** constant for a list */
    public static final String LIST = "list";
    /** constant for a list template id */
    public static final String LIST_TEMPLATE_ID = "listtemplateid";
    /** constant for a list level */
    public static final String LIST_LEVEL = "listlevel";
    /** constant for a list number type */
    public static final String LIST_NUMBER_TYPE = "levelnfc";
    /** constant for a list justification */
    public static final String LIST_JUSTIFICATION = "leveljc";
    /** constant for list following character */
    public static final String LIST_FOLLOWING_CHAR = "levelfollow";
    /** constant for list start at */
    public static final String LIST_START_AT = "levelstartat";
    /** constant for list space */
    public static final String LIST_SPACE = "levelspace";
    /** constant for list indentation */
    public static final String LIST_INDENT = "levelindent";
    /** constant for list text format */
    public static final String LIST_TEXT_FORM = "leveltext";
    /** constant for list number positioning */
    public static final String LIST_NUM_POSITION = "levelnumbers";
    /** constant for list name */
    public static final String LIST_NAME = "listname ;";
    /** constant for list ID */
    public static final String LIST_ID = "listid";
    /** constant for list font type */
    public static final String LIST_FONT_TYPE = "f";
    /** constant for list override table */
    public static final String LIST_OVR_TABLE = "listoverridetable";
    /** constant for list override */
    public static final String LIST_OVR = "listoverride";
    /** constant for list override count */
    public static final String LIST_OVR_COUNT = "listoverridecount";
    /** constant for list number */
    public static final String LIST_NUMBER = "ls";

    /** String array of list table attributes */
    public static final String [] LIST_TABLE_ATTR = {
        LIST_TABLE,             LIST,                   LIST_TEMPLATE_ID,
        LIST_NUMBER_TYPE,       LIST_JUSTIFICATION,     LIST_FOLLOWING_CHAR,
        LIST_START_AT,          LIST_SPACE,             LIST_INDENT,
        LIST_TEXT_FORM,         LIST_NUM_POSITION,      LIST_ID,
        LIST_OVR_TABLE,         LIST_OVR,               LIST_OVR_COUNT,
        LIST_NUMBER,            LIST_LEVEL
    };

    /**
     * RtfListTable Constructor: sets the number of the list, and allocates
     * for the RtfAttributes
     * @param parent RtfContainer holding this RtfListTable
     * @param w Writer
     * @param num number of the list in the document
     * @param attrs attributes of new RtfListTable
     * @throws IOException for I/O problems
     */
    public RtfListTable(RtfContainer parent, Writer w, Integer num, RtfAttributes attrs)
    throws IOException {
        super(parent, w, attrs);

        styles=new LinkedList();
    }

    /**
     * Add List
     * @param list RtfList to add
     */
    public int addList(RtfList list) {
        if(lists == null) {
            lists=new LinkedList();
        }

        lists.add(list);
        
        return lists.size();
    }

    /**
     * Write the content
     * @throws IOException for I/O problems
     */
    public void writeRtfContent() throws IOException {
        if(lists!=null) {
            //write '\listtable'
        writeGroupMark(true);
        writeStarControlWordNS(LIST_TABLE);
            for (Iterator it = lists.iterator(); it.hasNext();) {
                final RtfList list = (RtfList)it.next();
                writeListTableEntry(list);
            }
            writeGroupMark(false);
                
            //write '\listoveridetable'
        writeGroupMark(true);
            writeStarControlWordNS(LIST_OVR_TABLE);
            int z=1;
            
            for (Iterator it = styles.iterator(); it.hasNext();) {
                final RtfListStyle style = (RtfListStyle)it.next();
                        
                writeGroupMark(true);
                writeStarControlWordNS(LIST_OVR);
                writeGroupMark(true);
        
                writeOneAttributeNS(LIST_ID, style.getRtfList().getListId().toString());
                writeOneAttributeNS(LIST_OVR_COUNT, new Integer(0));
                writeOneAttributeNS(LIST_NUMBER, new Integer(z++));

                writeGroupMark(false);
                writeGroupMark(false);
            }
            
            writeGroupMark(false);
        }
    }

    /**
     * Since this has no text content we have to overwrite isEmpty to print
     * the table
     * @return false (always)
     */
    public boolean isEmpty() {
        return false;
    }
    
    private void writeListTableEntry(RtfList list)
    throws IOException {
        //write list-specific attributes
        writeGroupMark(true);
        writeControlWordNS(LIST);
        writeOneAttributeNS(LIST_TEMPLATE_ID, list.getListTemplateId().toString());
        writeOneAttributeNS(LIST, attrib.getValue(LIST));
        
        // write level-specific attributes
        writeGroupMark(true);
        writeControlWordNS(LIST_LEVEL);
        
        writeOneAttributeNS(LIST_JUSTIFICATION, attrib.getValue(LIST_JUSTIFICATION));
        writeOneAttributeNS(LIST_FOLLOWING_CHAR, attrib.getValue(LIST_FOLLOWING_CHAR));
        writeOneAttributeNS(LIST_SPACE, new Integer(0));
        writeOneAttributeNS(LIST_INDENT, attrib.getValue(LIST_INDENT));
        
        RtfListItem item=(RtfListItem)list.getChildren().get(0);
        item.getRtfListStyle().writeLevelGroup(this);
        
        writeGroupMark(false);
        
        writeGroupMark(true);
        writeControlWordNS(LIST_NAME);
        writeGroupMark(false);
        
        writeOneAttributeNS(LIST_ID, list.getListId().toString());
                
        writeGroupMark(false);
    }

    /**
     * Change list style
     * @param ls ListStyle to set
     */
    public int addRtfListStyle(RtfListStyle ls) {
        styles.add(ls);
        return styles.size();
    }
}