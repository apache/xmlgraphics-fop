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
import org.apache.fop.layout.Area;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.properties.*;
import org.apache.fop.layout.*;

// Java
import java.util.Enumeration;

public class FootnoteBody extends FObj {
		FontState fs;
		int align;
		int alignLast;
		int lineHeight;
		int startIndent;
		int endIndent;
		int textIndent;

	public static class Maker extends FObj.Maker {
		public FObj make(FObj parent, PropertyList propertyList)
				throws FOPException {
			return new FootnoteBody(parent, propertyList);
		}
	}

	public static FObj.Maker maker() {
			return new FootnoteBody.Maker();
	}

	public FootnoteBody(FObj parent, PropertyList propertyList)
			throws FOPException {
		super(parent, propertyList);
		this.name = "fo:footnote-body";
	}

		public Status layout(Area area) throws FOPException {
				if(this.marker == START) {
						this.marker = 0;
				}
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

						FontState fs = new FontState(area.getFontInfo(), fontFamily,
																		fontStyle, fontWeight, fontSize, fontVariant);

				BlockArea blockArea = new BlockArea(fs, area.getAllocationWidth(),
																			 area.spaceLeft(), startIndent, endIndent, textIndent,
																			 align, alignLast, lineHeight);
				blockArea.setPage(area.getPage());
				blockArea.start();

				blockArea.setAbsoluteHeight(area.getAbsoluteHeight());
				blockArea.setIDReferences(area.getIDReferences());

				blockArea.setTableCellXOffset(area.getTableCellXOffset());

				int numChildren = this.children.size();
				for ( int i = this.marker; i < numChildren; i++ ) {
						FONode fo = (FONode) children.elementAt(i);
						Status status;
						if ( (status = fo.layout(blockArea)).isIncomplete() ) {
								this.resetMarker();
								return status;
						}
				}
				blockArea.end();
				area.addChild(blockArea);
				area.increaseHeight(blockArea.getHeight());
				return new Status(Status.OK);
		}
}
