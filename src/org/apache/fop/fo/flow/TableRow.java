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
import org.apache.fop.datatypes.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;

// Java
import java.util.Vector;

public class TableRow extends FObj {

		public static class Maker extends FObj.Maker {
				public FObj make(FObj parent,
												 PropertyList propertyList) throws FOPException {
						return new TableRow(parent, propertyList);
				}
		}

		public static FObj.Maker maker() {
				return new TableRow.Maker();
		}

		boolean setup = false;

		FontState fs;
		int spaceBefore;
		int spaceAfter;
		int breakBefore;
		int breakAfter;
		ColorType backgroundColor;
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
		int paddingTop;
		int paddingBottom;
		int paddingLeft;
		int paddingRight;
		KeepValue keepWithNext;
		KeepValue keepWithPrevious;

		int widthOfCellsSoFar = 0;
		int largestCellHeight = 0;

		Vector columns;

		AreaContainer areaContainer;

		// added by Dresdner Bank, Germany
		DisplaySpace spacer = null;
		boolean hasAddedSpacer = false;
		DisplaySpace spacerAfter = null;
		boolean areaAdded = false;

		/**
		 * The list of cell states for this row. This is the location of
		 * where I will be storing the state of each cell so that I can
		 * spread a cell over multiple pages if I have to. This is part
		 * of fixing the TableRow larger than a single page bug.
		 * Hani Elabed, 11/22/2000.
		 */
		public Vector cells = null;

		/**
		 * CellState<BR>
		 *
		 * <B>Copyright @ 2000 Circuit Court Automation Program.
		 * state of Wisconsin.
		 * All Rights Reserved.</B>
		 * <p>
		 * This class is a container class for encapsulating a the
		 * state of a cell
		 * <TABLE>
		 * <TR><TD><B>Name:</B></TD>        <TD>CellState</TD></TR>
		 *
		 * <TR><TD><B>Purpose:</B></TD>     <TD>a helpful container class</TD></TR>
		 *
		 * <TR><TD><B>Description:</B></TD> <TD>This class is a container class for
		 *                                      encapsulating the state of a
		 *									   cell belonging to a TableRow class
		 *                                  </TD></TR>
		 *
		 * </TABLE>
		 *
		 * @author      Hani Elabed
		 * @version     0.14.0, 11/22/2000
		 * @since       JDK1.1
		 */
		public final class CellState {
				/** the cell location or index starting at 0.*/
				private int location;

				/** true if the layout of the cell was complete, false otherwise.*/
				private boolean layoutCompleted;

				/** the width of the cell so far.*/
				private int widthOfCellSoFar;

				private int column = 0;

				/**
				 * simple no args constructor.
				 */
				public CellState() {
						this(0, false, 0);
				}

				/**
				 * three argument fill everything constructor.
				 * @param int the location(index) of the cell.
				 * @param boolean flag of wether the cell was completely laid out or not.
				 * @param int the horizontal offset(width so far) of the cell.
				 */
				public CellState(int aLocation, boolean completed, int aWidth) {

						location = aLocation;
						layoutCompleted = completed;
						widthOfCellSoFar = aWidth;
				}

				/**
				 * returns the index of the cell starting at 0.
				 * @return int the location of the cell.
				 */
				public final int getLocation() {
						return location;
				}

				/**
				 * sets the location of the cell.
				 * @param int, the location of the cell.
				 */
				public final void setLocation(int aLocation) {
						location = aLocation;
				}


				/**
				 * returns true if the cell was completely laid out.
				 * @return false if cell was partially laid out.
				 */
				public final boolean isLayoutComplete() {
						return layoutCompleted;
				}

				/**
				 * sets the layoutCompleted flag.
				 * @param boolean, the layout Complete state of the cell.
				 */
				public final void setLayoutComplete(boolean completed) {
						layoutCompleted = completed;
				}


