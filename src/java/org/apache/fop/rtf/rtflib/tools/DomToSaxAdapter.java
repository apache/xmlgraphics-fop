package org.apache.fop.rtf.rtflib.tools;

import java.io.IOException;
import org.w3c.dom.*;
import org.xml.sax.*;
import org.xml.sax.helpers.AttributesImpl;

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
 *  Thanks to John Cowan (cowan@ccil.org) for his DomParser class on which parts 
 *  of this are based. <br>
-----------------------------------------------------------------------------*/

/**  Walks a DOM document and fires (some) SAX events to simulate parsing
 *  of the original document. <br>
 *  Written to use jfor with Cocoon 1.8. <br>
 *  Only SAX events that are used by the jfor Converter class are implemented. <br>
 *  Namespace URIs and local names are not implemented. <br>
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

//------------------------------------------------------------------------------
// $Id$
// $Log$
// Revision 1.1  2003/06/25 09:01:17  bdelacretaz
// additional jfor packages donated to the FOP project
//
// Revision 1.4  2002/07/12 08:08:31  bdelacretaz
// License changed to jfor Apache-style license
//
// Revision 1.3  2001/08/31 07:51:01  bdelacretaz
// MPL license text added + javadoc class comments corrected
//
// Revision 1.2  2001/08/30 10:21:50  bdelacretaz
// Converter modified to be usable as a pure SAX ContentHandler for Cocoon 2 integration
// (thanks to Gianugo Rabellino, gianugo@rabellino.it)
//
// Revision 1.1  2001/08/29 13:27:51  bdelacretaz
// V0.4.1 - base package name changed to org.apache.fop.rtf.rtflib.jfor
//
// Revision 1.1.1.1  2001/08/02 12:53:48  bdelacretaz
// initial SourceForge checkin of V0.1 code
//
//------------------------------------------------------------------------------

public class DomToSaxAdapter
extends ElementVisitor
{
    private ContentHandler m_handler;
    private final Document m_doc;
    
    /** create an adapter for the given DOM Document */
    public DomToSaxAdapter(Document doc)
    {
        m_doc = doc;
    }
    
    /** walk the document and send SAX events to the given ContentHandler */
    public void simulateParsing(ContentHandler ch) throws IOException,SAXException
    {
        m_handler = ch;
        this.visit(m_doc.getDocumentElement());
        m_handler.endDocument();
    }
    
    /** called at the start of the visit of an Element */
    protected void startElement(Element e) throws IOException,SAXException
    {
        m_handler.startElement(null,null,e.getTagName(),saxAttributes(e.getAttributes()));
    }

    /** called at the end of the visit of an Element */
    protected void endElement(Element e) throws IOException,SAXException
    {
        m_handler.endElement(null,null,e.getTagName());
    }

    /** called to visit a Text node */
    protected void visitText(Text t) throws IOException,SAXException
    {
        final String data = t.getNodeValue();
        final int datalen = data.length();
        final char [] array = new char[datalen];
        data.getChars(0, datalen, array, 0);
        m_handler.characters(array, 0, datalen);
    }

    /** called to visit a CDATASection node */
    protected void visitCDATA(CDATASection cds) throws IOException
    {
        // not needed by jfor
    }

    /** called to visit a Comment node */
    protected void visitComment(Comment c) throws IOException
    {
        // not needed by jfor
    }

    /** called to visit a Node that is not of the other types */
    protected void visitNode(Node n) throws IOException
    {
        // not needed by jfor
    }
    
    /** convert DOM attributes to SAX */
    private static Attributes saxAttributes(NamedNodeMap domAttr)
    {
        final String ATTR_TYPE = "CDATA";
        
        final AttributesImpl result = new AttributesImpl();
        for(int i=0; i < domAttr.getLength(); i++) {
            final Attr a = (Attr)domAttr.item(i);
            // namespace information is not provided, not needed by jfor
            result.addAttribute(null,null,a.getName(),ATTR_TYPE,a.getValue());
        }
        return result;
    }
}