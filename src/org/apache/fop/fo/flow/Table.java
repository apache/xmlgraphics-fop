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

// Java
import java.util.Vector;

public class Table extends FObj {

		public static class Maker extends FObj.Maker {
				public FObj make(FObj parent,
												 PropertyList propertyList) throws FOPException {
						return new Table(parent, propertyList);
				}
		}

		public static FObj.Maker maker() {
				return new Table.Maker();
		}

		FontState fs;
		int breakBefore;
		int breakAfter;
		int spaceBefore;
		int spaceAfter;
		ColorType backgroundColor;
		int width;
		int height;
		ColorType borderColor;
		int borderWidth;
		int borderStyle;
		String id;
		TableHeader tableHeader = null;
		TableFooter tableFooter = null;
		boolean omitHeaderAtBreak = false;
		boolean omitFooterAtBreak = false;

		Vector columns = new Vector();
		int currentColumnNumber = 0;
		int bodyCount = 0;

		AreaContainer areaContainer;

		public Table(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "fo:table";
		}

		public Status layout(Area area) throws FOPException {
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

						this.breakBefore =
							this.properties.get("break-before").getEnum();
						this.breakAfter = this.properties.get("break-after").getEnum();
						this.spaceBefore = this.properties.get(
																 "space-before.optimum").getLength().mvalue();
						this.spaceAfter = this.properties.get(
																"space-after.optimum").getLength().mvalue();
						this.backgroundColor = this.properties.get(
																		 "background-color").getColorType();
						this.width = this.properties.get("width").getLength().mvalue();
						this.height =
							this.properties.get("height").getLength().mvalue();

						this.borderColor =
							this.properties.get("border-color").getColorType();
						this.borderWidth = this.properties.get(
																 "border-width").getLength().mvalue();
						this.borderStyle =
							this.properties.get("border-style").getEnum();
						this. id = this.properties.get("id").getString();

						this.omitHeaderAtBreak = this.properties.get("table-omit-header-at-break").getEnum() == TableOmitHeaderAtBreak.TRUE;
						this.omitFooterAtBreak = this.properties.get("table-omit-footer-at-break").getEnum() == TableOmitFooterAtBreak.TRUE;

						if (area instanceof BlockArea) {
								area.end();
						}
						if (this.areaContainer == null) { // check if anything was previously laid out
						    area.getIDReferences().createID(id);
						}
						

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
				}

				if ((spaceBefore != 0) && (this.marker == 0)) {
						area.addDisplaySpace(spaceBefore);
				}

				if (marker == 0 && areaContainer == null) {
						// configure id
						area.getIDReferences().configureID(id, area);
				}

				this.areaContainer =
					new AreaContainer(fs, 0, 0, area.getAllocationWidth(),
														area.spaceLeft(), Position.STATIC);
				areaContainer.foCreator=this;	// G Seshadri
				areaContainer.setPage(area.getPage());
				areaContainer.setBackgroundColor(backgroundColor);
				areaContainer.setBorderStyle(borderStyle, borderStyle,
																		 borderStyle, borderStyle);
				areaContainer.setBorderWidth(borderWidth, borderWidth,
																		 borderWidth, borderWidth);
				areaContainer.setBorderColor(borderColor, borderColor,
																		 borderColor, borderColor);
				areaContainer.start();

				areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
				areaContainer.setIDReferences(area.getIDReferences());

				// added by Eric Schaeffer
				currentColumnNumber = 0;
				int offset = 0;

				boolean addedHeader = false;
				boolean addedFooter = false;
				int numChildren = this.children.size();
				for (int i = 0; i < numChildren; i++) {
						FONode fo = (FONode) children.elementAt(i);
						if (fo instanceof TableColumn) {
								TableColumn c = (TableColumn) fo;
								int num = c.getColumnNumber();
								if (num == 0) {
										num = currentColumnNumber + 1;
								}
								currentColumnNumber = num;
								if (num > columns.size()) {
										columns.setSize(num);
								}
								columns.setElementAt(c, num - 1);
								c.setColumnOffset(offset);
								fo.layout(areaContainer);
								offset += c.getColumnWidth();
						}
				}
				areaContainer.setAllocationWidth(offset);

