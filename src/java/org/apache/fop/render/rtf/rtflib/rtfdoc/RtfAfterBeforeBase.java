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
import org.apache.fop.render.rtf.rtflib.interfaces.ITableColumnsInfo;

/** Common code for RtfAfter and RtfBefore
*  @author Andreas Lambert <andreas.lambert@cronidesoft.com>
*  @author Christopher Scott, scottc@westinghouse.com
*  @author Christoph Zahm <zahm@jnet.ch> (support for tables in headers/footers)
*/

abstract class RtfAfterBeforeBase
extends RtfContainer
implements IRtfParagraphContainer, IRtfExternalGraphicContainer, IRtfTableContainer,
        IRtfTextrunContainer {
    protected RtfAttributes attrib;
    private RtfParagraph para;
    private RtfExternalGraphic externalGraphic;
    private RtfTable table;

    RtfAfterBeforeBase(RtfSection parent, Writer w, RtfAttributes attrs) throws IOException {
        super((RtfContainer)parent, w, attrs);
        attrib = attrs;
    }

    public RtfParagraph newParagraph() throws IOException {
        closeAll();
        para = new RtfParagraph(this, writer);
        return para;
    }

    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        closeAll();
        para = new RtfParagraph(this, writer, attrs);
        return para;
    }

    public RtfExternalGraphic newImage() throws IOException {
        closeAll();
        externalGraphic = new RtfExternalGraphic(this, writer);
        return externalGraphic;
    }

    private void closeCurrentParagraph() throws IOException {
        if (para != null) {
            para.close();
        }
    }

    private void closeCurrentExternalGraphic() throws IOException {
        if (externalGraphic != null) {
            externalGraphic.close();
        }
    }

    private void closeCurrentTable() throws IOException {
        if (table != null) {
            table.close();
        }
    }

    protected void writeRtfPrefix() throws IOException {
        writeGroupMark(true);
        writeMyAttributes();
    }

    /** must be implemented to write the header or footer attributes */
    protected abstract void writeMyAttributes() throws IOException;

    protected void writeRtfSuffix() throws IOException {
        writeGroupMark(false);
    }

    public RtfAttributes getAttributes() {
        return attrib;
    }

    public void closeAll() throws IOException {
        closeCurrentParagraph();
        closeCurrentExternalGraphic();
        closeCurrentTable();
    }

    /** close current table if any and start a new one
     * @param tc added by Boris Poudérous on july 2002 in order to process
     *  number-columns-spanned attribute
     */
    public RtfTable newTable(RtfAttributes attrs, ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, attrs, tc);
        return table;
    }

    /** close current table if any and start a new one  */
    public RtfTable newTable(ITableColumnsInfo tc) throws IOException {
        closeAll();
        table = new RtfTable(this, writer, tc);
        return table;
    }
    
    public RtfTextrun getTextrun()
    throws IOException {
        return RtfTextrun.getTextrun(this, writer, null);
    }
}
