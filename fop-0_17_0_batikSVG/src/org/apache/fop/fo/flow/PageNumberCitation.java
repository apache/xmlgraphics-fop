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
import org.apache.fop.fo.pagination.*;
import org.apache.fop.datatypes.*;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;
import org.apache.fop.apps.FOPException;


/**
* 6.6.11 fo:page-number-citation
*
*      Common Usage:
*        The fo:page-number-citation is used to reference the page-number for the page containing the first normal area returned by
*        the cited formatting object.
*
*           NOTE:
*            It may be used to provide the page-numbers in the table of contents, cross-references, and index entries.
*
*        Areas:
*        The fo:page-number-citation formatting object generates and returns a single normal inline-area.
*        Constraints:
*
*        The cited page-number is the number of the page containing, as a descendant, the first normal area returned by the
*        formatting object with an id trait matching the ref-id trait of the fo:page-number-citation (the referenced formatting
*        object).
*
*        The cited page-number string is obtained by converting the cited page-number in accordance with the number to string
*        conversion properties specified on the ancestor fo:page-sequence of the referenced formatting object.
*
*        The child areas of the generated inline-area are the same as the result of formatting a result-tree fragment consisting of
*        fo:character flow objects; one for each character in the cited page-number string and with only the "character" property
*        specified.
*
*        Contents:
*
*            EMPTY
*
*        The following properties apply to this formatting object:
*
*             [7.3 Common Accessibility Properties]
*           [7.5 Common Aural Properties]
*             [7.6 Common Border, Padding, and Background Properties]
*             [7.7 Common Font Properties]
*             [7.10 Common Margin Properties-Inline]
*             [7.11.1 "alignment-adjust"]
*             [7.11.2 "baseline-identifier"]
*             [7.11.3 "baseline-shift"]
*             [7.11.5 "dominant-baseline"]
*             [7.36.2 "id"]
*             [7.17.4 "keep-with-next"]
*             [7.17.5 "keep-with-previous"]
*             [7.14.2 "letter-spacing"]
*             [7.13.4 "line-height"]
*             [7.13.5 "line-height-shift-adjustment"]
*             [7.36.5 "ref-id"]
*             [7.18.4 "relative-position"]
*             [7.36.6 "score-spaces"]
*             [7.14.4 "text-decoration"]
*             [7.14.5 "text-shadow"]
*             [7.14.6 "text-transform"]
*             [7.14.8 "word-spacing"]
*/
public class PageNumberCitation extends FObj {

		public static class Maker extends FObj.Maker {
				public FObj make(FObj parent,
												 PropertyList propertyList) throws FOPException {
						return new PageNumberCitation(parent, propertyList);
				}
		}

		public static FObj.Maker maker() {
				return new PageNumberCitation.Maker();
		}

		FontState fs;
		float red;
		float green;
		float blue;
		int wrapOption;
		int whiteSpaceCollapse;
		Area area;
		String pageNumber;
		String refId;
		String id;
  TextState ts;


		public PageNumberCitation(FObj parent, PropertyList propertyList) {
				super(parent, propertyList);
				this.name = "fo:page-number-citation";
		}


		public Status layout(Area area) throws FOPException {
				if (!(area instanceof BlockArea)) {
						MessageHandler.errorln("WARNING: page-number-citation outside block area");
						return new Status(Status.OK);
				}

				IDReferences idReferences = area.getIDReferences();
				this.area = area;
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

						ColorType c = this.properties.get("color").getColorType();
						this.red = c.red();
						this.green = c.green();
						this.blue = c.blue();

						this.wrapOption = this.properties.get("wrap-option").getEnum();
						this.whiteSpaceCollapse =
							this.properties.get("white-space-collapse").getEnum();

						this.refId = this.properties.get("ref-id").getString();

						if (this.refId.equals("")) {
								throw new FOPException("page-number-citation must contain \"ref-id\"");
						}

						// create id
						this.id = this.properties.get("id").getString();
						idReferences.createID(id);
						ts = new TextState();

						this.marker = 0;
				}

				if (marker == 0) {
						idReferences.configureID(id, area);
				}


				pageNumber = idReferences.getPageNumber(refId);

				if (pageNumber != null) { // if we already know the page number
						this.marker = FOText.addText((BlockArea) area, fs, red, green, blue,
													wrapOption, null, whiteSpaceCollapse,
													pageNumber.toCharArray(), 0, pageNumber.length(),
													ts, VerticalAlign.BASELINE);
				} else { // add pageNumberCitation to area to be resolved during rendering
            BlockArea blockArea = (BlockArea)area;
            LineArea la = blockArea.getCurrentLineArea();
            if(la == null) {
						return new Status(Status.AREA_FULL_NONE);
            }
            la.changeFont(fs);
            la.changeColor(red, green, blue);
            la.changeWrapOption(wrapOption);
            la.changeWhiteSpaceCollapse(whiteSpaceCollapse);
/*            la.changeHyphenation(language, country, hyphenate,
									 hyphenationChar, hyphenationPushCharacterCount,
									 hyphenationRemainCharacterCount);*/

//            blockArea.setupLinkSet(null);
            la.addPageNumberCitation(refId, null);
            this.marker = -1;
				}


				if (this.marker == -1) {
						return new Status(Status.OK);
				} else {
						return new Status(Status.AREA_FULL_NONE);
				}

		}

}

