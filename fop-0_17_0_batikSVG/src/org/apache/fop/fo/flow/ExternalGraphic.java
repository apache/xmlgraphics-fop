/*
 * ============================================================================
 * The Apache Software License, Version 1.1
 * ============================================================================
 * Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. The end-user documentation included with the redistribution, if any, must
 * include  the following  acknowledgment:  "This product includes  software
 * developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 * Alternately, this  acknowledgment may  appear in the software itself,  if
 * and wherever such third-party acknowledgments normally appear.
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 * endorse  or promote  products derived  from this  software without  prior
 * written permission. For written permission, please contact
 * apache@apache.org.
 * 5. Products  derived from this software may not  be called "Apache", nor may
 * "Apache" appear  in their name,  without prior written permission  of the
 * Apache Software Foundation.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache
 * Software Foundation, please see <http://www.apache.org/>. */
/* Modified by Eric SCHAEFFER */

package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.*;
import org.apache.fop.messaging.MessageHandler;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.FontState;
import org.apache.fop.apps.FOPException;
import org.apache.fop.image.*;

// Java
import java.util.Enumeration;
import java.util.Hashtable;
import java.net.URL;
import java.net.MalformedURLException;

public class ExternalGraphic extends FObj {

	FontState fs;
	int align;
	int startIndent;
	int endIndent;
	int spaceBefore;
	int spaceAfter;
	String src;
	int height;
	int width;
	String id;

	ImageArea imageArea;


	public ExternalGraphic(FObj parent, PropertyList propertyList) {
		super(parent, propertyList);
		this.name = "fo:external-graphic";
	}


	public Status layout(Area area) throws FOPException {

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

			// FIXME
			this.align = this.properties.get("text-align").getEnum();

			this.startIndent = this.properties.get(
					"start-indent").getLength().mvalue();
			this.endIndent = this.properties.get(
					"end-indent").getLength().mvalue();

			this.spaceBefore = this.properties.get(
					"space-before.optimum").getLength().mvalue();
			this.spaceAfter = this.properties.get(
					"space-after.optimum").getLength().mvalue();

			this.src = this.properties.get("src").getString();

			this.width = this.properties.get("width").getLength().mvalue();

			this.height =
					this.properties.get("height").getLength().mvalue();

			this.id = this.properties.get("id").getString();

			area.getIDReferences().createID(id);

			if (area instanceof BlockArea) {
				area.end();
			}

			if (this.isInTableCell) {
				startIndent += forcedStartOffset;
				endIndent = area.getAllocationWidth() - forcedWidth -
						forcedStartOffset;
			}

			this.marker = 0;
		}

		try {
			FopImage img = FopImageFactory.Make(src);
			// if width / height needs to be computed
			if ((width == 0) || (height == 0)) {
				// aspect ratio
				double imgWidth = img.getWidth();
				double imgHeight = img.getHeight();
				if ((width == 0) && (height == 0)) {
					width = (int) ((imgWidth * 1000d));
					height = (int) ((imgHeight * 1000d));
				}
				else if (height == 0) {
					height = (int) ((imgHeight * ((double) width)) /
							imgWidth);
				}
				else if (width == 0) {
					width = (int) ((imgWidth * ((double) height)) /
							imgHeight);
				}
			}

			// scale image if it doesn't fit in the area/page
			// Need to be more tested...
			double ratio = ((double) width) / ((double) height);
			int areaWidth = area.getAllocationWidth() - startIndent - endIndent;
			int pageHeight = area.getPage().getHeight();
			if (height > pageHeight) {
				height = pageHeight;
				width = (int) (ratio * ((double) height));
			}
			if (width > areaWidth) {
				width = areaWidth;
				height = (int) (((double) width) / ratio);
			}

			if (area.spaceLeft() < (height + spaceBefore)) {
				return new Status(Status.AREA_FULL_NONE);
			}

			this.imageArea =
					new ImageArea(fs, img, area.getAllocationWidth(),
					width, height, startIndent, endIndent, align);

			if ((spaceBefore != 0) && (this.marker == 0)) {
				area.addDisplaySpace(spaceBefore);
			}

			if (marker == 0) {
				// configure id
				area.getIDReferences().configureID(id, area);
			}

			imageArea.start();
			imageArea.end();
			area.addChild(imageArea);
			area.increaseHeight(imageArea.getHeight());

			if (spaceAfter != 0) {
				area.addDisplaySpace(spaceAfter);
			}

		}
		catch (MalformedURLException urlex) {
			// bad URL
			MessageHandler.errorln("Error while creating area : " +
					urlex.getMessage());
		}
		catch (FopImageException imgex) {
			// image error
			MessageHandler.errorln("Error while creating area : " +
					imgex.getMessage());
		}

		if (area instanceof BlockArea) {
			area.start();
		}

		return new Status(Status.OK);
	}


	public static FObj.Maker maker() {
		return new ExternalGraphic.Maker();
	}


	public static class Maker extends FObj.Maker {
		public FObj make(FObj parent,
				PropertyList propertyList) throws FOPException {
			return new ExternalGraphic(parent, propertyList);
		}
	}
}

