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
package org.apache.fop.layout;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.flow.*;
import org.apache.fop.fo.*;
import org.apache.fop.apps.*;
import org.apache.fop.fo.properties.*;

// Java
import java.util.Vector;
import java.util.Enumeration;
import org.apache.fop.messaging.MessageHandler;

public class BlockArea extends Area {

		/* relative to area container */
		protected int startIndent;
		protected int endIndent;

		/* first line startIndent modifier */
		protected int textIndent;

		protected int lineHeight;

		protected int halfLeading;


		/* text-align of all but the last line */
		protected int align;

		/* text-align of the last line */
		protected int alignLastLine;

		protected LineArea currentLineArea;
		protected LinkSet currentLinkSet;

		/* have any line areas been used? */
		protected boolean hasLines = false;

		/*hyphenation*/
		protected int hyphenate;
		protected char hyphenationChar;
		protected int hyphenationPushCharacterCount;
		protected int hyphenationRemainCharacterCount;
		protected String language;
		protected String country;

		protected Vector pendingFootnotes = null;

		public BlockArea(FontState fontState, int allocationWidth,
										 int maxHeight, int startIndent, int endIndent,
										 int textIndent, int align, int alignLastLine, int lineHeight) {
				super(fontState, allocationWidth, maxHeight);

				this.startIndent = startIndent;
				this.endIndent = endIndent;
				this.textIndent = textIndent;
				this.contentRectangleWidth =
					allocationWidth - startIndent - endIndent;
				this.align = align;
				this.alignLastLine = alignLastLine;
				this.lineHeight = lineHeight;

				if (fontState != null)
						this.halfLeading = (lineHeight - fontState.getFontSize()) / 2;
		}

		public void render(Renderer renderer) {
				renderer.renderBlockArea(this);
		}

		public void addLineArea(LineArea la) {
				if (!la.isEmpty()) {
						this.addDisplaySpace(this.halfLeading);
						int size = la.getHeight();
						this.addChild(la);
						this.increaseHeight(size);
						this.addDisplaySpace(this.halfLeading);
				}
				// add pending footnotes
				if(pendingFootnotes != null) {
						for(Enumeration e = pendingFootnotes.elements(); e.hasMoreElements(); ) {
								FootnoteBody fb = (FootnoteBody)e.nextElement();
								Page page = getPage();
								if(!Footnote.layoutFootnote(page, fb, this)) {
										page.addPendingFootnote(fb);
								}
						}
						pendingFootnotes = null;
				}
		}

		public int addPageNumberCitation(FontState fontState, float red,
																		 float green, float blue, int wrapOption, LinkSet ls,
																		 int whiteSpaceCollapse, String refid) {

				this.currentLineArea.changeFont(fontState);
				this.currentLineArea.changeColor(red, green, blue);
				this.currentLineArea.changeWrapOption(wrapOption);
				this.currentLineArea.changeWhiteSpaceCollapse(whiteSpaceCollapse);
				this.currentLineArea.changeHyphenation(language, country, hyphenate,
																 hyphenationChar, hyphenationPushCharacterCount,
																 hyphenationRemainCharacterCount);


				if (ls != null) {
						this.currentLinkSet = ls;
						ls.setYOffset(currentHeight);
				}

				this.currentLineArea.addPageNumberCitation(refid, ls);
				this.hasLines = true;

				return -1;

		}

		// font-variant support : addText is a wrapper for addRealText
		// added by Eric SCHAEFFER
		public int addText(FontState fontState, float red, float green,
											 float blue, int wrapOption, LinkSet ls,
											 int whiteSpaceCollapse, char data[], int start, int end,
											 boolean ul) {
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
					int index = this.addRealText(fontStateToUse, red, green, blue, wrapOption, ls,
						whiteSpaceCollapse, data, caseStart, i, ul);
					if (index != -1) {
						return index;
					}
				}

