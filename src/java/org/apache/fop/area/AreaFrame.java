/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 14/06/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.apache.fop.area.Area.AreaGeometry;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.properties.WritingMode;


/**
 * <code>AreaFrame</code> is the basic type for the geometry of a rectangle
 * enclosing another rectangle, e.g., a padding rectangle. 
 * @author pbw
 * @version $Revision$ $Name$
 */
public class AreaFrame extends AreaGeometry {

    /** The framed rectangle */
    protected Rectangle2D contents = null;
    /** The offset from <code>this</code> origin to the origin of the framed
     * rectangle */
    protected Point2D contentOffset = null;

    /**
     * @param writingMode
     */
    public AreaFrame(Area area) {
        area.super(area.frameWritingMode);
        // contents and contentOffset remain null
    }

    /**
     * Contents and offset remain null
     */
    public AreaFrame(Area area,
            double ipOrigin, double bpOrigin, double ipDim, double bpDim) {
        area.super(area.frameWritingMode, ipOrigin, bpOrigin, ipDim, bpDim);
    }

    /**
     * Instantiates a frame with 0-width edges.
     * @param contents the contained rectangle
     */
    public AreaFrame(Area area, AreaGeometry contents) {
        area.super(area.frameWritingMode);
        setRect(contents);
        this.contents = contents;
        this.contentOffset = new Point2D.Double();
    }

    /**
     * Instantiates a new framing rectangle with the given origin point, the
     * given width and height, the given content rectangle, and the given
     * offset from the origin of the framing rectangle to the origin of the
     * content rectangle.
     * @param x x-value of the origin of the framing rectangle in user space
     * units
     * @param y y-value of the origin of the framing rectangle in user space
     * units
     * @param w width of the framing rectangle in user space units
     * @param h height of the framing rectangle in user space units
     * @param contents the framed rectangle
     * @param contentOffset the offset to the origin point of the framed
     * rectangle from the origin point of <code>this</code> framing rectangle.
     */
    public AreaFrame(Area area,
            double ipOrigin, double bpOrigin, double ipDim, double bpDim,
            AreaGeometry contents, Point2D contentOffset) {
        area.super(area.frameWritingMode, ipOrigin, bpOrigin, ipDim,  bpDim);
        this.contents = contents;
        this.contentOffset = contentOffset;
    }

    /**
     * Instantiates a new framing rectangle from the given rectangle, given
     * contents, and given offset from the origin of the framing rectangle to
     * the origin of the framed rectangle.  The dimensions and location of the
     * given rectangle are copied into the dimensions of the new framing
     * rectangle.
     * @param rect the framing rectangle
     * @param contents the framed rectangle
     * @param contentOffset offset from origin of the framing rectangle to the
     * origin of the framed rectangle
     */
    public AreaFrame(Area area,
            Rectangle2D rect, AreaGeometry contents,
            Point2D contentOffset) {
        area.super(area.frameWritingMode, rect);
        this.contents = contents;
        this.contentOffset = contentOffset;
    }

    public int getWritingMode() {
        return writingMode;
    }

    /**
     * Sets the contents rectangle.  The dimensions of <code>this</code> are
     * adjusted to the difference between the current framed contents and
     * the new framed contents.  The offset is not affected.
     * @param contents the new framed contents
     */
    public void setContents(Rectangle2D contents) {
        setRect(getX(), getY(),
                getWidth() + (contents.getWidth() - this.contents.getWidth()),
                getHeight() + (contents.getWidth() - this.contents.getWidth()));
        contents = this.contents;
    }

    public Rectangle2D getContents() {
        return contents;
    }

    /**
     * Sets the offset from the origin of <code>this</code> to the origin of
     * the framed contents rectangle.  The dimensions of the framed contents
     * are not affected, but the dimensions of <code>this</code> are changed
     * by the difference between the current offset and the new offset in the
     * X and Y axes.
     * @param offset the new offset to the framed rectangle
     */
    public void setContentOffset(Point2D offset) {
        setStart(offset.getX());
        setBefore(offset.getY());
        contentOffset = offset;
    }

    public Point2D getContentOffset() {
        return contentOffset;
    }

