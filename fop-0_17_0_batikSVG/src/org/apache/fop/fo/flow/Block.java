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

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.apps.FOPException;

public class Block extends FObjMixed {

		public static class Maker extends FObj.Maker {
				public FObj make(FObj parent,
												 PropertyList propertyList) throws FOPException {
						return new Block(parent, propertyList);
				}
		}

		public static FObj.Maker maker() {
				return new Block.Maker();
		}

		FontState fs;
		int align;
		int alignLast;
		int breakBefore;
		int breakAfter;
		int lineHeight;
		int startIndent;
		int endIndent;
		int spaceBefore;
		int spaceAfter;
		int textIndent;
		int keepWithNext;
		ColorType backgroundColor;
		int paddingTop;
		int paddingBottom;
		int paddingLeft;
		int paddingRight;
		int blockWidows;
		int blockOrphans;

		String id;

		ColorType borderTopColor;
		int borderTopWidth;
		int borderTopStyle;
		ColorType borderBottomColor;
		int borderBottomWidth;
		int borderBottomStyle;
		ColorType borderLeftColor;
		int borderLeftWidth;
		int borderLeftStyle;
		ColorType borderRightColor;
		int borderRightWidth;
		int borderRightStyle;

		int hyphenate;
		char hyphenationChar;
		int hyphenationPushCharacterCount;
		int hyphenationRemainCharacterCount;
		String language;
		String country;
	int span;

		BlockArea blockArea;

		// this may be helpful on other FOs too
		boolean anythingLaidOut = false;

		public Block(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "fo:block";
				this.span = this.properties.get("span").getEnum();
		}

