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
import java.io.*;
import java.util.*;
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
    IRtfJforCmdContainer
{
    private RtfParagraph m_paragraph;
    private RtfTable m_table;
    private RtfList m_list;
    private RtfExternalGraphic m_externalGraphic;
    private RtfBefore m_before;
    private RtfAfter m_after;
    private RtfJforCmd m_jforCmd;

    /** Create an RTF container as a child of given container */
    RtfSection(RtfDocumentArea parent, Writer w) throws IOException {
        super(parent,w);
    }

    /** start a new external graphic after closing current paragraph, list and table */
    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        m_externalGraphic = new RtfExternalGraphic(this,m_writer);
        return m_externalGraphic;
    }

    /** start a new paragraph after closing current paragraph, list and table */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        closeAll();
        m_paragraph = new RtfParagraph(this,m_writer,attrs);
        return m_paragraph;
    }

    /** close current paragraph if any and start a new one with default attributes */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }

    /** close current paragraph if any and start a new one */
    public RtfParagraphKeepTogether newParagraphKeepTogether() throws IOException {
        return new RtfParagraphKeepTogether(this,m_writer);
    }

    /** start a new table after closing current paragraph, list and table
   * @param tc Table context used for number-columns-spanned attribute (added by Boris Poudérous on july 2002)
   */
    public RtfTable newTable(ITableColumnsInfo tc) throws IOException {
        closeAll();
        m_table = new RtfTable(this,m_writer,tc);
        return m_table;
    }

    /** start a new table after closing current paragraph, list and table
   * @param tc Table context used for number-columns-spanned attribute (added by Boris Poudérous on july 2002)
   */
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException
    {
        closeAll();
        m_table = new RtfTable(this,m_writer, attrs, tc);
        return m_table;
    }

    /** start a new list after closing current paragraph, list and table */
    public RtfList newList(RtfAttributes attrs) throws IOException {
        closeAll();
        m_list = new RtfList(this,m_writer, attrs);
        return m_list;
    }

    /** IRtfBeforeContainer */
    public RtfBefore newBefore(RtfAttributes attrs) throws IOException {
        closeAll();
        m_before = new RtfBefore(this,m_writer,attrs);
        return m_before;
    }

    /** IRtfAfterContainer */
    public RtfAfter newAfter(RtfAttributes attrs) throws IOException {
        closeAll();
        m_after = new RtfAfter(this,m_writer,attrs);
        return m_after;
    }


    public RtfJforCmd newJforCmd(RtfAttributes attrs) throws IOException {
        m_jforCmd  = new RtfJforCmd(this,m_writer,attrs);
        return m_jforCmd;
    }



    /** can be overridden to write RTF prefix code, what comes before our children */
    protected void writeRtfPrefix() throws IOException {
        writeControlWord("sectd");
    }

    /** can be overridden to write RTF suffix code, what comes after our children */
    protected void writeRtfSuffix() throws IOException {
        writeControlWord("sect");
    }

    private void closeCurrentTable() throws IOException {
        if(m_table != null) m_table.close();
    }

    private void closeCurrentParagraph() throws IOException {
        if(m_paragraph!=null) m_paragraph.close();
    }

    private void closeCurrentList() throws IOException {
        if(m_list!=null) m_list.close();
    }

    private void closeCurrentExternalGraphic() throws IOException {
        if(m_externalGraphic!=null) m_externalGraphic.close();
    }

    private void closeCurrentBefore() throws IOException {
        if(m_before!=null) m_before.close();
    }

    private void closeAll()
    throws IOException {
        closeCurrentTable();
        closeCurrentParagraph();
        closeCurrentList();
        closeCurrentExternalGraphic();
        closeCurrentBefore();
    }
}