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

/**
 * Creates an hyperlink.
 * This class belongs to the <fo:basic-link> tag processing.
 * @author <a href="mailto:a.putz@skynamics.com">Andreas Putz</a>
 *
 * {\field {\*\fldinst HYPERLINK "http://www.test.de"   }{\fldrslt Joe Smith}}
 */
public class RtfHyperLink extends RtfContainer implements IRtfTextContainer
{

	//////////////////////////////////////////////////
	// @@ Members
	//////////////////////////////////////////////////

	/** The url of the image */
	protected String url = null;

	/** RtfText */
	protected RtfText mText = null;

	//////////////////////////////////////////////////
	// @@ Construction
	//////////////////////////////////////////////////


	/**
	 * Default constructor.
	 *
	 * @param container a <code>RtfContainer</code> value
	 * @param writer a <code>Writer</code> value
	 * @param attributes a <code>RtfAttributes</code> value
	 */
	public RtfHyperLink (IRtfTextContainer parent, Writer writer, String str, RtfAttributes attr)
		throws IOException
	{
		super ((RtfContainer) parent, writer, attr);
		new RtfText (this, writer, str, attr);
	}


	//////////////////////////////////////////////////
	// @@ RtfElement implementation
	//////////////////////////////////////////////////

	/**
	 * Writes the RTF content to m_writer.
	 *
	 * @exception IOException On error
	 */
	public void writeRtfPrefix () throws IOException
	{
		super.writeGroupMark (true);
		super.writeControlWord ("field");

		super.writeGroupMark (true);
		super.writeStarControlWord ("fldinst");

		m_writer.write ("HYPERLINK \"" + url + "\" ");
		super.writeGroupMark (false);

		super.writeGroupMark (true);
		super.writeControlWord ("fldrslt");

		// start a group for this paragraph and write our own attributes if needed
		if (m_attrib != null && m_attrib.isSet ("cs"))
		{
			writeGroupMark (true);
			writeAttributes(m_attrib, new String [] {"cs"});
		}
	}

	/**
	 * Writes the RTF content to m_writer.
	 *
	 * @exception IOException On error
	 */
	public void writeRtfSuffix () throws IOException
	{
		if (m_attrib != null && m_attrib.isSet ("cs"))
		{
			writeGroupMark (false);
		}
		super.writeGroupMark (false);
		super.writeGroupMark (false);
	}


	//////////////////////////////////////////////////
	// @@ IRtfContainer implementation
	//////////////////////////////////////////////////

	/** close current text run if any and start a new one with default attributes
	 *  @param str if not null, added to the RtfText created
	 */
	public RtfText newText (String str) throws IOException {
		return newText (str,null);
	}

	/** close current text run if any and start a new one
	 *  @param str if not null, added to the RtfText created
	 */
	public RtfText newText (String str,RtfAttributes attr) throws IOException
	{
		closeAll ();
		mText = new RtfText (this, m_writer, str, attr);
		return mText;
	}

	/** IRtfTextContainer requirement: return a copy of our attributes */
	public RtfAttributes getTextContainerAttributes()
	{
		if (m_attrib == null) return null;
		return (RtfAttributes) this.m_attrib.clone ();
	}


	/** add a line break */
	public void newLineBreak () throws IOException
	{
		new RtfLineBreak (this, m_writer);
	}


	//////////////////////////////////////////////////
	// @@ Common container methods
	//////////////////////////////////////////////////

	private void closeCurrentText () throws IOException
	{
		if (mText != null) mText.close ();
	}

	private void closeAll () throws IOException
	{
		closeCurrentText();
	}


	//////////////////////////////////////////////////
	// @@ Member access
	//////////////////////////////////////////////////

	/**
	 * Sets the url of the external link.
	 *
	 * @param url Link url like "http://..."
	 */
	public void setExternalURL (String url)
	{
		this.url = url;
	}

	/**
	 * Sets the url of the external link.
	 *
	 * @param jumpTo Name of the text mark
	 */
	public void setInternalURL (String jumpTo)
	{
		int now = jumpTo.length ();
		int max = RtfBookmark.maxBookmarkLength;
		this.url = "#" + jumpTo.substring (0, now > max ? max : now);
		this.url = this.url.replace ('.', RtfBookmark.replaceCharacter);
		this.url = this.url.replace (' ', RtfBookmark.replaceCharacter);
	}

	public boolean isEmpty ()
	{
		return false;
	}
}
