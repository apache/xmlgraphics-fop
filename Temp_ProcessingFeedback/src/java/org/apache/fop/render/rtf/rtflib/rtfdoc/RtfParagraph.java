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

import java.io.Writer;
import java.io.IOException;
import java.util.List;

/**  Model of an RTF paragraph, which can contain RTF text elements.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 *  @author Boris Poudérous, boris.pouderous@free.fr
 */

public class RtfParagraph extends RtfBookmarkContainerImpl
implements IRtfTextContainer, IRtfPageBreakContainer, IRtfHyperLinkContainer,
        IRtfExternalGraphicContainer, IRtfPageNumberContainer,
        IRtfPageNumberCitationContainer {
    private RtfText text;
    private RtfHyperLink hyperlink;
    private RtfExternalGraphic externalGraphic;
    private RtfPageNumber pageNumber;
    private RtfPageNumberCitation pageNumberCitation;
    // Above line added by Boris POUDEROUS on 2002/07/09
    private boolean keepn = false;
    private boolean resetProperties = false;

    /* needed for importing Rtf into FrameMaker
       FrameMaker is not as forgiving as word in rtf
           thus /pard/par must be written in a page break directly
           after a table.  /pard is probably needed in other places
           also, this is just a hack to make FrameMaker import Jfor rtf
           correctly */
    private boolean writeForBreak = false;

    /** Set of attributes that must be copied at the start of a paragraph */
    private static final String[] PARA_ATTRIBUTES = {"intbl"};

    /** Create an RTF paragraph as a child of given container with default attributes */
    RtfParagraph(IRtfParagraphContainer parent, Writer w) throws IOException {
        super((RtfContainer)parent, w);
    }

    /** Create an RTF paragraph as a child of given container with given attributes */
    RtfParagraph(IRtfParagraphContainer parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent, w, attr);
    }

    /**
     * Accessor for the paragraph text
     * @return the paragraph text
     */
    public String getText() {
        return (text.getText());
    }

    /** Set the keepn attribute for this paragraph */
    public void setKeepn() {
        this.keepn = true;
    }

    /** Force reset properties */
    public void setResetProperties() {
        this.resetProperties = true;
    }

    /**
     * IRtfTextContainer requirement: return a copy of our attributes
     * @return a copy of this paragraphs attributes
     */
    public RtfAttributes getTextContainerAttributes() {
        if (attrib == null) {
            return null;
        }
        return (RtfAttributes)this.attrib.clone();
    }

    /**
     * Overridden to write our attributes before our content
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {

        //Reset paragraph properties if needed
           if (resetProperties) {
               writeControlWord("pard");
           }

        /*
         * Original comment said "do not write text attributes here, they are
         * handled by RtfText." However, the text attributes appear to be
         * relevant to paragraphs as well.
         */
        writeAttributes(attrib, RtfText.ATTR_NAMES);
        writeAttributes(attrib, PARA_ATTRIBUTES);
        // Added by Normand Masse
        // Write alignment attributes after \intbl for cells
        if (attrib.isSet("intbl") && mustWriteAttributes()) {
            writeAttributes(attrib, RtfText.ALIGNMENT);
        }

        //Set keepn if needed (Keep paragraph with the next paragraph)
        if (keepn) {
            writeControlWord("keepn");
        }

        // start a group for this paragraph and write our own attributes if needed
        if (mustWriteGroupMark()) {
            writeGroupMark(true);
        }


        if (mustWriteAttributes()) {
            // writeAttributes(m_attrib, new String [] {"cs"});
            // Added by Normand Masse
            // If \intbl then attributes have already been written (see higher in method)
            if (!attrib.isSet("intbl")) {
                writeAttributes(attrib, RtfText.ALIGNMENT);
            }
            //this line added by Chris Scott, Westinghouse
            writeAttributes(attrib, RtfText.BORDER);
            writeAttributes(attrib, RtfText.INDENT);
            writeAttributes(attrib, RtfText.TABS);
            if (writeForBreak) {
                writeControlWord("pard\\par");
            }
        }

    }

    /**
     * Overridden to close paragraph
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        // sometimes the end of paragraph mark must be suppressed in table cells
        boolean writeMark = true;
        if (parent instanceof RtfTableCell) {
            writeMark = ((RtfTableCell)parent).paragraphNeedsPar(this);
        }
        if (writeMark) {
            writeControlWord("par");
        }

        if (mustWriteGroupMark()) {
            writeGroupMark(false);
        }


    }

    /**
     * Close current text run if any and start a new one with default attributes
     * @param str if not null, added to the RtfText created
     * @return the new RtfText object
     * @throws IOException for I/O problems
     */
    public RtfText newText(String str) throws IOException {
        return newText(str, null);
    }

    /**
     * Close current text run if any and start a new one
     * @param str if not null, added to the RtfText created
     * @param attr attributes of the text
     * @return the new RtfText object
     * @throws IOException for I/O problems
     */
    public RtfText newText(String str, RtfAttributes attr) throws IOException {
        closeAll();
        text = new RtfText(this, writer, str, attr);
        return text;
    }

    /**
     * add a page break
     * @throws IOException for I/O problems
     */
    public void newPageBreak() throws IOException {
        writeForBreak = true;
        new RtfPageBreak(this, writer);
    }

    /**
     * add a line break
     * @throws IOException for I/O problems
     */
    public void newLineBreak() throws IOException {
        new RtfLineBreak(this, writer);
    }

    /**
     * Add a page number
     * @return new RtfPageNumber object
     * @throws IOException for I/O problems
     */
    public RtfPageNumber newPageNumber()throws IOException {
        pageNumber = new RtfPageNumber(this, writer);
        return pageNumber;
    }

    /**
     * Added by Boris POUDEROUS on 2002/07/09
     * @param id string containing the citation text
     * @return the new RtfPageNumberCitation object
     * @throws IOException for I/O problems
     */
    public RtfPageNumberCitation newPageNumberCitation(String id) throws IOException {
       pageNumberCitation = new RtfPageNumberCitation(this, writer, id);
       return pageNumberCitation;
    }

    /**
     * Creates a new hyperlink.
     * @param str string containing the hyperlink text
     * @param attr attributes of new hyperlink
     * @return the new RtfHyperLink object
     * @throws IOException for I/O problems
     */
    public RtfHyperLink newHyperLink(String str, RtfAttributes attr) throws IOException {
        hyperlink = new RtfHyperLink(this, writer, str, attr);
        return hyperlink;
    }

    /**
     * Start a new external graphic after closing all other elements
     * @return the new RtfExternalGraphic
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        externalGraphic = new RtfExternalGraphic(this, writer);
        return externalGraphic;
    }

    private void closeCurrentText() throws IOException {
        if (text != null) {
            text.close();
        }
    }

    private void closeCurrentHyperLink() throws IOException {
        if (hyperlink != null) {
            hyperlink.close();
        }
    }

    private void closeAll() throws IOException {
        closeCurrentText();
        closeCurrentHyperLink();
    }

    /**
     * Depending on RtfOptions, do not emit any RTF for empty paragraphs
     * @return true if RTF should be written
     */
    protected boolean okToWriteRtf() {
        boolean result = super.okToWriteRtf();

        if (parent.getOptions().ignoreEmptyParagraphs() && getChildCount() == 0) {
            // TODO should test that this is the last RtfParagraph in the cell instead
            // of simply testing for last child??
            result = false;
        }

        return result;
    }

    /** true if we must write our own (non-text) attributes in the RTF */
    private boolean mustWriteAttributes() {
        boolean writeAttributes = false;
        final int children = getChildCount();
        if (children > 0) {
            final List childList = getChildren();
            for (int i = 0; i < children; i++) {
                final RtfElement el = (RtfElement) childList.get(i);
                if (!el.isEmpty()) {
                    if (el.getClass() == RtfText.class) {
                        boolean tmp = ((RtfText) el).isNbsp();
                        if (!tmp) {
                            writeAttributes = true;
                            break;
                        }
                    } else {
                        writeAttributes = true;
                        break;
                    }
                }
            }
        }
        return writeAttributes;
    }

    /** true if we must write a group mark around this paragraph
     *  TODO is this correct, study interaction with mustWriteAttributes()
     *       <-- On implementation i have noticed if the groupmark set, the
     *       format attributes are only for this content, i think this
     *       implementation is ok
     */
    private boolean mustWriteGroupMark() {
        return getChildCount() > 0;
    }

    /**
     * accessor for text attributes
     * @return attributes of the text
     */
    public RtfAttributes getTextAttributes() {
        if (text == null) {
            return null;
        }
        return text.getTextAttributes();
    }
}
