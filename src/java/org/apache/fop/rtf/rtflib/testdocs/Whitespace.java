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
package org.apache.fop.rtf.rtflib.testdocs;

import java.util.Date;
import java.io.*;
import org.apache.fop.rtf.rtflib.rtfdoc.*;

/**  Generates an RTF document to test the WhitespaceCollapser
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class Whitespace extends TestDocument
{
    /** generate the body of the test document */
    protected void generateDocument(RtfDocumentArea rda,RtfSection sect)
    throws IOException {
        final RtfParagraph p1 = sect.newParagraph();
        p1.newText("\t  Each word  of this paragraph must   be separated\tfrom\t\n\tthe next word with exactly\t \tone");
        p1.newText("   space.");
        
        final RtfParagraph p2 = sect.newParagraph();
        p2.newText("");
        p2.newText("In this");
        p2.newText(" paragraph ");
        p2.newText("as well,");
        p2.newText("   there must\tbe    \t");
        p2.newText("exactly");
        p2.newText(" one space   ");
        p2.newText("between  each\tword and the  next, and no spaces at the beginning or end of the paragraph.");
        
        final RtfParagraph p3 = sect.newParagraph();
        p3.newText("The word 'boomerang' must be written after this with no funny spacing: ");
        p3.newText("boo");
        p3.newText("me");
        p3.newText("r");
        p3.newText("a");
        p3.newText("ng.");
    }
}