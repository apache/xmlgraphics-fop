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

import java.io.*;
import java.util.Iterator;
import org.apache.fop.rtf.rtflib.interfaces.ITableColumnsInfo;

/**  Container for RtfRow elements
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfTable extends RtfContainer
{
    private RtfTableRow m_row;
    private int highestRow=0;

    /** Added by Boris Poudérous on 07/22/2002 in order to process number-columns-spanned attribute */
    private ITableColumnsInfo m_table_context;

    /** Create an RTF element as a child of given container */
    RtfTable(IRtfTableContainer parent, Writer w, ITableColumnsInfo tc) throws IOException
    {
        super((RtfContainer)parent,w);
        // Line added by Boris Poudérous on 07/22/2002
        m_table_context = tc;
    }

    /** Create an RTF element as a child of given container
   * Modified by Boris Poudérous in order to process 'number-columns-spanned' attribute
   */
  RtfTable(IRtfTableContainer parent, Writer w, RtfAttributes attrs, ITableColumnsInfo tc) throws IOException
    {
        super((RtfContainer)parent,w,attrs);
    // Line added by Boris Poudérous on 07/22/2002
    m_table_context = tc;
    }

    /** close current row if any and start a new one */
    public RtfTableRow newTableRow() throws IOException
    {
        if(m_row != null) m_row.close();

        highestRow++;
        m_row = new RtfTableRow(this,m_writer,m_attrib,highestRow);
        return m_row;
    }
    /** close current row if any and start a new one */
    public RtfTableRow newTableRow(RtfAttributes attrs) throws IOException
    {
        RtfAttributes attr = null;
        if (m_attrib != null)
        {
            attr = (RtfAttributes) m_attrib.clone ();
            attr.set (attrs);
        }
        else
            attr = attrs;
        if(m_row != null) m_row.close();
        highestRow++;

        m_row = new RtfTableRow(this,m_writer,attr,highestRow);
        return m_row;
    }



    /** overridden to write RTF prefix code, what comes before our children */
    protected void writeRtfPrefix() throws IOException
    {
        writeGroupMark(true);
    }

    /** overridden to write RTF suffix code, what comes after our children */
    protected void writeRtfSuffix() throws IOException
    {
        writeGroupMark(false);
    }

    public boolean isHighestRow(int id)
    {
        return (highestRow == id) ? true : false;
    }

    /** Added by Boris Poudérous on 07/22/2002 */
    public ITableColumnsInfo getITableColumnsInfo()
    {
      return this.m_table_context;
    }

    private RtfAttributes m_header_attribs = null;

    // Added by Normand Masse
    // Support for table-header attributes (used instead of table attributes
    public void setHeaderAttribs( RtfAttributes attrs ) {
        m_header_attribs = attrs;
    }

    // Added by Normand Masse
    // Returns the table-header attributes if they are present, otherwise the
    // parent's attributes are returned normally.
    public RtfAttributes getRtfAttributes() {
        if ( m_header_attribs != null ) {
            return m_header_attribs;
        }

        return super.getRtfAttributes();
    }
    /** - end - */
}