				return -1;
			}

			// font-variant normal
			return this.addRealText(fontState, red, green, blue, wrapOption, ls,
				whiteSpaceCollapse, data, start, end, ul);
		}

		protected int addRealText(FontState fontState, float red, float green,
											 float blue, int wrapOption, LinkSet ls,
											 int whiteSpaceCollapse, char data[], int start, int end,
											 boolean ul) {
				int ts, te;
				char[] ca;

				ts = start;
				te = end;
				ca = data;

				if (currentHeight + currentLineArea.getHeight() > maxHeight) {
						return start;
				}

				this.currentLineArea.changeFont(fontState);
				this.currentLineArea.changeColor(red, green, blue);
				this.currentLineArea.changeWrapOption(wrapOption);
				this.currentLineArea.changeWhiteSpaceCollapse(whiteSpaceCollapse);
				this.currentLineArea.changeHyphenation(language, country, hyphenate,
																 hyphenationChar, hyphenationPushCharacterCount,
																 hyphenationRemainCharacterCount);
				if (ls != null) {
						this.currentLinkSet = ls;
						ls.setYOffset(currentHeight);
				}

				ts = this.currentLineArea.addText(ca, ts, te, ls, ul);
				this.hasLines = true;

				while (ts != -1) {
						this.currentLineArea.align(this.align);
						this.addLineArea(this.currentLineArea);

						this.currentLineArea =
							new LineArea(fontState, lineHeight, halfLeading,
													 allocationWidth, startIndent, endIndent,
													 currentLineArea);
						if (currentHeight + currentLineArea.getHeight() >
										this.maxHeight) {
								return ts;
						}
						this.currentLineArea.changeFont(fontState);
						this.currentLineArea.changeColor(red, green, blue);
						this.currentLineArea.changeWrapOption(wrapOption);
						this.currentLineArea.changeWhiteSpaceCollapse(
							whiteSpaceCollapse);
						this.currentLineArea.changeHyphenation(language, country, hyphenate,
																		 hyphenationChar, hyphenationPushCharacterCount,
																		 hyphenationRemainCharacterCount);
						if (ls != null) {
								ls.setYOffset(currentHeight);
						}

						ts = this.currentLineArea.addText(ca, ts, te, ls, ul);
				}
				return -1;
		}


		/**
			* adds a leader to current line area of containing block area
			* the actual leader area is created in the line area
			*
			* @return int +1 for success and -1 for none
			*/
		public int addLeader(FontState fontState, float red, float green,
												 float blue, int leaderPattern, int leaderLengthMinimum,
												 int leaderLengthOptimum, int leaderLengthMaximum,
												 int ruleThickness, int ruleStyle, int leaderPatternWidth,
												 int leaderAlignment) {

				//this should start a new page
				if (currentHeight + currentLineArea.getHeight() > maxHeight) {
						return -1;
				}

				this.currentLineArea.changeFont(fontState);
				this.currentLineArea.changeColor(red, green, blue);

				//check whether leader fits into the (rest of the) line
				//using length.optimum to determine where to break the line as defined
				// in the xsl:fo spec: "User agents may choose to use the value of 'leader-length.optimum'
				// to determine where to break the line" (7.20.4)
				//if leader is longer then create a new LineArea and put leader there
				if (leaderLengthOptimum <= (this.getContentWidth() -
																		this.currentLineArea.finalWidth -
																		this.currentLineArea.pendingWidth)) {
						this.currentLineArea.addLeader(leaderPattern,
																					 leaderLengthMinimum, leaderLengthOptimum,
																					 leaderLengthMaximum, ruleStyle, ruleThickness,
																					 leaderPatternWidth, leaderAlignment);
				} else {
						//finish current line area and put it into children vector
						this.currentLineArea.align(this.align);
						this.addLineArea(this.currentLineArea);

						//create new line area
						this.currentLineArea =
							new LineArea(fontState, lineHeight, halfLeading,
													 allocationWidth, startIndent, endIndent,
													 currentLineArea);
						this.currentLineArea.changeFont(fontState);
						this.currentLineArea.changeColor(red, green, blue);

						if (currentHeight + currentLineArea.getHeight() >
										this.maxHeight) {
								return -1;
						}

						//check whether leader fits into LineArea at all, otherwise
						//clip it (should honor the clip option of containing area)
						if (leaderLengthMinimum <=
											this.currentLineArea.getContentWidth()) {
								this.currentLineArea.addLeader(leaderPattern,
																							 leaderLengthMinimum, leaderLengthOptimum,
																							 leaderLengthMaximum, ruleStyle, ruleThickness,
																							 leaderPatternWidth, leaderAlignment);
						} else {
								MessageHandler.errorln("Leader doesn't fit into line, it will be clipped to fit.");
								this.currentLineArea.addLeader(leaderPattern,
																							 this.currentLineArea.getContentWidth() -
																							 this.currentLineArea.finalWidth -
																							 this.currentLineArea.pendingWidth,
																							 leaderLengthOptimum, leaderLengthMaximum,
																							 ruleStyle, ruleThickness, leaderPatternWidth,
																							 leaderAlignment);
						}
				}
				this.hasLines = true;
				return 1;
		}

		public void addCharacter(FontState fontState, float red, float green,
											 float blue, int wrapOption, LinkSet ls,
											 int whiteSpaceCollapse, char data, boolean ul) {

				this.currentLineArea.changeFont(fontState);
				this.currentLineArea.changeColor(red, green, blue);
				this.currentLineArea.changeWrapOption(wrapOption);
				this.currentLineArea.changeWhiteSpaceCollapse(whiteSpaceCollapse);

				if (ls != null) {
						this.currentLinkSet = ls;
						ls.setYOffset(currentHeight);
				}

				int marker = this.currentLineArea.addCharacter(data, ls, ul);
				//if character didn't fit into line, open a new one
				if (marker == org.apache.fop.fo.flow.Character.DOESNOT_FIT) {
						this.currentLineArea.align(this.align);
						this.addLineArea(this.currentLineArea);

						this.currentLineArea =
							new LineArea(fontState, lineHeight, halfLeading,
													 allocationWidth, startIndent, endIndent,
													 currentLineArea);
						this.currentLineArea.changeFont(fontState);
						this.currentLineArea.changeColor(red, green, blue);
						this.currentLineArea.changeWrapOption(wrapOption);
						this.currentLineArea.changeWhiteSpaceCollapse(
							whiteSpaceCollapse);
						if (ls != null) {
								this.currentLinkSet = ls;
								ls.setYOffset(currentHeight);
						}

						this.currentLineArea.addCharacter(data, ls, ul);
				}
				this.hasLines = true;
		}


		public void end() {
				if (this.hasLines) {
						this.currentLineArea.addPending();
						this.currentLineArea.align(this.alignLastLine);
						this.addLineArea(this.currentLineArea);
				}
		}

		public void start() {
				currentLineArea = new LineArea(fontState, lineHeight, halfLeading,
																			 allocationWidth, startIndent + textIndent, endIndent, null);
		}

		public int getEndIndent() {
				return endIndent;
		}

		public int getStartIndent() {
				return startIndent + paddingLeft + borderWidthLeft;
		}

		public void setIndents(int startIndent, int endIndent) {
				this.startIndent = startIndent;
				this.endIndent = endIndent;
				this.contentRectangleWidth =
					allocationWidth - startIndent - endIndent;
		}

		public int spaceLeft() {
				return maxHeight - currentHeight;
		}

		public int getHalfLeading() {
				return halfLeading;
		}

		public void setHyphenation(String language, String country, int hyphenate, char hyphenationChar,
																 int hyphenationPushCharacterCount,
																 int hyphenationRemainCharacterCount) {
			this.language = language;
			this.country = country;
			this.hyphenate =  hyphenate;
			this.hyphenationChar = hyphenationChar;
			this.hyphenationPushCharacterCount = hyphenationPushCharacterCount;
			this.hyphenationRemainCharacterCount = hyphenationRemainCharacterCount;
		}

		public void addFootnote(FootnoteBody fb) {
				if(pendingFootnotes == null) {
						pendingFootnotes = new Vector();
				}
				pendingFootnotes.addElement(fb);
		}
}
