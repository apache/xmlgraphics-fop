/*-- $Id$ --
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;

public class TableCell extends FObj {

		public static class Maker extends FObj.Maker {
				public FObj make(FObj parent, PropertyList propertyList)
						throws FOPException {
						return new TableCell(parent, propertyList);
				}
		}

		public static FObj.Maker maker() {
				return new TableCell.Maker();
		}

		//		int spaceBefore;
		//		int spaceAfter;
		ColorType backgroundColor;

		String id;
		int numColumnsSpanned;
		int numRowsSpanned;

		/** Offset of content rectangle in inline-progression-direction,
		 * relative to table.
		 */
		protected int startOffset;

		/** Dimension of content rectangle in inline-progression-direction */
		protected int width;

		/** Offset of content rectangle, in block-progression-direction,
		 * relative to the row.
		 */
		protected int beforeOffset;

		protected int height = 0;
		protected int top; // Ypos of cell ???
		protected int verticalAlign ;
		protected boolean bRelativeAlign = false;

		boolean setup = false;
		boolean bSepBorders = true;

		/** Border separation value in the block-progression dimension.
		 *  Used in calculating cells height.
		 */
		int m_borderSeparation = 0;

		AreaContainer cellArea;

		public TableCell(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "fo:table-cell";
		}

		// Set position relative to table (set by body?)
		public void setStartOffset(int offset) {
				startOffset = offset;
		}

		// Initially same as the column width containg this cell or the
		// sum of the spanned columns if numColumnsSpanned > 1
		public void setWidth(int width) {
				this.width = width;
		}

		public int getNumColumnsSpanned()
		{
				return numColumnsSpanned;
		}

		public int getNumRowsSpanned()
		{
				return numRowsSpanned;
		}

		public void doSetup(Area area) throws FOPException
		{
			this.numColumnsSpanned =
					this.properties.get("number-columns-spanned").getNumber().intValue();
			this.numRowsSpanned =
					this.properties.get("number-rows-spanned").getNumber().intValue();

			/**
			this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();
			this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue();
			**/
			this.backgroundColor =
					this.properties.get("background-color").getColorType();
			this.id =	this.properties.get("id").getString();
			bSepBorders = (this.properties.get("border-collapse").getEnum() ==
													BorderCollapse.SEPARATE);
			// Vertical cell alignment
			verticalAlign = this.properties.get("display-align").getEnum();
			if (verticalAlign == DisplayAlign.AUTO) {
					// Depends on all cells starting in row
					bRelativeAlign = true;
					verticalAlign = this.properties.get("relative-align").getEnum();
			}
			else bRelativeAlign = false; // Align on a per-cell basis

		}


		public Status layout(Area area) throws FOPException {
				int originalAbsoluteHeight = area.getAbsoluteHeight();
				if (this.marker == BREAK_AFTER) {
						return new Status(Status.OK);
				}

				if (this.marker == START) {
						if (!setup) {
								doSetup(area);
						}

						// Calculate cell borders
						calcBorders(propMgr.getBorderAndPadding());

						area.getIDReferences().createID(id);

						this.marker = 0;
				}

				/*
					if ((spaceBefore != 0) && (this.marker ==0)) {
					area.increaseHeight(spaceBefore);
					}
				*/

				if ( marker==0 ) {
						// configure id
						area.getIDReferences().configureID(id,area);
				}

				int spaceLeft = area.spaceLeft();

				// The Area position defines the content rectangle! Borders
				// and padding are outside of this rectangle.
				this.cellArea =
						new AreaContainer(propMgr.getFontState(area.getFontInfo()),
															startOffset, beforeOffset,
															width, area.spaceLeft()- m_borderSeparation/2,
															Position.RELATIVE);

				cellArea.foCreator=this;	// G Seshadri
				cellArea.setPage(area.getPage());
				cellArea.setBorderAndPadding(propMgr.getBorderAndPadding());
				cellArea.start();

				cellArea.setAbsoluteHeight(area.getAbsoluteHeight()); //???
				cellArea.setIDReferences(area.getIDReferences());
				//******** CHECK THIS: we've changed startOffset (KL)
				cellArea.setTableCellXOffset(startOffset);

				int numChildren = this.children.size();
				for (int i = this.marker; i < numChildren; i++) {
						FObj fo = (FObj) children.elementAt(i);
						fo.setIsInTableCell();
						fo.forceWidth(width); // ???

						Status status;
						if ((status = fo.layout(cellArea)).isIncomplete()) {
								this.marker = i;
								if ((i == 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
										return new Status(Status.AREA_FULL_NONE);
								} else {
										// hani Elabed 11/21/2000
										area.addChild(cellArea);
										area.setAbsoluteHeight(cellArea.getAbsoluteHeight());
										return new Status(Status.AREA_FULL_SOME);
								}
						}
						area.setMaxHeight(area.getMaxHeight() - spaceLeft +
															this.cellArea.getMaxHeight());
				}
				cellArea.end();
				area.addChild(cellArea);

				// This is the allocation height of the cell (including borders
				// and padding
				// ALSO need to include offsets if using "separate borders"
				height = cellArea.getHeight();
				top = cellArea.getCurrentYPosition(); // CHECK THIS!!!

				// reset absoluteHeight to beginning of row
				// area.setHeight(cellArea.getHeight() + spaceBefore + spaceAfter);
				// I don't think we should do this here (KL) !!!
				area.setHeight(cellArea.getHeight());
				area.setAbsoluteHeight(originalAbsoluteHeight);

				return new Status(Status.OK);
		}

		// TableRow calls this. Anyone else?
		public int getHeight() {
				// return cellArea.getHeight() + spaceBefore + spaceAfter;
				return cellArea.getHeight() + m_borderSeparation ;
		}

		/** Called by TableRow to set final size of cell content rectangles and
		 * to vertically align the actual content within the cell rectangle.
		 * Passed value is height of this row in the grid : allocation height
		 * of the cells (including any border separation values).
		 */
		public void setRowHeight(int h) {
				// This seems wierd. It's very old.
				// The height passed here is the total row height.
				// But we need to align the content of the cell.
				//	cellArea.setMaxHeight(h);
				// Increase content height by difference of row content height
				// and current cell allocation height (includes borders & padding)
				cellArea.increaseHeight(h - cellArea.getHeight());
				if (bRelativeAlign) {
						// Must get info for all cells starting in row!
						// verticalAlign can be BEFORE or BASELINE
				}
				else {
						int delta = h - getHeight();
						if (delta > 0) {
						switch(verticalAlign) {
						  case DisplayAlign.CENTER:
								// Increase cell padding before and after and change
								// "Y" position of content rectangle
									//	cellArea.getBorderAndPadding().setPaddingBefore(delta/2);
									//cellArea.getBorderAndPadding().setPaddingAfter(delta-delta/2);
									cellArea.shiftYPosition(delta/2);
								break;
						  case DisplayAlign.AFTER:
								// Increase cell padding before and change
								// "Y" position of content rectangle
									//cellArea.getBorderAndPadding().setPaddingBefore(delta);
									cellArea.shiftYPosition(delta);
								break;
						  case DisplayAlign.BEFORE:
						  default: // OK
								break;
						}
						}
				}
		}

		// Calculate cell border and padding
		private void calcBorders(BorderAndPadding bp) {
				if (this.bSepBorders) {
						/* Easy case.
						 * Cell border is the property specified directly on cell.
						 * Offset content rect by half the border-separation value,
						 * in addition to the border and padding values. Note:
						 * border-separate should only be specified on the table object,
						 * but it inherits.
						 */
						int iSep = properties.get("border-separation.inline-progression-direction").getLength().mvalue()/2;
						int contentOffset = iSep + bp.getBorderLeftWidth(false) +
								bp.getPaddingLeft(false);
						/*
						int contentOffset = iSep + bp.getBorderStartWidth(false) +
								bp.getPaddingStart(false);
						*/
						this.startOffset += contentOffset;
						this.width -= (contentOffset + iSep +
								bp.getBorderRightWidth(false) + bp.getPaddingRight(false));
						// bp.getBorderEndWidth(false) + bp.getPaddingEnd(false);
						// Offset of content rectangle in the block-progression direction
						m_borderSeparation =
							properties.get("border-separation.block-progression-direction").getLength().mvalue();
						this.beforeOffset = m_borderSeparation/2 +
								bp.getBorderTopWidth(false) +	bp.getPaddingTop(false);
						// bp.getBorderBeforeWidth(false) +	bp.getPaddingBefore(false);
				}
				else {
						//System.err.println("Collapse borders");
						/* Hard case.
						 * Cell border is combination of other cell borders, or table
						 * border for edge cells. Also seems to border values specified
						 * on row and column FO in the table (if I read CR correclty.)
						 */
						/*
							border-start
							If cell in column 1, then combine with table border-start props
							else combine with border-end props for preceding cell in this
							row. Look out for spanning rows.
							border-end
							If cell in last column, then combine with table border-end props
							else combine with border-start props for following cell in this
							row. Look out for spanning rows.
							border-before
							If cell in row 1 (of whole table, not just body),
							then combine with table border-before props,
							else combine with border-after props for preceding cell in this
							column. Look out for spanning columns.
							border-after
							If cell in last row (of whole table, not just body),
							then combine with table border-after props,
							else combine with border-before props for following cell in this
							column. Look out for spanning columns.
						*/

				}
		}
}
