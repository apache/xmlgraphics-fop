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
import java.io.*;

/**  Model of an RTF list item, which can contain RTF paragraphs
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 *  @author Andreas Putz a.putz@skynamics.com
 */
public class RtfListItem
extends RtfContainer
implements IRtfParagraphContainer {
    private RtfList m_parentList;
    private RtfParagraph m_paragraph;
    
    /** special RtfParagraph that writes list item setup code before its content */
    private class RtfListItemParagraph extends RtfParagraph {
        
        RtfListItemParagraph(RtfListItem rli,RtfAttributes attrs)
        throws IOException {
            super(rli,rli.m_writer,attrs);
        }
        
        protected void writeRtfPrefix() throws IOException {
            super.writeRtfPrefix(); 
            // for bulleted list, add list item setup group before paragraph contents
            if(m_parentList.isBulletedList()) {
                writeGroupMark(true);
                writeControlWord("pntext");
				writeControlWord("f" + RtfFontManager.getInstance().getFontNumber("Symbol"));
                writeControlWord("'b7");
                writeControlWord("tab");
                writeGroupMark(false);
            }else{
            	writeGroupMark(true);
        		writeControlWord("pntext");
        		writeGroupMark(false);
        	}
        }
        
    }
    
    /** Create an RTF list item as a child of given container with default attributes */
    RtfListItem(RtfList parent, Writer w) throws IOException {
        super((RtfContainer)parent,w);
        m_parentList = parent;
    }
    
    /** Create an RTF list item as a child of given container with given attributes */
    RtfListItem(RtfList parent, Writer w, RtfAttributes attr) throws IOException {
        super((RtfContainer)parent,w,attr);
        m_parentList = parent;
    }
    
    /** close current paragraph and start a new one */
    /** close current paragraph if any and start a new one */
    public RtfParagraph newParagraph(RtfAttributes attrs) throws IOException {
        if(m_paragraph != null) m_paragraph.close();
        m_paragraph = new RtfListItemParagraph(this,attrs);
        return m_paragraph;
    }
    
    /** close current paragraph if any and start a new one with default attributes */
    public RtfParagraph newParagraph() throws IOException {
        return newParagraph(null);
    }
    
}
