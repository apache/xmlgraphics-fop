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
import org.apache.fop.rtf.rtflib.interfaces.ITableColumnsInfo;

/**  Models a section in an RTF document
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfSection
extends RtfContainer
implements
    IRtfParagraphContainer,
    IRtfTableContainer,
    IRtfListContainer,
    IRtfExternalGraphicContainer,
    IRtfBeforeContainer,
    IRtfParagraphKeepTogetherContainer,
    IRtfAfterContainer,
    IRtfJforCmdContainer,
    IRtfTextrunContainer {
    private RtfParagraph paragraph;
    private RtfTable table;
    private RtfList list;
    private RtfExternalGraphic externalGraphic;
    private RtfBefore before;
    private RtfAfter after;
    private RtfJforCmd jforCmd;

    /** Create an RTF container as a child of given container */
    RtfSection(RtfDocumentArea parent, Writer w) throws IOException {
        super(parent, w);
    }

    /**
     * Start a new external graphic after closing current paragraph, list and table
     * @return new RtfExternalGraphic object
     * @throws IOException for I/O problems
     */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        externalGraphic = new RtfExternalGraphic(this, writer);
        return externalGraphic;
    }

    /**
     * Start a new paragraph after closing current paragraph, list and table
     * @param attrs attributes for new RtfParagraph
     * @return new RtfParagraph object
     * @throws IOException for I/O problems
     */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        closeAll();
        paragraph = new RtfParagraph(this, writer, attrs);
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

    /**
     * Close current paragraph if any and start a new one
     * @return new RtfParagraphKeepTogether
     * @throws IOException for I/O problems
     */
    public RtfParagraphKeepTogether newParagraphKeepTogether() throws IOException {
        return new RtfParagraphKeepTogether(this, writer);
    }

    /**
     * Start a new table after closing current paragraph, list and table
     * @param tc Table context used for number-columns-spanned attribute (added by
     * Boris Poudérous on july 2002)
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    public RtfTable newTable(ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, tc);
        return table;
    }

    /**
     * Start a new table after closing current paragraph, list and table
     * @param attrs attributes of new RtfTable
     * @param tc Table context used for number-columns-spanned attribute (added by
     * Boris Poudérous on july 2002)
     * @return new RtfTable object
     * @throws IOException for I/O problems
     */
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, attrs, tc);
        return table;
    }

    /**
     * Start a new list after closing current paragraph, list and table
     * @param attrs attributes of new RftList object
     * @return new RtfList
     * @throws IOException for I/O problems
     */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        closeAll();
        list = new RtfList(this, writer, attrs);
        return list;
    }

    /**
     * IRtfBeforeContainer
     * @param attrs attributes of new RtfBefore object
     * @return new RtfBefore object
     * @throws IOException for I/O problems
     */
    public RtfBefore newBefore(RtfAttributes attrs) throws IOException {
        closeAll();
        before = new RtfBefore(this, writer, attrs);
        return before;
    }

    /**
     * IRtfAfterContainer
     * @param attrs attributes of new RtfAfter object
     * @return new RtfAfter object
     * @throws IOException for I/O problems
     */
    public RtfAfter newAfter(RtfAttributes attrs) throws IOException {
        closeAll();
        after = new RtfAfter(this, writer, attrs);
        return after;
    }

    /**
     *
     * @param attrs attributes of new RtfJforCmd
     * @return the new RtfJforCmd
     * @throws IOException for I/O problems
     */
    public RtfJforCmd newJforCmd(RtfAttributes attrs) throws IOException {
        jforCmd  = new RtfJforCmd(this, writer, attrs);
        return jforCmd;
    }



    /**
     * Can be overridden to write RTF prefix code, what comes before our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfPrefix() throws IOException {
        writeControlWord("sectd");
        writeAttributes(attrib, RtfPage.PAGE_ATTR);
    }

    /**
     * Can be overridden to write RTF suffix code, what comes after our children
     * @throws IOException for I/O problems
     */
    protected void writeRtfSuffix() throws IOException {
        writeControlWord("sect");
    }

    private void closeCurrentTable() throws IOException {
        if (table != null) {
            table.close();
        }
    }

    private void closeCurrentParagraph() throws IOException {
        if (paragraph != null) {
            paragraph.close();
        }
    }

    private void closeCurrentList() throws IOException {
        if (list != null) {
            list.close();
        }
    }

    private void closeCurrentExternalGraphic() throws IOException {
        if (externalGraphic != null) {
            externalGraphic.close();
        }
    }

    private void closeCurrentBefore() throws IOException {
        if (before != null) {
            before.close();
        }
    }

    private void closeAll()
    throws IOException {
        closeCurrentTable();
        closeCurrentParagraph();
        closeCurrentList();
        closeCurrentExternalGraphic();
        closeCurrentBefore();
    }
    
    public RtfTextrun getTextrun()
    throws IOException {
        return RtfTextrun.getTextrun(this, writer, null);
    }
}
