/*-- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
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

		int breakBefore;
		int breakAfter;
		int spaceBefore;
		int spaceAfter;
		ColorType backgroundColor;
		int width;
		int height;
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

				int spaceLeft = area.spaceLeft();
				this.areaContainer =
					new AreaContainer(propMgr.getFontState(area.getFontInfo()), 0, 0, area.getAllocationWidth(),
														area.spaceLeft(), Position.STATIC);
				areaContainer.foCreator=this;	// G Seshadri
				areaContainer.setPage(area.getPage());
				areaContainer.setBackgroundColor(backgroundColor);
				areaContainer.setBorderAndPadding(propMgr.getBorderAndPadding());
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
								c.doSetup(areaContainer);
								int numColumnsRepeated = c.getNumColumnsRepeated();
								//int currentColumnNumber = c.getColumnNumber();
									
								for (int j = 0; j < numColumnsRepeated; j++) {
									currentColumnNumber++;
									if (currentColumnNumber > columns.size()) {
										columns.setSize(currentColumnNumber);
									}
									columns.setElementAt(c, currentColumnNumber - 1);
									c.setColumnOffset(offset);
									c.layout(areaContainer);
									offset += c.getColumnWidth();
								}
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
        								area.setMaxHeight(area.getMaxHeight() - spaceLeft + this.areaContainer.getMaxHeight());
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
								area.setMaxHeight(area.getMaxHeight() - spaceLeft + this.areaContainer.getMaxHeight());
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
								((TableColumn) fo).setHeight(areaContainer.getContentHeight());
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

// 		/**
// 		 * Return the last TableRow in the header or null if no header or
// 		 * no header in non-first areas.
// 		 * @param bForInitialArea If true, return the header row for the
// 		 * initial table area, else for a continuation area, taking into
// 		 * account the omit-header-at-break property.
// 		 */
// 		TableRow getLastHeaderRow(boolean bForInitialArea) {
// 				// Check omit...
// 				if ((tableHeader != null)  &&
// 						(bForInitialArea || omitHeaderAtBreak == false)) {
// 						return tableHeader.children.lastElement();
// 				}
// 				return null;
// 		}

// 		/**
// 		 * Return the first TableRow in the footer or null if no footer or
// 		 * no footer in non-last areas.
// 		 * @param bForFinalArea If true, return the footer row for the
// 		 * final table area, else for a non-final area, taking into
// 		 * account the omit-footer-at-break property.
// 		 */
// 		TableRow getLastFooterRow(boolean bForFinalArea) {
// 				if ((tableFooter != null) &&
// 						(bForFinalArea || omitFooterAtBreak == false)) {
// 						return tableFooter.children.firstElement();
// 				}
// 				return null;
// 		}


// 		/**
// 		 * Return border information for the side (start/end) of the column
// 		 * whose number is iColNumber (first column = 1).
// 		 * ATTENTION: for now we assume columns are in order in the array!
// 		 */
// 		BorderInfo getColumnBorder(BorderInfo.Side side, int iColNumber) {
// 				TableColumn col = (TableColumn)columns.elementAt(iColNumber);
// 				return col.getBorderInfo(side);
// 		}
}
