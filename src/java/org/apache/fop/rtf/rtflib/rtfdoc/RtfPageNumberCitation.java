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

/*
 * @author Christopher Scott, scottc@westinghouse.com
 * @author Boris Pouderous, boris.pouderous@free.fr
 */

public class RtfPageNumberCitation extends RtfContainer
{
  // Page field :
	//  "{\field {\*\fldinst {PAGEREF xx}} {\fldrslt}}" where xx represents the
  // 'id' of the referenced page
	public static final String RTF_FIELD = "field";
	public static final String RTF_FIELD_PAGEREF_MODEL = "fldinst { PAGEREF }";
	public static final String RTF_FIELD_RESULT = "fldrslt";

  // The 'id' of the referenced page
  private String id = null;

  /** Create an RTF page number citation as a child of given container with default attributes */
  RtfPageNumberCitation (IRtfPageNumberCitationContainer parent, Writer w, String id) throws IOException
  {
     super((RtfContainer)parent,w);
     this.id = id;
  }

  /** Create an RTF page number citation as a child of given
   *    paragraph, copying its attributes */
  RtfPageNumberCitation (RtfParagraph parent, Writer w, String id)
    throws IOException
  {
      // add the attributes ant text attributes of the parent paragraph
      super((RtfContainer)parent,w, parent.m_attrib);
      if (parent.getTextAttributes() != null) {
          m_attrib.set(parent.getTextAttributes());
      }
      this.id = id;
  }

    protected void writeRtfContent() throws IOException
    {
        // If we have a valid ID
        if (isValid()) {
            // Build page reference field
            String pageRef = RTF_FIELD_PAGEREF_MODEL;
            final int insertionIndex = pageRef.indexOf("}");
            pageRef =
                pageRef.substring(0,insertionIndex) + "\"" + id + "\"" + " "
                + pageRef.substring(insertionIndex, pageRef.length())
            ;
            id = null;

            // Write RTF content
            writeGroupMark(true);
            writeControlWord(RTF_FIELD);
            writeGroupMark(true);
            writeAttributes(m_attrib,RtfText.ATTR_NAMES); // Added by Boris Poudérous
            writeStarControlWord(pageRef);
            writeGroupMark(false);
            writeGroupMark(true);
            writeControlWord(RTF_FIELD_RESULT);
            writeGroupMark(false);
            writeGroupMark(false);
        }
    }

  /** checks that the 'ref-id' attribute exists */
  private boolean isValid()
  {
    if (id != null)
      return true;
    else
      return false;
  }

  /** true if this element would generate no "useful" RTF content */
  public boolean isEmpty()
  {
      return false;
  }
}
