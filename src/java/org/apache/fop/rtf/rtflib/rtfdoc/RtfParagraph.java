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

package org.apache.fop.rtf.rtflib.rtfdoc;

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
    private RtfText m_text;
    private RtfHyperLink m_hyperlink;
    private RtfExternalGraphic m_externalGraphic;
    private RtfPageNumber m_pageNumber;
    private RtfPageNumberCitation m_pageNumberCitation;
    // Above line added by Boris POUDEROUS on 2002/07/09
    private boolean m_keepn = false;
    private boolean m_resetProperties = false;

    /* needed for importing Rtf into FrameMaker
       FrameMaker is not as forgiving as word in rtf
           thus /pard/par must be written in a page break directly
           after a table.  /pard is probably needed in other places
           also, this is just a hack to make FrameMaker import Jfor rtf
           correctly */
    private boolean writeForBreak = false;

    /** Set of attributes that must be copied at the start of a paragraph */
    private static final String [] PARA_ATTRIBUTES = { "intbl" };

    /** Create an RTF paragraph as a child of given container with default attributes */
    RtfParagraph(IRtfParagraphContainer parent, Writer w) throws IOException {
        super((RtfContainer)parent, w);
    }

    /** Create an RTF paragraph as a child of given container with given attributes */
    RtfParagraph(IRtfParagraphContainer parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent, w, attr);
    }

    public String getText() {
        return (m_text.getText());
    }

    /** Set the keepn attribute for this paragraph */
    public void setKeepn() {
        this.m_keepn = true;
    }

    /** Force reset properties */
    public void setResetProperties() {
        this.m_resetProperties = true;
    }

    /** IRtfTextContainer requirement: return a copy of our attributes */
    public RtfAttributes getTextContainerAttributes() {
        if (m_attrib == null) {
            return null;
        }
        return (RtfAttributes)this.m_attrib.clone();
    }

    /** overridden to write our attributes before our content */
    protected void writeRtfPrefix() throws IOException {
        // collapse whitespace before writing out
        // TODO could be made configurable
        if (m_attrib != null && m_attrib.isSet("WhiteSpaceFalse")) {
            m_attrib.unset("WhiteSpaceFalse");
        } else {
            new WhitespaceCollapser(this);
        }

        //Reset paragraph properties if needed
           if (m_resetProperties) {
               writeControlWord("pard");
           }

        // do not write text attributes here, they are handled
        // by RtfText
        writeAttributes(m_attrib, PARA_ATTRIBUTES);
        // Added by Normand Masse
        // Write alignment attributes after \intbl for cells
        if (m_attrib.isSet("intbl") && mustWriteAttributes()) {
            writeAttributes(m_attrib, RtfText.ALIGNMENT);
        }

        //Set keepn if needed (Keep paragraph with the next paragraph)
        if (m_keepn) {
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
            if (!m_attrib.isSet("intbl")) {
                writeAttributes(m_attrib, RtfText.ALIGNMENT);
            }
            //this line added by Chris Scott, Westinghouse
            writeAttributes(m_attrib, RtfText.BORDER);
            writeAttributes(m_attrib, RtfText.INDENT);
            writeAttributes(m_attrib, RtfText.TABS);
            if (writeForBreak) {
                writeControlWord("pard\\par");
            }
        }

    }

    /** overridden to close paragraph */
    protected void writeRtfSuffix() throws IOException {
        // sometimes the end of paragraph mark must be suppressed in table cells
        boolean writeMark = true;
        if (m_parent instanceof RtfTableCell) {
            writeMark = ((RtfTableCell)m_parent).paragraphNeedsPar(this);
        }
        if (writeMark) {
            writeControlWord("par");
        }

        if (mustWriteGroupMark()) {
            writeGroupMark(false);
        }


    }

    /** close current text run if any and start a new one with default attributes
     *  @param str if not null, added to the RtfText created
     */
    public RtfText newText(String str) throws IOException {
        return newText(str, null);
    }

    /** close current text run if any and start a new one
     *  @param str if not null, added to the RtfText created
     */
    public RtfText newText(String str, RtfAttributes attr) throws IOException {
        closeAll();
        m_text = new RtfText(this, m_writer, str, attr);
        return m_text;
    }

    /** add a page break */
    public void newPageBreak() throws IOException {
        writeForBreak = true;
        new RtfPageBreak(this, m_writer);
    }

    /** add a line break */
    public void newLineBreak() throws IOException {
        new RtfLineBreak(this, m_writer);
    }

    public RtfPageNumber newPageNumber()throws IOException {
        m_pageNumber = new RtfPageNumber(this, m_writer);
        return m_pageNumber;
    }

    /**
     * Added by Boris POUDEROUS on 2002/07/09
     */
    public RtfPageNumberCitation newPageNumberCitation(String id) throws IOException {
       m_pageNumberCitation = new RtfPageNumberCitation(this, m_writer, id);
       return m_pageNumberCitation;
    }

    /** Creates a new hyperlink. */
    public RtfHyperLink newHyperLink(String str, RtfAttributes attr) throws IOException {
        m_hyperlink = new RtfHyperLink(this, m_writer, str, attr);
        return m_hyperlink;
    }

    /** start a new external graphic after closing all other elements */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        m_externalGraphic = new RtfExternalGraphic(this, m_writer);
        return m_externalGraphic;
    }

    private void closeCurrentText() throws IOException {
        if (m_text != null) {
            m_text.close();
        }
    }

    private void closeCurrentHyperLink() throws IOException {
        if (m_hyperlink != null) {
            m_hyperlink.close();
        }
    }

    private void closeAll() throws IOException {
        closeCurrentText();
        closeCurrentHyperLink();
    }

    /** depending on RtfOptions, do not emit any RTF for empty paragraphs */
    protected boolean okToWriteRtf() {
        boolean result = super.okToWriteRtf();

        if (m_parent.getOptions().ignoreEmptyParagraphs() && getChildCount() == 0) {
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

    /** get the attributes of our text */
    public RtfAttributes getTextAttributes() {
        if (m_text == null) {
            return null;
        }
        return m_text.getTextAttributes();
    }
}
