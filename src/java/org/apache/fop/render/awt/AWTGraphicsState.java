/*
 * Copyright 2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.awt;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.List;

import org.apache.fop.datatypes.ColorType;
import org.apache.fop.fo.Constants;
import org.apache.fop.fonts.FontInfo;

/**
 * Keeps information about the current state of the Graphics2D currentGraphics.
 * It is also used as a stack to hold a graphics context.
 * <p>
 * The graphics context is updated with the updateXXX() methods.
 */
public class AWTGraphicsState implements Constants, RendererState {

    /** Holds the datas of the current state */
    private Graphics2D currentGraphics;

    private BasicStroke currentStroke;

    private float currentStrokeWidth;

    private int currentStrokeStyle;

    private List stateStack = new java.util.ArrayList();

    /** Font configuration, passed from AWTRenderer */
    private FontInfo fontInfo;

    /** State for storing graphics state. */
    public AWTGraphicsState(Graphics2D graphics, FontInfo fontInfo) {
        this.fontInfo = fontInfo;
        this.currentGraphics = graphics;
    }

    /**
     * @return the currently valid state
     */
    public Graphics2D getGraph() {
        return currentGraphics;
    }

    /** @see org.apache.fop.render.awt.RendererState#push() */
    public void push() {
        Graphics2D tmpGraphics = (Graphics2D) currentGraphics.create();
        stateStack.add(tmpGraphics);
    }

    /** @see org.apache.fop.render.awt.RendererState#pop() */
    public Graphics2D pop() {
        if (getStackLevel() > 0) {
            Graphics2D popped = (Graphics2D) stateStack.remove(stateStack
                    .size() - 1);

            currentGraphics = popped;
            return popped;
        } else {
            return null;
        }
    }

    /** @see org.apache.fop.render.awt.RendererState#getStackLevel() */
    public int getStackLevel() {
        return stateStack.size();
    }

    /**
     * Restore the state to a particular level. this can be used to restore to a
     * known level without making multiple pop calls.
     *
     * @param stack the level to restore to
     */
    /*
     * public void restoreLevel(int stack) { int pos = stack; while
     * (stateStack.size() > pos + 1) { stateStack.remove(stateStack.size() - 1); }
     * if (stateStack.size() > pos) { pop(); } }
     */

