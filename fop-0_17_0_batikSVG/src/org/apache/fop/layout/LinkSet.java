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

/* this class contributed by Arved Sandstrom with minor modifications
   by James Tauber */

package org.apache.fop.layout;

// Java
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Rectangle;

import org.apache.fop.fo.properties.WrapOption; // for enumerated
// values
// import org.apache.fop.fo.properties.WhiteSpaceCollapse; // for
// enumerated values
import org.apache.fop.fo.properties.TextAlign; // for enumerated
// values
import org.apache.fop.fo.properties.TextAlignLast; // for enumerated
// values

/**
 * a set of rectangles on a page that are linked to a common
 * destination
 */
public class LinkSet {

    /** the destination of the links */
    String destination;

    /** the set of rectangles */
    Vector rects = new Vector();

    private int xoffset = 0;
    private int yoffset = 0;

    /* the maximum Y offset value encountered for this LinkSet*/
    private int maxY = 0;

    protected int startIndent;
    protected int endIndent;

    private int linkType;

    private Area area;

    public final static int INTERNAL = 0, // represents internal link
    EXTERNAL = 1; // represents external link

    // property required for alignment adjustments
    int contentRectangleWidth = 0;

    public LinkSet(String destination, Area area, int linkType) {
        this.destination = destination;
        this.area = area;
        this.linkType = linkType;
    }

    public void addRect(Rectangle r, LineArea lineArea) {
        LinkedRectangle linkedRectangle = new LinkedRectangle(r, lineArea);
        linkedRectangle.setY(this.yoffset);
        if (this.yoffset > maxY) {
            maxY = this.yoffset;
        }
        rects.addElement(linkedRectangle);
    }

    public void setYOffset(int y) {
        this.yoffset = y;
    }

    public void setXOffset(int x) {
        this.xoffset = x;
    }

    public void setContentRectangleWidth(int contentRectangleWidth) {
        this.contentRectangleWidth = contentRectangleWidth;
    }

    public void applyAreaContainerOffsets(AreaContainer ac, Area area) {
        int height = area.getAbsoluteHeight();
        BlockArea ba = (BlockArea) area;
        Enumeration re = rects.elements();
        while (re.hasMoreElements()) {
            LinkedRectangle r = (LinkedRectangle) re.nextElement();
            r.setX(r.getX() + ac.getXPosition() +
                   area.getTableCellXOffset());
            r.setY(ac.getYPosition() - height + (maxY - r.getY()) -
                   ba.getHalfLeading());
        }
    }

    // intermediate implementation for joining all sublinks on same line
    public void mergeLinks() {
        int numRects = rects.size();
        if (numRects == 1)
            return;

        LinkedRectangle curRect =
          new LinkedRectangle((LinkedRectangle) rects.elementAt(0));
        Vector nv = new Vector();

        for (int ri = 1; ri < numRects; ri++) {
            LinkedRectangle r = (LinkedRectangle) rects.elementAt(ri);

            // yes, I'm really happy with comparing refs...
            if (r.getLineArea() == curRect.getLineArea()) {
                curRect.setWidth(r.getX() + r.getWidth() - curRect.getX());
            } else {
                nv.addElement(curRect);
                curRect = new LinkedRectangle(r);
            }

            if (ri == numRects - 1)
                nv.addElement(curRect);
        }

        rects = nv;
    }

    public void align() {
        Enumeration re = rects.elements();
        while (re.hasMoreElements()) {
            LinkedRectangle r = (LinkedRectangle) re.nextElement();
            r.setX(r.getX() + r.getLineArea().getStartIndent());
        }
    }

    public String getDest() {
        return this.destination;
    }

    public Vector getRects() {
        return this.rects;
    }

    public int getEndIndent() {
        return endIndent;
    }

    public int getStartIndent() {
        return startIndent;
    }

    public Area getArea() {
        return area;
    }

    public int getLinkType() {
        return linkType;
    }
}