				/**
				 * returns the horizontal offset of the cell.
				 * @return int the horizontal offset of the cell, also known as width
				 * of the cell so far.
				 */
				public final int getWidthOfCellSoFar() {
						return widthOfCellSoFar;
				}

				/**
				 * sets the width of the Cell So Far, i.e the cell's offset.
				 * @param int, the horizontal offset of the cell.
				 */
				public final void setWidthOfCellSoFar(int aWidth) {
						widthOfCellSoFar = aWidth;
				}

				public int getColumn() {
						return column;
				}

				public void setColumn(int col) {
						column = col;
				}
		}


		public TableRow(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "fo:table-row";
		}

		public void setColumns(Vector columns) {
				this.columns = columns;
		}

		public KeepValue getKeepWithPrevious() {
				return keepWithPrevious;
		}

		public void doSetup(Area area) throws FOPException {
				String fontFamily = this.properties.get("font-family").getString();
				String fontStyle = this.properties.get("font-style").getString();
				String fontWeight = this.properties.get("font-weight").getString();
				int fontSize =
					this.properties.get("font-size").getLength().mvalue();
				// font-variant support
				// added by Eric SCHAEFFER
				int fontVariant =
					this.properties.get("font-variant").getEnum();

				this.fs = new FontState(area.getFontInfo(), fontFamily,
																fontStyle, fontWeight, fontSize, fontVariant);

				this.spaceBefore = this.properties.get(
														 "space-before.optimum").getLength().mvalue();
				this.spaceAfter = this.properties.get(
														"space-after.optimum").getLength().mvalue();
						this.breakBefore =
							this.properties.get("break-before").getEnum();
						this.breakAfter = this.properties.get("break-after").getEnum();
				this.backgroundColor =
					this.properties.get("background-color").getColorType();
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
				this.borderTopWidth =
					this.properties.get("border-width").getLength().mvalue();
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
				this.borderTopStyle = this.properties.get("border-style").getEnum();
				this.borderBottomStyle = this.borderTopStyle;
				this.borderLeftStyle = this.borderTopStyle;
				this.borderRightStyle = this.borderTopStyle;
				if (this.borderTopStyle == 0) {
						this.borderTopStyle =
							this.properties.get("border-top-style").getEnum();
						this.borderBottomStyle =
							this.properties.get("border-bottom-style").getEnum();
						this.borderLeftStyle =
							this.properties.get("border-left-style").getEnum();
						this.borderRightStyle =
							this.properties.get("border-right-style").getEnum();
				}
				this.keepWithNext = getKeepValue("keep-with-next.within-column");
				this.keepWithPrevious = getKeepValue("keep-with-previous.within-column");
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
				this.id = this.properties.get("id").getString();
				setup = true;
		}

		private KeepValue getKeepValue(String sPropName) {
				Property p= this.properties.get(sPropName);
				Number n = p.getNumber();
				if (n != null)
						return new KeepValue(KeepValue.KEEP_WITH_VALUE, n.intValue());
				switch(p.getEnum()) {
						case 2:
								return new KeepValue(KeepValue.KEEP_WITH_ALWAYS, 0);
						//break;
						case 1:
						default:
								return new KeepValue(KeepValue.KEEP_WITH_AUTO, 0);
						//break;
				}
		}