    /**
     * Set the current background color. Check if the background color will
     * change and then set the new color.
     *
     * @param col the new color as a java.awt.Color
     * @return true if the background color has changed
     */
    public boolean updateColor(Color col) {
        if (!col.equals(getGraph().getColor())) {
            getGraph().setColor(col);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Converts a ColorType to a java.awt.Color (sRGB).
     *
     * @param col the color as a org.apache.fop.datatypes.ColorType
     * @return the converted color as a java.awt.Color
     */
    public Color toColor(ColorType col) {
        return new Color(col.getRed(), col.getGreen(), col.getBlue());
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#updateColor(org.apache.fop.datatypes.ColorType,
     * boolean, java.lang.StringBuffer)
     */
    public boolean updateColor(ColorType col, boolean fill, StringBuffer pdf) {
        if (col == null) {
            return false;
        }
        Color newCol = toColor(col);
        return updateColor(newCol);
    }

    /**
     * Update the current Color
     * @param col the ColorType
     */
    public void updateColor(ColorType col) {
        if (col == null) {
            return;
        }
        Color newCol = toColor(col);
        updateColor(newCol);
    }

    /**
     * @return the current java.awt.Color
     */
    public java.awt.Color getColor() {
        return currentGraphics.getColor();
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#updateFont(java.lang.String,
     * int, java.lang.StringBuffer)
     */
    public boolean updateFont(String name, int size, StringBuffer pdf) {

        boolean updateName = (!name.equals(getGraph().getFont().getFontName()));
        boolean updateSize = (size != (getGraph().getFont().getSize()));

        if (updateName || updateSize) {
            // the font name and/or the font size have changed
            FontMetricsMapper mapper = (FontMetricsMapper) fontInfo
                    .getMetricsFor(name);
            java.awt.Font font = mapper.getFont(size);

            currentGraphics.setFont(font);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the current java.awt.Font
     */
    public java.awt.Font getFont() {
        return currentGraphics.getFont();
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#updateStroke(float, int)
     */
    public boolean updateStroke(float width, int style) {

        boolean update = false;

        // only update if necessary
        if ((width != currentStrokeWidth) || (style != currentStrokeStyle)) {

            update = true;

            switch (style) {
            case EN_DOTTED:

                currentStroke = new BasicStroke(width, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 0f, new float[] { 2f }, 0f);
                currentGraphics.setStroke(currentStroke);

                currentStrokeWidth = width;
                currentStrokeStyle = style;

                break;

            case EN_DASHED:

                currentStroke = new BasicStroke(width, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_BEVEL, 0f, new float[] { 8f, 2f }, 0f);
                currentGraphics.setStroke(currentStroke);

                currentStrokeWidth = width;
                currentStrokeStyle = style;

                break;

            default: // EN_SOLID:

                currentStroke = new BasicStroke(width);
                currentGraphics.setStroke(currentStroke);

                currentStrokeWidth = width;
                currentStrokeStyle = style;

                break;
            }
        }

        return update;
    }

    public BasicStroke getStroke() {
        return (BasicStroke) currentGraphics.getStroke();
    }

    /** @see org.apache.fop.render.awt.RendererState#updatePaint(java.awt.Paint) */
    public boolean updatePaint(Paint p) {
        if (getGraph().getPaint() == null) {
            if (p != null) {
                getGraph().setPaint(p);
                return true;
            }
        } else if (p.equals(getGraph().getPaint())) {
            getGraph().setPaint(p);
            return true;
        }
        return false;
    }

    /** @see org.apache.fop.render.awt.RendererState#checkClip(java.awt.Shape) */
    // TODO implement and test
    public boolean checkClip(Shape cl) {
        if (getGraph().getClip() == null) {
            if (cl != null) {
                return true;
            }
        } else if (cl.equals(getGraph().getClip())) {
            return true;
        }
        // TODO check for clips that are larger than the current
        return false;
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#updateClip(java.awt.Shape)
     */
    public boolean updateClip(Shape cl) {
        if (getGraph().getClip() != null) {
            Area newClip = new Area(getGraph().getClip());
            newClip.intersect(new Area(cl));
            getGraph().setClip(new GeneralPath(newClip));
        } else {
            getGraph().setClip(cl);
        }
        return true; // TODO only update if necessary
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#checkTransform(java.awt.geom.AffineTransform)
     */
    public boolean checkTransform(AffineTransform tf) {
        return !tf.equals(getGraph().getTransform());
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#setTransform(java.awt.geom.AffineTransform)
     */
    public void setTransform(AffineTransform tf) {
        getGraph().setTransform(tf);
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform tf) {
        getGraph().transform(tf);
    }

    /**
     * @see org.apache.fop.render.awt.RendererState#getTransform()
     */
    public AffineTransform getTransform() {
        /*
         * AffineTransform tf; AffineTransform at = new AffineTransform(); for
         * (Iterator iter = stateStack.iterator(); iter.hasNext();) { Data d =
         * (Data) iter.next(); tf = d.transform; at.concatenate(tf); }
         * at.concatenate(getCurrentGraphics().transform);
         *
         * return at;
         */
        return getGraph().getTransform();
    }

    /** a verbose description of the current state */
    public String toString() {
        String s = "AWTGraphicsState " + currentGraphics.toString()
                + ", Stroke (width: " + currentStrokeWidth + " style: "
                + currentStrokeStyle + "), " + getTransform()
                + ", StackLevel: " + getStackLevel();
        return s;
    }
}