				for (int i = this.marker; i < numChildren; i++) {
						FONode fo = (FONode) children.elementAt(i);
						if (fo instanceof TableHeader) {
								if (columns.size() == 0) {
										MessageHandler.errorln("WARNING: current implementation of tables requires a table-column for each column, indicating column-width");
										return new Status(Status.OK);
								}
								tableHeader = (TableHeader) fo;
								tableHeader.setColumns(columns);
						} else if (fo instanceof TableFooter) {
								if (columns.size() == 0) {
										MessageHandler.errorln("WARNING: current implementation of tables requires a table-column for each column, indicating column-width");
										return new Status(Status.OK);
								}
								tableFooter = (TableFooter) fo;
								tableFooter.setColumns(columns);
						} else if (fo instanceof TableBody) {
								if (columns.size() == 0) {
										MessageHandler.errorln("WARNING: current implementation of tables requires a table-column for each column, indicating column-width");
										return new Status(Status.OK);
								}
								Status status;
								if (tableHeader != null && !addedHeader) {
										if ((status = tableHeader.layout(areaContainer)).
														isIncomplete()) {
												return new Status(Status.AREA_FULL_NONE);
										}
										addedHeader = true;
										tableHeader.resetMarker();
								}
								if (tableFooter != null && !this.omitFooterAtBreak && !addedFooter) {
										if ((status = tableFooter.layout(areaContainer)).
														isIncomplete()) {
												return new Status(Status.AREA_FULL_NONE);
										}
										addedFooter = true;
										tableFooter.resetMarker();
								}
								fo.setWidows(widows);
								fo.setOrphans(orphans);
								((TableBody) fo).setColumns(columns);

								if ((status = fo.layout(areaContainer)).isIncomplete()) {
										this.marker = i;
										if (bodyCount == 0 && status.getCode() == Status.AREA_FULL_NONE) {
												if (tableHeader != null)
														tableHeader.removeLayout(areaContainer);
												if (tableFooter != null)
														tableFooter.removeLayout(areaContainer);
												resetMarker();
												//			status = new Status(Status.AREA_FULL_SOME);
										}
										//areaContainer.end();
										if (areaContainer.getContentHeight() > 0) {
												area.addChild(areaContainer);
												area.increaseHeight(areaContainer.getHeight());
												area.setAbsoluteHeight(
													areaContainer.getAbsoluteHeight());
												if(this.omitHeaderAtBreak) {
														// remove header, no longer needed
														tableHeader = null;
												}
												if (tableFooter != null && !this.omitFooterAtBreak) {
														// move footer to bottom of area and move up body
														((TableBody) fo).setYPosition(
															tableFooter.getYPosition());
														tableFooter.setYPosition(
															tableFooter.getYPosition() +
															((TableBody) fo).getHeight());
												}
												setupColumnHeights();
												status = new Status(Status.AREA_FULL_SOME);
										}
										return status;
								} else {
										bodyCount++;
								}
								if (tableFooter != null && !this.omitFooterAtBreak) {
										// move footer to bottom of area and move up body
										// space before and after footer will make this wrong
										((TableBody) fo).setYPosition(
											tableFooter.getYPosition());
										tableFooter.setYPosition( tableFooter.getYPosition() +
																							((TableBody) fo).getHeight());
								}
						}
				}

				if (tableFooter != null && this.omitFooterAtBreak) {
						if (tableFooter.layout(areaContainer).isIncomplete()) {
								// this is a problem since we need to remove a row
								// from the last table body and place it on the
								// next page so that it can have a footer at
								// the end of the table.
								MessageHandler.errorln("WARNING: footer could not fit on page, moving last body row to next page");
								area.addChild(areaContainer);
								area.increaseHeight(areaContainer.getHeight());
								area.setAbsoluteHeight(
									areaContainer.getAbsoluteHeight());
								if(this.omitHeaderAtBreak) {
										// remove header, no longer needed
										tableHeader = null;
								}
								tableFooter.removeLayout(areaContainer);
								tableFooter.resetMarker();
								return new Status(Status.AREA_FULL_SOME);
						}
				}

				if (height != 0)
						areaContainer.setHeight(height);

				setupColumnHeights();

				areaContainer.end();
				area.addChild(areaContainer);

				/* should this be combined into above? */
				area.increaseHeight(areaContainer.getHeight());

				area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

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

				return new Status(Status.OK);
		}

		protected void setupColumnHeights()
		{
				int numChildren = this.children.size();
				for (int i = 0; i < numChildren; i++) {
						FONode fo = (FONode) children.elementAt(i);
						if (fo instanceof TableColumn) {
								((TableColumn) fo).setHeight(areaContainer.getHeight());
						}
				}
		}

		public int getAreaHeight() {
				return areaContainer.getHeight();
		}

		/**
		 * Return the content width of the boxes generated by this table FO.
		 */
		public int getContentWidth() {
				if (areaContainer != null)
						return areaContainer.getContentWidth(); //getAllocationWidth()??
				else
						return 0; // not laid out yet
		}

}
