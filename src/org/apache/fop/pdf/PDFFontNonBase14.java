/*-- $Id$ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */
package org.apache.fop.pdf;

// Java

/**
 * A common ancestor for Type1, TrueType, MMType1 and Type3 fonts
 * (all except base 14 fonts).
 */
public abstract class PDFFontNonBase14 extends PDFFont {

	/** first character code in the font */
	protected int firstChar;
	/** last character code in the font */
	protected int lastChar;
	/** widths of characters from firstChar to lastChar */
	protected PDFArray widths;
	/** descriptor of font metrics */
	protected PDFFontDescriptor descriptor;

	/**
	 * create the /Font object
	 *
	 * @param number the object's number
	 * @param fontname the internal name for the font
	 * @param subtype the font's subtype
	 * @param basefont the base font name
	 * @param encoding the character encoding schema used by the font
	 * @param mapping the Unicode mapping mechanism
	 */
	public PDFFontNonBase14(int number, String fontname, byte subtype,
			String basefont, Object encoding/*, PDFToUnicode mapping*/) {

		/* generic creation of PDF object */
		super(number, fontname, subtype, basefont, encoding);

		this.descriptor = null;
	}

	/**
	 * set the width metrics for the font
	 *
	 * @param firstChar the first character code in the font
	 * @param lastChar the last character code in the font
	 * @param widths an array of size (lastChar - firstChar +1)
	 */
	public void setWidthMetrics(int firstChar, int lastChar, PDFArray widths) {
		/* set fields using paramaters */
		this.firstChar = firstChar;
		this.lastChar = lastChar;
		this.widths = widths;
	}

	/**
	 * set the font descriptor (unused for the Type3 fonts)
	 *
	 * @param descriptor the descriptor for other font's metrics
	 */
	public void setDescriptor(PDFFontDescriptor descriptor) {
		this.descriptor = descriptor;
	}

	/**
	 * fill in the specifics for the font's subtype
	 */
	protected void fillInPDF(StringBuffer p) {
		p.append("\n/FirstChar "); p.append(firstChar);
		p.append("\n/LastChar "); p.append(lastChar);
		p.append("\n/Widths "); p.append(this.widths.referencePDF());
		if (descriptor != null) {
			p.append("\n/FontDescriptor ");
			p.append(this.descriptor.referencePDF());
		}
	}
}
