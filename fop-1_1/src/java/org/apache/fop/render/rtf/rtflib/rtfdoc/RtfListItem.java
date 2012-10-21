/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Model of an RTF list item, which can contain RTF paragraphs.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch)
 * and Andreas Putz (a.putz@skynamics.com).</p>
 */
public class RtfListItem extends RtfContainer
        implements IRtfTextrunContainer,
                   IRtfListContainer,
                   IRtfParagraphContainer {

    private RtfList parentList;
    private RtfParagraph paragraph;
    private RtfListStyle listStyle;
    private int number = 0;

    /**
     * special RtfParagraph that writes list item setup code before its content
     */
    private class RtfListItemParagraph extends RtfParagraph {

        RtfListItemParagraph(RtfListItem rli, RtfAttributes attrs)
        throws IOException {
            super(rli, rli.writer, attrs);
        }

        protected void writeRtfPrefix() throws IOException {
            super.writeRtfPrefix();
            getRtfListStyle().writeParagraphPrefix(this);
        }
    }

    /**
     * special RtfTextrun that is used as list item label
     */
    public class RtfListItemLabel extends RtfTextrun implements IRtfTextrunContainer {

        private RtfListItem rtfListItem;

        /**
         * Constructs the RtfListItemLabel
         * @param item The RtfListItem the label belongs to
         * @throws IOException Thrown when an IO-problem occurs
         */
        public RtfListItemLabel(RtfListItem item) throws IOException {
            super(null, item.writer, null);

            rtfListItem = item;
        }

        /**
         * Returns the current RtfTextrun object.
         * Opens a new one if necessary.
         * @return The RtfTextrun object
         * @throws IOException Thrown when an IO-problem occurs
         */
        public RtfTextrun getTextrun() throws IOException {
            return this;
        }

        /**
         * Sets the content of the list item label.
         * @param s Content of the list item label.
         * @throws IOException Thrown when an IO-problem occurs
         */
        public void addString(String s) throws IOException {

            final String label = s.trim();
            if (label.length() > 0 && Character.isDigit(label.charAt(0))) {
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
     * @throws IOException Thrown when an IO-problem occurs
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
     * @throws IOException Thrown when an IO-problem occurs
     */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }

    /** Create an RTF list item as a child of given container with given attributes */
    RtfListItem(RtfList parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent, w, attr);
        parentList = parent;
    }


    /**
     * Get the current textrun.
     * @return current RtfTextrun object
     * @throws IOException Thrown when an IO-problem occurs
     */
    public RtfTextrun getTextrun() throws IOException {
        RtfTextrun textrun = RtfTextrun.getTextrun(this, writer, null);
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

        writeOneAttribute(RtfText.LEFT_INDENT_FIRST,
                "360"); //attrib.getValue(RtfListTable.LIST_INDENT));

        writeOneAttribute(RtfText.LEFT_INDENT_BODY,
                attrib.getValue(RtfText.LEFT_INDENT_BODY));

        // group for list setup info
        writeGroupMark(true);

        writeStarControlWord("pn");
        //Modified by Chris Scott
        //fixes second line indentation
        getRtfListStyle().writeListPrefix(this);

        writeGroupMark(false);
        writeOneAttribute(RtfListTable.LIST_NUMBER, new Integer(number));
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
        if (listStyle == null) {
            return parentList.getRtfListStyle();
        } else {
            return listStyle;
        }
    }

    /**
     * Get the parent list.
     * @return the parent list
     */
    public RtfList getParentList() {
        return parentList;
    }

    /**
     * Returns the list number
     * @return list number
     */
    public int getNumber() {
        return number;
    }
}
