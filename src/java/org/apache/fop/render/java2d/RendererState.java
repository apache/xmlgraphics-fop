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

package org.apache.fop.render.java2d;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

import org.apache.fop.datatypes.ColorType;

/**
 * An interface for the classes which hold the state of the current graphics context.
 */
public interface RendererState {

    /**
     * Push the current state onto the stack.
     */
    public abstract void push();

    /**
     * Pop the state from the stack and restore the graphics context.
     * @return the restored state, null if the stack is empty.
     */
    public abstract Graphics2D pop();

    /**
     * Get the current stack level.
     *
     * @return the current stack level
     */
    public abstract int getStackLevel();

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf only used by the PDFRenderer, is set to null.
     * @return true if the new Color changes the current Color
     */
    public abstract boolean updateColor(ColorType col, boolean fill, StringBuffer pdf);

    /**
     * Set the current font name. Check if the font name will change and then
     * set the new name.
     *
     * @param name the new font name
     * @param size
     * @param pdf
     * @return true if the new Font changes the current Font
     */
    public abstract boolean updateFont(String name, int size, StringBuffer pdf);

    /**
     * Sets the current Stroke. The line width should be set with
     * updateLineWidth() before calling this method
     *
     * @param style the constant for the style of the line as an int
     * @return true if the new Stroke changes the current Stroke
     */
    public abstract boolean updateStroke(float width, int style);

    /**
     * Set the current paint. This checks if the paint will change and then sets
     * the current paint.
     *
     * @param p the new paint
     * @return true if the new paint changes the current paint
     */
    public abstract boolean updatePaint(Paint p);

    /**
     * Check if the clip will change the current state. A clip is assumed to be
     * used in a situation where it will add to any clip in the current or
     * parent states. A clip cannot be cleared, this can only be achieved by
     * going to a parent level with the correct clip. If the clip is different
     * then it may start a new state so that it can return to the previous clip.
     *
     * @param cl the clip shape to check
     * @return true if the clip will change the current clip.
     */
    // TODO test
    public abstract boolean checkClip(Shape cl);

    /**
     * Set the current clip. This either sets a new clip or sets the clip to the
     * intersect of the old clip and the new clip.
     *
     * @param cl the new clip in the current state
     */
    public abstract boolean updateClip(Shape cl);

    /**
     * Check the current transform. The transform for the current state is the
     * combination of all transforms in the current state. The parameter is
     * compared against this current transform.
     *
     * @param tf the transform to check against
     * @return true if the new transform is different from the current transform
     */
    public abstract boolean checkTransform(AffineTransform tf);

    /**
     * Overwrites the Transform in the Graphics2D context. Use <code>transform()</code> if you
     * wish to compose with the current Affinetransform instead.
     * @see java.awt.Graphics2D#setTransform(AffineTransform tf).
     * @param tf the transform to concatonate to the current level transform
     */
    public abstract void setTransform(AffineTransform tf);

    /**
     * Composes an AffineTransform object with the Transform in this Graphics2D
     * according to the rule last-specified-first-applied.
     * @see java.awt.Graphics2D#transform(AffineTransform tf).
     *
     * @param tf the transform to concatonate to the current level transform
     */
    public abstract void transform(AffineTransform tf);

    /**
     * Get the current transform. This gets the combination of all transforms in
     * the current state.
     *
     * @return the calculate combined transform for the current state
     */
    public abstract AffineTransform getTransform();

}