    /**
     * Sets the before edge width of the frame.  The <code>contents</code> size
     * is unaffected, but the size of the frame (<code>this</code>) will
     * change.  The height will vary by the difference between the previous and
     * new before edge.  The <code>contentOffset</code> will also change by the
     * same amount.  Note that the origin of this frame (<code>getX(),
     * getY()</code>) will not change.
     * @param before
     */
    public void setBefore(double before) {
        try {
            setAbsoluteEdgeWidth(
                    WritingMode.getCorrespondingAbsoluteEdge(
                    writingMode, WritingMode.BEFORE), before);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the start edge width of the frame.  The <code>contents</code> size
     * is unaffected, but the size of the frame (<code>this</code>) will
     * change.  The width will vary by the difference between the previous and
     * new start edge.  The <code>contentOffset</code> will also change by the
     * same amount.  Note that the origin of this frame (<code>getX(),
     * getY()</code>) will not change.
     * @param start
     */
    public void setStart(double start) {
        try {
            setAbsoluteEdgeWidth(
                    WritingMode.getCorrespondingAbsoluteEdge(
                    writingMode, WritingMode.START), start);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the after edge width of the frame.  The <code>contents</code> size
     * and the <code>contentOffset</code> are unaffected, but the size of the
     * frame (<code>this</code>) will change.  The height will vary by the
     * difference between the previous and new after edge.  Note that the
     * origin of this frame (<code>getX(), getY()</code>) will not change.
     * @param after
     */
    public void setAfter(double after) {
        try {
            setAbsoluteEdgeWidth(
                    WritingMode.getCorrespondingAbsoluteEdge(
                    writingMode, WritingMode.AFTER), after);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the end edge width of the frame.  The <code>contents</code> size
     * and the <code>contentOffset</code> are unaffected, but the size of the
     * frame (<code>this</code>) will change.  The width will vary by the
     * difference between the previous and new end edge.  Note that the
     * origin of this frame (<code>getX(), getY()</code>) will not change.
     * @param end
     */
    public void setEnd(double end) {
        try {
            setAbsoluteEdgeWidth(
                    WritingMode.getCorrespondingAbsoluteEdge(
                    writingMode, WritingMode.END), end);
        } catch (PropertyException e) {
            throw new RuntimeException(e);
        }
    }

    public void setAbsoluteEdgeWidth(int edge, double width) {
        switch (edge) {
        case WritingMode.TOP:
            setTop(width);
            break;
        case WritingMode.BOTTOM:
            setBottom(width);
            break;
        case WritingMode.LEFT:
            setLeft(width);
            break;
        case WritingMode.RIGHT:
            setRight(width);
            break;
        default:
            throw new RuntimeException(
                    "Invalid absolute writing mode: " + edge);
        }
    }

    /**
     * Sets the top edge width of the frame.  The <code>contents</code> size
     * is unaffected, but the size of the frame (<code>this</code>) will
     * change.  The height will vary by the difference between the previous and
     * new top edge.  The <code>contentOffset</code> will also change by the
     * same amount.  Note that the origin of this frame (<code>getX(),
     * getY()</code>) will not change.
     * @param top
     */
    public void setTop(double top) {
        double diff = top - contentOffset.getY();
        setRect(getX(), getY(),
                getWidth(), getHeight() + diff);
        contentOffset.setLocation(contentOffset.getX(), top);
    }

    /**
     * Sets the left edge width of the frame.  The <code>contents</code> size
     * is unaffected, but the size of the frame (<code>this</code>) will
     * change.  The width will vary by the difference between the previous and
     * new left edge.  The <code>contentOffset</code> will also change by the
     * same amount.  Note that the origin of this frame (<code>getX(),
     * getY()</code>) will not change.
     * @param left
     */
    public void setLeft(double left) {
        double diff = left - contentOffset.getY();
        setRect(getX(), getY(),
                getWidth() + diff, getHeight());
        contentOffset.setLocation(left, contentOffset.getX());
    }

    /**
     * Sets the bottom edge width of the frame.  The <code>contents</code> size
     * and the <code>contentOffset</code> are unaffected, but the size of the
     * frame (<code>this</code>) will change.  The height will vary by the
     * difference between the previous and new bottom edge.  Note that the
     * origin of this frame (<code>getX(), getY()</code>) will not change.
     * @param bottom
     */
    public void setBottom(double bottom) {
        double diff = bottom - (getY() - contentOffset.getY() - contents.getY());
        setRect(getX(), getY(), getWidth(), getHeight() + diff);
    }

    /**
     * Sets the right edge width of the frame.  The <code>contents</code> size
     * and the <code>contentOffset</code> are unaffected, but the size of the
     * frame (<code>this</code>) will change.  The width will vary by the
     * difference between the previous and new right edge.  Note that the
     * origin of this frame (<code>getX(), getY()</code>) will not change.
     * @param right
     */
    public void setRight(double right) {
        double diff = right - (getX() - contentOffset.getX() - contents.getX());
        setRect(getX(), getY(), getWidth() + diff, getHeight());
    }

    public double getAbsoluteEdgeWidth(int edge) {
        switch (edge) {
        case WritingMode.TOP:
            return getTop();
        case WritingMode.BOTTOM:
            return getBottom();
        case WritingMode.LEFT:
            return getLeft();
        case WritingMode.RIGHT:
            return getRight();
        default:
            throw new RuntimeException(
                    "Invalid absolute writing mode: " + edge);
        }
    }

    public double getTop() {
        return contentOffset.getY();
    }

    public double getLeft() {
        return contentOffset.getX();
    }

    public double getBottom() {
        return getHeight() - contentOffset.getY() - contents.getHeight(); 
    }

    public double getRight() {
        return getWidth() - contentOffset.getX() - contents.getWidth();
    }

}
