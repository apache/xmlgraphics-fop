/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.fo.flow;

// FOP
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.Status;
import org.apache.fop.layout.Area;
import org.apache.fop.layout.Page;
import org.apache.fop.layout.BlockArea;
import org.apache.fop.layout.BodyAreaContainer;
import org.apache.fop.layout.AreaContainer;
import org.apache.fop.apps.FOPException;

// Java
import java.util.ArrayList;

public class Footnote extends FObj {

    public static class Maker extends FObj.Maker {
        public FObj make(FObj parent, PropertyList propertyList,
                         String systemId, int line, int column)
            throws FOPException {
            return new Footnote(parent, propertyList, systemId, line, column);
        }

    }

    public static FObj.Maker maker() {
        return new Footnote.Maker();
    }

    public Footnote(FObj parent, PropertyList propertyList,
                    String systemId, int line, int column) throws FOPException {
        super(parent, propertyList, systemId, line, column);
    }

    public String getName() {
        return "fo:footnote";
    }

    public int layout(Area area) throws FOPException {
        FONode inline = null;
        FONode fbody = null;
        if (this.marker == START) {
            this.marker = 0;
        }
        int numChildren = this.children.size();
        for (int i = this.marker; i < numChildren; i++) {
            FONode fo = (FONode)children.get(i);
            if (fo instanceof Inline) {
                inline = fo;
                int status = fo.layout(area);
                if (Status.isIncomplete(status)) {
                    return status;
                }
            } else if (inline != null && fo instanceof FootnoteBody) {
                // add footnote to current page or next if it can't fit
                fbody = fo;
                if (area instanceof BlockArea) {
                    ((BlockArea)area).addFootnote((FootnoteBody)fbody);
                } else {
                    Page page = area.getPage();
                    layoutFootnote(page, (FootnoteBody)fbody, area);
                }
            }
        }
        if (fbody == null) {
            log.error("no footnote-body in footnote");
        }
        if (area instanceof BlockArea) {}
        return Status.OK;
    }

    public static boolean layoutFootnote(Page p, FootnoteBody fb, Area area) {
        try {
            BodyAreaContainer bac = p.getBody();
            AreaContainer footArea = bac.getFootnoteReferenceArea();
            footArea.setIDReferences(bac.getIDReferences());
            int basePos = footArea.getCurrentYPosition()
                          - footArea.getHeight();
            int oldHeight = footArea.getHeight();
            if (area != null) {
                footArea.setMaxHeight(area.getMaxHeight() - area.getHeight()
                                      + footArea.getHeight());
            } else {
                footArea.setMaxHeight(bac.getMaxHeight()
                                      + footArea.getHeight());
            }
            if (!footArea.hasChildren()) {
                StaticContent separator = bac.getPage().getPageSequence()
                    .getStaticContent("xsl-footnote-separator");
                if (separator!=null) {
                    footArea.setIDReferences(bac.getIDReferences());
                    separator.layout(footArea, null);
                    int diff = footArea.getHeight() - oldHeight;
                    if (area != null) {
                        area.setMaxHeight(area.getMaxHeight() - diff);
                    }
                    if (bac.getFootnoteState() == 0) {
                        Area ar = bac.getMainReferenceArea();
                        decreaseMaxHeight(ar, diff);
                        footArea.setYPosition(basePos + footArea.getHeight());
                    }
                     basePos = footArea.getCurrentYPosition()
                               - footArea.getHeight();
                     oldHeight = footArea.getHeight();
                }
            }
            int status = fb.layout(footArea);
            if (Status.isIncomplete(status)) {
                // add as a pending footnote
                return false;
            } else {
                if (area != null) {
                    area.setMaxHeight(area.getMaxHeight()
                                      - footArea.getHeight() + oldHeight);
                }
                // bac.setMaxHeight(bac.getMaxHeight() - footArea.getHeight() + oldHeight);
                if (bac.getFootnoteState() == 0) {
                    Area ar = bac.getMainReferenceArea();
                    decreaseMaxHeight(ar, footArea.getHeight() - oldHeight);
                    footArea.setYPosition(basePos + footArea.getHeight());
                }
            }
        } catch (FOPException fope) {
            return false;
        }
        return true;
    }

    protected static void decreaseMaxHeight(Area ar, int change) {
        ar.setMaxHeight(ar.getMaxHeight() - change);
        ArrayList children = ar.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Object obj = children.get(i);
            if (obj instanceof Area) {
                Area childArea = (Area)obj;
                decreaseMaxHeight(childArea, change);
            }
        }
    }

}
