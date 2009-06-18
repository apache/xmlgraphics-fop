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

		int spaceBefore;
		int spaceAfter;
		ColorType backgroundColor;

		FontState fs;
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
		int position;
		String id;
		int numColumnsSpanned;
		int numRowsSpanned;

		protected int startOffset;
		protected int width;

		protected int height = 0;
		protected int top;
		protected int verticalAlign = VerticalAlign.BASELINE;

		boolean setup = false;

		AreaContainer areaContainer;

		public TableCell(FObj parent, PropertyList propertyList) {
	super(parent, propertyList);
	this.name = "fo:table-cell";
		}

		public void setStartOffset(int offset) {
	startOffset = offset;
		}

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

			this.borderTopColor =
		this.properties.get("border-color").getColorType();
			this.borderBottomColor = this.borderTopColor;
			this.borderLeftColor = this.borderTopColor;
			this.borderRightColor = this.borderTopColor;
			if (this.borderTopColor == null) {
		this.borderTopColor =
				this.properties.get("border-top-color").getColorType();
		this.borderBottomColor =
				this.properties.get("border-bottom-color").getColorType();
		this.borderLeftColor =
				this.properties.get("border-left-color").getColorType();
		this.borderRightColor =
				this.properties.get("border-right-color").getColorType();
			}
			this.borderTopWidth =
		this.properties.get("border-width").getLength().mvalue();
			this.borderBottomWidth = this.borderTopWidth;
			this.borderLeftWidth = this.borderTopWidth;
			this.borderRightWidth = this.borderTopWidth;
			if (this.borderTopWidth == 0) {
		this.borderTopWidth =
				this.properties.get("border-top-width").getLength().mvalue();
		this.borderBottomWidth =
				this.properties.get("border-bottom-width").getLength().mvalue();
		this.borderLeftWidth =
				this.properties.get("border-left-width").getLength().mvalue();
		this.borderRightWidth =
				this.properties.get("border-right-width").getLength().mvalue();
			}
			this.borderTopStyle =
		this.properties.get("border-style").getEnum();
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
			this.paddingTop =
		this.properties.get("padding").getLength().mvalue();
						this.paddingLeft = this.paddingTop;
						this.paddingRight = this.paddingTop;
						this.paddingBottom = this.paddingTop;
						if (this.paddingTop == 0) {
				this.paddingTop =
			this.properties.get("padding-top").getLength().mvalue();
				this.paddingLeft =
			this.properties.get("padding-left").getLength().mvalue();
				this.paddingBottom =
			this.properties.get("padding-bottom").getLength().mvalue();
				this.paddingRight =
			this.properties.get("padding-right").getLength().mvalue();
						}

			this.spaceBefore =
		this.properties.get("space-before.optimum").getLength().mvalue();
			this.spaceAfter =
		this.properties.get("space-after.optimum").getLength().mvalue();
			this.backgroundColor =
		this.properties.get("background-color").getColorType();
						this.id =
								this.properties.get("id").getString();
		}

		public Status layout(Area area) throws FOPException {
	int originalAbsoluteHeight = area.getAbsoluteHeight();
				if (this.marker == BREAK_AFTER) {
			return new Status(Status.OK);
	}

	if (this.marker == START) {
			if(!setup)
					doSetup(area);

			if (area instanceof BlockArea) {
		area.end();
			}

			//if (this.isInListBody) {
			//startIndent += bodyIndent + distanceBetweenStarts;
			//}

						area.getIDReferences().createID(id);

			this.marker = 0;

	}

	if ((spaceBefore != 0) && (this.marker ==0)) {
			area.increaseHeight(spaceBefore);
	}

				if ( marker==0 ) {
						// configure id
						area.getIDReferences().configureID(id,area);
				}

	this.areaContainer =
			new AreaContainer(fs, startOffset - area.borderWidthLeft,
															- area.borderWidthTop + ((this.marker ==0) ? spaceBefore : 0),
													width, area.spaceLeft(), Position.RELATIVE);
	areaContainer.foCreator=this;	// G Seshadri
	areaContainer.setPage(area.getPage());
	areaContainer.setPadding(paddingTop, paddingLeft, paddingBottom,
					 paddingRight);
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
				areaContainer.setTableCellXOffset(startOffset);
	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
			FObj fo = (FObj) children.elementAt(i);
			fo.setIsInTableCell();
			fo.forceWidth(width);
			verticalAlign = fo.properties.get("vertical-align").getEnum();
			Status status;
			if ((status = fo.layout(areaContainer)).isIncomplete()) {
		this.marker = i;
		if ((i == 0) && (status.getCode() == Status.AREA_FULL_NONE)) {
				return new Status(Status.AREA_FULL_NONE);
		} else {
					// hani Elabed 11/21/2000
			 area.addChild(areaContainer);
					area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

				return new Status(Status.AREA_FULL_SOME);
		}
			}
	}
	areaContainer.end();
	area.addChild(areaContainer);

	height = areaContainer.getHeight();
	top = areaContainer.getCurrentYPosition();
	// reset absoluteHeight to beginning of row
	area.setHeight(areaContainer.getHeight() + spaceBefore + spaceAfter);
	area.setAbsoluteHeight(originalAbsoluteHeight);

	return new Status(Status.OK);
		}

		public int getHeight() {
	return areaContainer.getHeight() + spaceBefore + spaceAfter;
		}

		public void setRowHeight(int h) {
				areaContainer.setMaxHeight(h);
			switch(verticalAlign) {
					case VerticalAlign.MIDDLE:
							areaContainer.setHeight(height);
							areaContainer.setYPosition(spaceBefore + top + h / 2 - height / 2);
				break;
				case VerticalAlign.BASELINE:
				default:
							areaContainer.setHeight(h - (spaceBefore + spaceAfter));
				break;
			}
		}
}
