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
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.*;

// Java
import java.util.Hashtable;
import java.util.Enumeration;

public class BlockContainer extends FObj {

		FontState fs;
		ColorType backgroundColor;
		int paddingTop;
		int paddingBottom;
		int paddingLeft;
		int paddingRight;
		int position;

		int top;
		int bottom;
		int left;
		int right;
		int width;
		int height;

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

	int span;

		AreaContainer areaContainer;

		public static class Maker extends FObj.Maker {
	public FObj make(FObj parent, PropertyList propertyList)
			throws FOPException {
			return new BlockContainer(parent, propertyList);
	}
		}

		public static FObj.Maker maker() {
	return new BlockContainer.Maker();
		}

		PageSequence pageSequence;

		protected BlockContainer(FObj parent, PropertyList propertyList)
	throws FOPException {
	super(parent, propertyList);
				this.name =  "fo:block-container";
				this.span = this.properties.get("span").getEnum();
		}

		public Status layout(Area area) throws FOPException {
	if (this.marker == START) {
			this.marker = 0;

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

			this.backgroundColor =
		this.properties.get("background-color").getColorType();
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

			this.position =
		this.properties.get("position").getEnum();
			this.top =
		this.properties.get("top").getLength().mvalue();
			this.bottom =
		this.properties.get("bottom").getLength().mvalue();
			this.left =
		this.properties.get("left").getLength().mvalue();
			this.right =
		this.properties.get("right").getLength().mvalue();
			this.width =
		this.properties.get("width").getLength().mvalue();
			this.height =
		this.properties.get("height").getLength().mvalue();

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

						span = this.properties.get("span").getEnum();

						// initialize id
						String id = this.properties.get("id").getString();
						area.getIDReferences().initializeID(id,area);
	}

	boolean prevChildMustKeepWithNext = false;

				AreaContainer container = (AreaContainer)area;
				if ((this.width == 0) && (this.height == 0)) {
					width = right - left;
					height = bottom - top;
				}
	this.areaContainer =
					new AreaContainer(fs, container.getXPosition() + left, container.getYPosition() - top, width, height,
														position);

	areaContainer.setPage(area.getPage());
	areaContainer.setBackgroundColor(backgroundColor);
	areaContainer.setPadding(paddingTop, paddingLeft, paddingBottom,
					 paddingRight);
				areaContainer.setBorderStyle(borderTopStyle, borderLeftStyle,
						 borderBottomStyle, borderRightStyle);
				areaContainer.setBorderWidth(borderTopWidth, borderLeftWidth,
						 borderBottomWidth, borderRightWidth);
				areaContainer.setBorderColor(borderTopColor, borderLeftColor,
						 borderBottomColor, borderRightColor);
	areaContainer.start();

				areaContainer.setAbsoluteHeight(area.getAbsoluteHeight());
				areaContainer.setIDReferences(area.getIDReferences());

	int numChildren = this.children.size();
	for (int i = this.marker; i < numChildren; i++) {
			FObj fo = (FObj) children.elementAt(i);
			Status status;
			if ((status = fo.layout(areaContainer)).isIncomplete()) {
							/*
		if ((prevChildMustKeepWithNext) && (status.laidOutNone())) {
				this.marker = i - 1;
				FObj prevChild = (FObj) children.elementAt(this.marker);
				prevChild.removeAreas();
				prevChild.resetMarker();
				return new Status(Status.AREA_FULL_SOME);
				// should probably return AREA_FULL_NONE if first
				// or perhaps an entirely new status code
		} else {
				this.marker = i;
				return status;
		}
							*/
			}
			if (status.getCode() == Status.KEEP_WITH_NEXT) {
		prevChildMustKeepWithNext = true;
			}
	}
				area.setAbsoluteHeight(areaContainer.getAbsoluteHeight());

	areaContainer.end();
				if (position == Position.ABSOLUTE)
					areaContainer.setHeight(height);
	area.addChild(areaContainer);

	return new Status(Status.OK);
		}

	/**
	 * Return the content width of the boxes generated by this block
	 * container FO.
	 */
	public int getContentWidth() {
		if (areaContainer != null)
			return areaContainer.getContentWidth(); //getAllocationWidth()??
		else return 0;  // not laid out yet
	}

	public boolean generatesReferenceAreas() {
	return true;
	}

	public int getSpan() {
		return this.span;
	}
}
