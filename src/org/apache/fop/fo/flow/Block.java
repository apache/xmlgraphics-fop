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

		int align;
		int alignLast;
		int breakAfter;
		int lineHeight;
		int startIndent;
		int endIndent;
		int spaceBefore;
		int spaceAfter;
		int textIndent;
		int keepWithNext;
		ColorType backgroundColor;
		int blockWidows;
		int blockOrphans;

		String id;
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

						this.align = this.properties.get("text-align").getEnum();
						this.alignLast =
							this.properties.get("text-align-last").getEnum();
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
						this.backgroundColor = this.properties.get("background-color").getColorType();

						this.blockWidows =
							this.properties.get("widows").getNumber().intValue();
						this.blockOrphans =
							this.properties.get("orphans").getNumber().intValue();



						this.id = this.properties.get("id").getString();

						if (area instanceof BlockArea) {
						    area.end();
						}

						if(area.getIDReferences() != null)
								area.getIDReferences().createID(id);

						this.marker = 0;
						int breakStatus;
  if ((breakStatus = propMgr.checkBreakBefore()) != Status.OK) {
    return new Status(breakStatus);
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
				this.blockArea = new BlockArea(propMgr.getFontState(area.getFontInfo()), area.getAllocationWidth(),
								area.spaceLeft(), startIndent, endIndent, textIndent,
								align, alignLast, lineHeight);
				this.blockArea.setParent(area);	// BasicLink needs it
				blockArea.setPage(area.getPage());
				blockArea.setBackgroundColor(backgroundColor);
				blockArea.setBorderAndPadding(propMgr.getBorderAndPadding());
				blockArea.setHyphenation(propMgr.getHyphenationProps());
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

				area.setMaxHeight(area.getMaxHeight() - spaceLeft + blockArea.getMaxHeight());

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