		public Status layout(Area area) throws FOPException {
				boolean configID = false;

				if (this.marker == BREAK_AFTER) {
						return new Status(Status.OK);
				}

				if (this.marker == START) {
						if (!setup)
								doSetup(area);

						if (area instanceof BlockArea) {
								area.end();
						}
			if (cells == null) { // check to make sure this row hasn't been partially
										 // laid out yet (with an id created already)
		area.getIDReferences().createID(id);
		configID = true;
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

						if (breakBefore == BreakBefore.COLUMN) {
								return new Status(Status.FORCE_COLUMN_BREAK);
						}
				}

				if ((spaceBefore != 0) && (this.marker == 0)) {
						spacer = new DisplaySpace(spaceBefore);
						area.increaseHeight(spaceBefore);
				}

				if (marker == 0 && configID) {
						// configure id
						area.getIDReferences().configureID(id, area);
				}

				this.areaContainer = new AreaContainer(fs, -area.borderWidthLeft,
																							 -area.borderWidthTop, area.getAllocationWidth(),
																							 area.spaceLeft(), Position.RELATIVE);
				areaContainer.foCreator=this;	// G Seshadri
				areaContainer.setPage(area.getPage());
				areaContainer.setPadding(paddingTop, paddingLeft,
																 paddingBottom, paddingRight);
				areaContainer.setBackgroundColor(backgroundColor);
				areaContainer.setBorderStyle(borderTopStyle, borderLeftStyle,
																		 borderBottomStyle, borderRightStyle);
				areaContainer.setBorderWidth(borderTopWidth, borderLeftWidth,
																		 borderBottomWidth, borderRightWidth);
				areaContainer.setBorderColor(borderTopColor, borderLeftColor,
																		 borderBottomColor, borderRightColor);
				areaContainer.start();

				areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
				areaContainer.setIDReferences(area.getIDReferences());

				// cells is The list of cell states for this row. This is the location of
				// where I will be storing the state of each cell so that I can
				// spread a cell over multiple pages if I have to. This is part
				// of fixing the TableRow larger than a single page bug.
				// Hani Elabed, 11/22/2000.
				if (cells == null)// do it once..
				{
						widthOfCellsSoFar = 0;
						cells = new Vector();
						int colCount = 0;
						int numChildren = this.children.size();
						for (int i = 0; i < numChildren; i++) {
								TableCell cell = (TableCell) children.elementAt(i);
								cell.doSetup(areaContainer);
								int numCols = cell.getNumColumnsSpanned();
								int numRows = cell.getNumRowsSpanned();
								int width = 0;
								CellState state =
									new CellState(i, false, widthOfCellsSoFar);
								state.setColumn(colCount);
								// add the state of a cell.
								cells.insertElementAt(state, i);
								if(colCount + numCols > columns.size()) {
									MessageHandler.errorln("WARNING: Number of cell columns under table-row not equal to number of table-columns");
									return new Status(Status.OK);
								}
								for (int count = 0;
												count < numCols && count < columns.size();
												count++) {
										width += ((TableColumn) columns.elementAt(colCount)).
														 getColumnWidth();
										colCount++;
								}

								cell.setWidth(width);
								widthOfCellsSoFar += width;

						}
						if(colCount < columns.size()) {
							MessageHandler.errorln("WARNING: Number of cell columns under table-row not equal to number of table-columns");
							return new Status(Status.OK);
						}
				}

				int numChildren = this.children.size();
				//	if (numChildren != columns.size()) {
				//	    MessageHandler.errorln("WARNING: Number of children under table-row not equal to number of table-columns");
				//	    return new Status(Status.OK);
				//	}

				// added by Eric Schaeffer
				largestCellHeight = 0;

				// added by Hani Elabed 11/27/2000
				boolean someCellDidNotLayoutCompletely = false;

				for (int i = this.marker; i < numChildren; i++) {
						TableCell cell = (TableCell) children.elementAt(i);

						// added by Hani Elabed 11/22/2000
						CellState cellState = (CellState) cells.elementAt(i);

						//if (this.isInListBody) {
						//fo.setIsInListBody();
						//fo.setDistanceBetweenStarts(this.distanceBetweenStarts);
						//fo.setBodyIndent(this.bodyIndent);
						//}


						//--- this is modified to preserve the state of start
						//--- offset of the cell.
						//--- change by Hani Elabed 11/22/2000
						cell.setStartOffset(cellState.getWidthOfCellSoFar());

						Status status;
						if ((status = cell.layout(areaContainer)).isIncomplete()) {
								this.marker = i;
								/*			if ((i != 0) && (status.getCode() == Status.AREA_FULL_NONE))
											{
													status = new Status(Status.AREA_FULL_SOME);
											}*/


								if (status.getCode() == Status.AREA_FULL_SOME) {
										// this whole block added by
										// Hani Elabed 11/27/2000

										cellState.setLayoutComplete(false);

										// locate the first cell
										// that need to be laid out further

										for (int j = 0; j < numChildren; j++) {
												CellState state = (CellState) cells.elementAt(j);

												if (! state.isLayoutComplete()) {
														this.marker = j;
														break; // out of for loop
												}
										}
								} else {
										// added on 11/28/2000, by Dresdner Bank, Germany
										if (spacer != null) {
												area.removeChild(spacer);
												spacer = null;
										}
										hasAddedSpacer = false;
										if(spacerAfter != null)
												area.removeChild(spacerAfter);
										spacerAfter = null;

										// removing something that was added by succession
										// of cell.layout()
										// just to keep my sanity here, Hani
										area.increaseHeight(areaContainer.getHeight());
										area.removeChild(areaContainer);
										this.resetMarker();
										this.removeID(area.getIDReferences());

										// hani elabed 11/27/2000
										cellState.setLayoutComplete(false);

										return status;
								}
						} else // layout was complete for a particular cell
						{ // Hani Elabed
								cellState.setLayoutComplete(true);
						}

						int h = cell.getHeight();
						if (h > largestCellHeight) {
								largestCellHeight = h;
						}
				}

				for (int i = 0; i < numChildren; i++) {
						TableCell cell = (TableCell) children.elementAt(i);
						cell.setRowHeight(largestCellHeight);
				}

				// added by Dresdner Bank, Germany
				if (!hasAddedSpacer && spacer != null) {
						area.addChild(spacer);
						hasAddedSpacer = true;
				}

				area.addChild(areaContainer);
				areaContainer.setHeight(largestCellHeight);
				areaAdded = true;
				areaContainer.end();
				area.addDisplaySpace(largestCellHeight +
														 areaContainer.getPaddingTop() +
														 areaContainer.borderWidthTop +
														 areaContainer.getPaddingBottom() +
														 areaContainer.borderWidthBottom);

				// bug fix from Eric Schaeffer
				//area.increaseHeight(largestCellHeight);

				// test to see if some cells are not
				// completely laid out.
				// Hani Elabed 11/22/2000
				for (int i = 0; i < numChildren; i++) {
						CellState cellState = (CellState) cells.elementAt(i);

						if (! cellState.isLayoutComplete()) {
								someCellDidNotLayoutCompletely = true;
								break; // out of for loop
						}
				}

				if (!someCellDidNotLayoutCompletely && spaceAfter != 0) {
						spacerAfter = new DisplaySpace(spaceAfter);
						area.addChild(spacerAfter);
						area.increaseHeight(spaceAfter);
				}

				if (area instanceof BlockArea) {
						area.start();
				}

				// replaced by Hani Elabed 11/27/2000
				//return new Status(Status.OK);

				if (someCellDidNotLayoutCompletely) {
						return new Status(Status.AREA_FULL_SOME);
				} else {
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
						if (keepWithNext.getType() != KeepValue.KEEP_WITH_AUTO) {
								return new Status(Status.KEEP_WITH_NEXT);
						}
						return new Status(Status.OK);
				}

		}

		public int getAreaHeight() {
				return areaContainer.getHeight();
		}

		public void removeLayout(Area area) {
				if (spacer != null) {
						if(hasAddedSpacer) {
								area.removeChild(spacer);
						} else {
								area.increaseHeight(-spaceBefore);
						}
				}
				if(spacerAfter != null)
						area.removeChild(spacerAfter);
				//area.increaseHeight(areaContainer.getHeight());
				if(areaAdded)
						area.removeChild(areaContainer);
				areaAdded = false;
				this.resetMarker();
				cells = null;
				this.removeID(area.getIDReferences());
		}

		public void resetMarker()
		{
				super.resetMarker();
				spacer = null;
				spacerAfter = null;
				hasAddedSpacer = false;
		}
}
