package org.apache.fop.rtf.rtflib.tools;

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/*-----------------------------------------------------------------------------
 * jfor - Open-Source XSL-FO to RTF converter - see www.jfor.org
 *
 * ====================================================================
 * jfor Apache-Style Software License.
 * Copyright (c) 2002 by the jfor project. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed
 * by the jfor project (http://www.jfor.org)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The name "jfor" must not be used to endorse
 * or promote products derived from this software without prior written
 * permission.  For written permission, please contact info@jfor.org.
 *
 * 5. Products derived from this software may not be called "jfor",
 * nor may "jfor" appear in their name, without prior written
 * permission of info@jfor.org.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE JFOR PROJECT OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * Contributor(s):
-----------------------------------------------------------------------------*/

/**  Recursive visit of a DOM Element.
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

//------------------------------------------------------------------------------
// $Id$
// $Log$
// Revision 1.1  2003/06/25 09:01:17  bdelacretaz
// additional jfor packages donated to the FOP project
//
// Revision 1.3  2002/07/12 08:08:31  bdelacretaz
// License changed to jfor Apache-style license
//
// Revision 1.2  2001/08/31 07:51:01  bdelacretaz
// MPL license text added + javadoc class comments corrected
//
// Revision 1.1  2001/08/29 13:27:51  bdelacretaz
// V0.4.1 - base package name changed to org.apache.fop.rtf.rtflib.jfor
//
// Revision 1.1.1.1  2001/08/02 12:53:48  bdelacretaz
// initial SourceForge checkin of V0.1 code
//
//------------------------------------------------------------------------------

public abstract class ElementVisitor
{
    /** recursively visit element e */
    public final void visit(Element e)
        throws IOException,SAXException
    {
        if(e!=null) {
            startElement(e);

            final NodeList list = e.getChildNodes();
            for(int i=0; i < list.getLength(); i++) {
                Node n = list.item(i);
                if(n instanceof Element) {
                    visit((Element)n);
                } else if(n instanceof CDATASection) {
                    visitCDATA((CDATASection)n);
                } else if(n instanceof Text) {
                    visitText((Text)n);
                } else if(n instanceof Comment) {
                    visitComment((Comment)n);
                } else {
                    visitNode(n);
                }
            }

            endElement(e);
        }
    }

    /** called at the start of the visit of an Element */
    protected abstract void startElement(Element e) throws IOException,SAXException;

    /** called at the end of the visit of an Element */
    protected abstract void endElement(Element e) throws IOException,SAXException;

    /** called to visit a Text node */
    protected abstract void visitText(Text t) throws IOException,SAXException;

    /** called to visit a CDATASection node */
    protected abstract void visitCDATA(CDATASection cds) throws IOException,SAXException;

    /** called to visit a Comment node */
    protected abstract void visitComment(Comment c) throws IOException,SAXException;

    /** called to visit a Node that is not of the other types */
    protected abstract void visitNode(Node n) throws IOException,SAXException;
}