		public Status layout(Area area) throws FOPException {
				// MessageHandler.error(" b:LAY[" + marker + "] ");


				if (this.marker == BREAK_AFTER) {
						return new Status(Status.OK);
				}

				if (this.marker == START) {
						String fontFamily =
							this.properties.get("font-family").getString();
						String fontStyle =
							this.properties.get("font-style").getString();
						String fontWeight =
							this.properties.get("font-weight").getString();
						int fontSize =
							this.properties.get("font-size").getLength().mvalue();

						// font-variant support
						// added by Eric SCHAEFFER
						int fontVariant =
							this.properties.get("font-variant").getEnum();

						this.fs = new FontState(area.getFontInfo(), fontFamily,
																		fontStyle, fontWeight, fontSize, fontVariant);

						this.align = this.properties.get("text-align").getEnum();
						this.alignLast =
							this.properties.get("text-align-last").getEnum();
						this.breakBefore =
							this.properties.get("break-before").getEnum();
						this.breakAfter = this.properties.get("break-after").getEnum();
						this.lineHeight = this.properties.get(
																"line-height").getLength().mvalue();
						this.startIndent = this.properties.get(
																 "start-indent").getLength().mvalue();
						this.endIndent = this.properties.get(
															 "end-indent").getLength().mvalue();
						this.spaceBefore = this.properties.get(
																 "space-before.optimum").getLength().mvalue();
						this.spaceAfter = this.properties.get(
																"space-after.optimum").getLength().mvalue();
						this.textIndent = this.properties.get(
																"text-indent").getLength().mvalue();
						this.keepWithNext =
							this.properties.get("keep-with-next").getEnum();
						this.backgroundColor = this.properties.get(
																		 "background-color").getColorType();
						this.paddingTop =
							this.properties.get("padding").getLength().mvalue();

						this.paddingLeft = this.paddingTop;
						this.paddingRight = this.paddingTop;
						this.paddingBottom = this.paddingTop;
						if (this.paddingTop == 0) {
								this.paddingTop = this.properties.get(
																		"padding-top").getLength().mvalue();
								this.paddingLeft = this.properties.get(
																		 "padding-left").getLength().mvalue();
								this.paddingBottom = this.properties.get(
																			 "padding-bottom").getLength().mvalue();
								this.paddingRight = this.properties.get(
																			"padding-right").getLength().mvalue();
						}
						this.borderTopColor =
							this.properties.get("border-color").getColorType();
						this.borderBottomColor = this.borderTopColor;
						this.borderLeftColor = this.borderTopColor;
						this.borderRightColor = this.borderTopColor;
						if (this.borderTopColor == null) {
								this.borderTopColor = this.properties.get(
																				"border-top-color").getColorType();
								this.borderBottomColor = this.properties.get(
																					 "border-bottom-color").getColorType();
								this.borderLeftColor = this.properties.get(
																				 "border-left-color").getColorType();
								this.borderRightColor = this.properties.get(
																					"border-right-color").getColorType();
						}
						this.borderTopWidth = this.properties.get(
																		"border-width").getLength().mvalue();
						this.borderBottomWidth = this.borderTopWidth;
						this.borderLeftWidth = this.borderTopWidth;
						this.borderRightWidth = this.borderTopWidth;
						if (this.borderTopWidth == 0) {
								this.borderTopWidth = this.properties.get(
																				"border-top-width").getLength().mvalue();
								this.borderBottomWidth = this.properties.get(
																					 "border-bottom-width").getLength().mvalue();
								this.borderLeftWidth = this.properties.get(
																				 "border-left-width").getLength().mvalue();
								this.borderRightWidth = this.properties.get(
																					"border-right-width").getLength().mvalue();
						}
						this.borderTopStyle =
							this.properties.get("border-style").getEnum();
						this.borderBottomStyle = this.borderTopStyle;
						this.borderLeftStyle = this.borderTopStyle;
						this.borderRightStyle = this.borderTopStyle;
						if (this.borderTopStyle == 0) {
								this.borderTopStyle =
									this.properties.get("border-top-style").getEnum();
								this.borderBottomStyle = this.properties.get(
																					 "border-bottom-style").getEnum();
								this.borderLeftStyle = this.properties.get(
																				 "border-left-style").getEnum();
								this.borderRightStyle = this.properties.get(
																					"border-right-style").getEnum();
						}
						this.blockWidows =
							this.properties.get("widows").getNumber().intValue();
						this.blockOrphans =
							this.properties.get("orphans").getNumber().intValue();

						this.hyphenate = this.properties.get("hyphenate").getEnum();
						this.hyphenationChar = this.properties.get("hyphenation-character").getCharacter();
						this.hyphenationPushCharacterCount = this.properties.get(
																									 "hyphenation-push-character-count").getNumber().
																								 intValue();
						this.hyphenationRemainCharacterCount = this.properties.get(
																										 "hyphenation-remain-character-count").getNumber().
																									 intValue();
						this.language = this.properties.get("language").getString();
						this.country = this.properties.get("country").getString();

						this.id = this.properties.get("id").getString();

						if (area instanceof BlockArea) {
								area.end();
						}

						if(area.getIDReferences() != null)
								area.getIDReferences().createID(id);

						this.marker = 0;

						if (breakBefore == BreakBefore.PAGE) {
								return new Status(Status.FORCE_PAGE_BREAK);
						}

						if (breakBefore == BreakBefore.ODD_PAGE) {
								return new Status(Status.FORCE_PAGE_BREAK_ODD);
						}

						if (breakBefore == BreakBefore.EVEN_PAGE) {
								return new Status(Status.FORCE_PAGE_BREAK_EVEN);
						}

						if (breakBefore == BreakBefore.COLUMN) {
								return new Status(Status.FORCE_COLUMN_BREAK);
						}

						int numChildren = this.children.size();
						for (int i = 0; i < numChildren; i++) {
								FONode fo = (FONode) children.elementAt(i);
								if (fo instanceof FOText) {
										if (((FOText) fo).willCreateArea()) {
												fo.setWidows(blockWidows);
												break;
										} else {
												children.removeElementAt(i);
												numChildren = this.children.size();
												i--;
										}
								} else {
										fo.setWidows(blockWidows);
										break;
								}
						}

						for (int i = numChildren - 1; i >= 0; i--) {
								FONode fo = (FONode) children.elementAt(i);
								if (fo instanceof FOText) {
										if (((FOText) fo).willCreateArea()) {
												fo.setOrphans(blockOrphans);
												break;
										}
								} else {
										fo.setOrphans(blockOrphans);
										break;
								}
						}
				}

				if ((spaceBefore != 0) && (this.marker == 0)) {
						area.addDisplaySpace(spaceBefore);
				}

				if (anythingLaidOut) {
						this.textIndent = 0;
				}

				if (marker == 0 && area.getIDReferences() != null) {
						area.getIDReferences().configureID(id, area);
				}

				int spaceLeft = area.spaceLeft();
				this.blockArea = new BlockArea(fs, area.getAllocationWidth(),
																			 area.spaceLeft(), startIndent, endIndent, textIndent,
																			 align, alignLast, lineHeight);
				this.blockArea.setParent(area);	// BasicLink needs it
				blockArea.setPage(area.getPage());
				blockArea.setBackgroundColor(backgroundColor);
				blockArea.setPadding(paddingTop, paddingLeft, paddingBottom,
														 paddingRight);
				blockArea.setBorderStyle(borderTopStyle, borderLeftStyle,
																 borderBottomStyle, borderRightStyle);
				blockArea.setBorderWidth(borderTopWidth, borderLeftWidth,
																 borderBottomWidth, borderRightWidth);
				blockArea.setBorderColor(borderTopColor, borderLeftColor,
																 borderBottomColor, borderRightColor);
				blockArea.setHyphenation(language, country, hyphenate,
																 hyphenationChar, hyphenationPushCharacterCount,
																 hyphenationRemainCharacterCount);
				blockArea.start();

				blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
				blockArea.setIDReferences(area.getIDReferences());

				blockArea.setTableCellXOffset(area.getTableCellXOffset());

				int numChildren = this.children.size();
				for (int i = this.marker; i < numChildren; i++) {
						FONode fo = (FONode) children.elementAt(i);
						Status status;
						if ((status = fo.layout(blockArea)).isIncomplete()) {
								this.marker = i;
								// this block was modified by
								// Hani Elabed 11/27/2000
								//if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE))
								//{
								//    status = new Status(Status.AREA_FULL_SOME);
								//}

								// new block to replace the one above
								// Hani Elabed 11/27/2000
								if (status.getCode() == Status.AREA_FULL_NONE) {
										// something has already been laid out
										if ((i != 0)) {
												status = new Status(Status.AREA_FULL_SOME);
												area.addChild(blockArea);
												area.setMaxHeight(area.getMaxHeight() - spaceLeft + blockArea.getMaxHeight());
												area.increaseHeight(blockArea.getHeight());
												area.setAbsoluteHeight(
													blockArea.getAbsoluteHeight());
												anythingLaidOut = true;

												return status;
										} else // i == 0 nothing was laid out..
										{
												anythingLaidOut = false;
												return status;
										}
								}

								//blockArea.end();
								area.addChild(blockArea);
								area.setMaxHeight(area.getMaxHeight() - spaceLeft + blockArea.getMaxHeight());
								area.increaseHeight(blockArea.getHeight());
								area.setAbsoluteHeight(blockArea.getAbsoluteHeight());
								anythingLaidOut = true;
								return status;
						}
						anythingLaidOut = true;
				}

				blockArea.end();
				area.addChild(blockArea);

				/* should this be combined into above? */
				area.increaseHeight(blockArea.getHeight());

				area.setAbsoluteHeight(blockArea.getAbsoluteHeight());

				if (spaceAfter != 0) {
						area.addDisplaySpace(spaceAfter);
				}

				if (area instanceof BlockArea) {
						area.start();
				}

				if (breakAfter == BreakAfter.PAGE) {
						this.marker = BREAK_AFTER;
						return new Status(Status.FORCE_PAGE_BREAK);
				}

				if (breakAfter == BreakAfter.ODD_PAGE) {
						this.marker = BREAK_AFTER;
						return new Status(Status.FORCE_PAGE_BREAK_ODD);
				}

				if (breakAfter == BreakAfter.EVEN_PAGE) {
						this.marker = BREAK_AFTER;
						return new Status(Status.FORCE_PAGE_BREAK_EVEN);
				}

				if (breakAfter == BreakAfter.COLUMN) {
						this.marker = BREAK_AFTER;
						return new Status(Status.FORCE_COLUMN_BREAK);
				}

				if (keepWithNext != 0) {
						return new Status(Status.KEEP_WITH_NEXT);
				}

				//MessageHandler.error(" b:OK" + marker + " ");
				return new Status(Status.OK);
		}

		public int getAreaHeight() {
				return blockArea.getHeight();
		}


	/**
	 * Return the content width of the boxes generated by this FO.
	 */
	public int getContentWidth() {
		if (blockArea != null)
			return blockArea.getContentWidth(); //getAllocationWidth()??
		else return 0;  // not laid out yet
	}

	public int getSpan() {
		return this.span;
	}
}
