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

 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
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

package org.apache.fop.fo;

// FOP
import org.apache.fop.layout.Area;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.apps.FOPException;

/**
 * a text node in the formatting object tree
 */
public class FOText extends FONode {

		protected char[] ca;
		protected int start;
		protected int length;

		FontState fs;
		float red;
		float green;
		float blue;
		int wrapOption;
		int whiteSpaceCollapse;
		int verticalAlign;

		// Textdecoration
		protected boolean underlined = false;
		protected boolean overlined = false;
		protected boolean lineThrough = false;

                TextState ts;


		public FOText(char[] chars, int s, int e, FObj parent) {
				super(parent);
				this.start = 0;
				this.ca = new char[e - s];
				for (int i = s; i < e; i++)
						this.ca[i - s] = chars[i];
				this.length = e - s;
		}

		public void setUnderlined(boolean ul) {
				this.underlined = ul;
		}

    public void setOverlined(boolean ol) {
        this.overlined = ol;
    }

    public void setLineThrough(boolean lt) {
        this.lineThrough = lt;
    }


		public boolean willCreateArea()
		{
				this.whiteSpaceCollapse = this.parent.properties.get(
																		"white-space-collapse").getEnum();
				if(this.whiteSpaceCollapse == WhiteSpaceCollapse.FALSE && length > 0) {
						return true;
				}
				for (int i = start; i < start + length; i++) {
						char ch = ca[i];
						if (!((ch == ' ') || (ch == '\n') || (ch == '\r') ||
										(ch == '\t'))) { // whitespace
								return true;
						}
				}
				return false;
		}

		public Status layout(Area area) throws FOPException {
				if (!(area instanceof BlockArea)) {
						MessageHandler.errorln("WARNING: text outside block area" +
																	 new String(ca, start, length));
						return new Status(Status.OK);
				}
				if (this.marker == START) {
						String fontFamily =
							this.parent.properties.get("font-family").getString();
						String fontStyle =
							this.parent.properties.get("font-style").getString();
						String fontWeight =
							this.parent.properties.get("font-weight").getString();
						int fontSize = this.parent.properties.get(
														 "font-size").getLength().mvalue();
						// font-variant support
						// added by Eric SCHAEFFER
						int fontVariant = this.parent.properties.get("font-variant").getEnum();

						this.fs = new FontState(area.getFontInfo(), fontFamily,
																		fontStyle, fontWeight, fontSize, fontVariant);

						ColorType c =
							this.parent.properties.get("color").getColorType();
						this.red = c.red();
						this.green = c.green();
						this.blue = c.blue();

						this.verticalAlign = this.parent.properties.get("vertical-align").getEnum();

						this.wrapOption =
							this.parent.properties.get("wrap-option").getEnum();
						this.whiteSpaceCollapse = this.parent.properties.get(
																				"white-space-collapse").getEnum(); 
						this.ts = new TextState();
						ts.setUnderlined(underlined);
						ts.setOverlined(overlined);
						ts.setLineThrough(lineThrough);

						this.marker = this.start;
				}
				int orig_start = this.marker;
				this.marker = addText((BlockArea)area, fs, red, green, blue,
											wrapOption, this.getLinkSet(), whiteSpaceCollapse, ca,
											this.marker, length, ts, verticalAlign);
				if (this.marker == -1) {


				// commented out by Hani Elabed, 11/28/2000
				// if this object has been laid out
				// successfully, leave it alone....
				// Now, to prevent the array index out of
				// bound of LineArea.addText(), I have added
				// the following test at the beginning of that method.
				// if( start == -1 ) return -1;
				// see LineArea.addText()

			//this.marker = 0;
						return new Status(Status.OK);
				} else if (this.marker != orig_start) {
						return new Status(Status.AREA_FULL_SOME);
				} else {
						return new Status(Status.AREA_FULL_NONE);
				}
		}

		// font-variant support : addText is a wrapper for addRealText
		// added by Eric SCHAEFFER
		public static int addText(BlockArea ba, FontState fontState, float red, float green,
											 float blue, int wrapOption, LinkSet ls,
											 int whiteSpaceCollapse, char data[], int start, int end,
											 TextState textState, int vAlign) {
			if (fontState.getFontVariant() == FontVariant.SMALL_CAPS) {
				FontState smallCapsFontState;
				try {
					int smallCapsFontHeight = (int) (((double) fontState.getFontSize()) * 0.8d);
					smallCapsFontState = new FontState(
						fontState.getFontInfo(),
						fontState.getFontFamily(),
						fontState.getFontStyle(),
						fontState.getFontWeight(),
						smallCapsFontHeight,
						FontVariant.NORMAL);
				} catch (FOPException ex) {
					smallCapsFontState = fontState;
					MessageHandler.errorln("Error creating small-caps FontState: " + ex.getMessage());
				}

				// parse text for upper/lower case and call addRealText
				char c;
				boolean isLowerCase;
				int caseStart;
				FontState fontStateToUse;
				for (int i = start; i < end; ) {
					caseStart = i;
					c = data[i];
					isLowerCase = (java.lang.Character.isLetter(c) && java.lang.Character.isLowerCase(c));
					while (isLowerCase == (java.lang.Character.isLetter(c) && java.lang.Character.isLowerCase(c))) {
						if (isLowerCase) {
							data[i] = java.lang.Character.toUpperCase(c);
						}
						i++;
						if (i == end)
							break;
						c = data[i];
					}
					if (isLowerCase) {
						fontStateToUse = smallCapsFontState;
					} else {
						fontStateToUse = fontState;
					}
					int index = addRealText(ba, fontStateToUse, red, green, blue, wrapOption, ls,
						whiteSpaceCollapse, data, caseStart, i, textState, vAlign);
					if (index != -1) {
						return index;
					}
				}

				return -1;
			}

			// font-variant normal
			return addRealText(ba, fontState, red, green, blue, wrapOption, ls,
				whiteSpaceCollapse, data, start, end, textState, vAlign);
		}

		protected static int addRealText(BlockArea ba, FontState fontState, float red, float green,
											 float blue, int wrapOption, LinkSet ls,
											 int whiteSpaceCollapse, char data[], int start, int end,
											 TextState textState, int vAlign) {
				int ts, te;
				char[] ca;

				ts = start;
				te = end;
				ca = data;

				LineArea la = ba.getCurrentLineArea();
				if (la == null) {
						return start;
				}

				la.changeFont(fontState);
				la.changeColor(red, green, blue);
				la.changeWrapOption(wrapOption);
				la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
				la.changeVerticalAlign(vAlign);
//				la.changeHyphenation(language, country, hyphenate,
//																 hyphenationChar, hyphenationPushCharacterCount,
//																 hyphenationRemainCharacterCount);
				ba.setupLinkSet(ls);

				ts = la.addText(ca, ts, te, ls, textState);
//				this.hasLines = true;

				while (ts != -1) {
						la = ba.createNextLineArea();
						if (la == null) {
								return ts;
						}
						la.changeFont(fontState);
						la.changeColor(red, green, blue);
						la.changeWrapOption(wrapOption);
						la.changeWhiteSpaceCollapse(
							whiteSpaceCollapse);
//						la.changeHyphenation(language, country, hyphenate,
//																		 hyphenationChar, hyphenationPushCharacterCount,
//																		 hyphenationRemainCharacterCount);
        				ba.setupLinkSet(ls);

						ts = la.addText(ca, ts, te, ls, textState);
				}
				return -1;
		}


}
