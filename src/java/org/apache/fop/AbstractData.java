package org.apache.fop;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

/**
 * A base state data holding object
 */
public abstract class AbstractData implements Cloneable, Serializable {

    private static final long serialVersionUID = 5208418041189828624L;

    /** The current color */
    protected Color color = null;

    /** The current background color */
    protected Color backColor = null;

    /** The current font name */
    protected String fontName = null;

    /** The current font size */
    protected int fontSize = 0;

    /** The current line width */
    protected float lineWidth = 0;

    /** The dash array for the current basic stroke (line type) */
    protected float[] dashArray = null;

    /** The current transform */
    protected AffineTransform transform = null;

    /**
     * Returns a newly create data object
     *
     * @return a new data object
     */
    protected abstract AbstractData instantiate();

    /**
     * Concatenate the given AffineTransform with the current thus creating
     * a new viewport. Note that all concatenation operations are logged
     * so they can be replayed if necessary (ex. for block-containers with
     * "fixed" positioning.
     *
     * @param at Transformation to perform
     */
    public void concatenate(AffineTransform at) {
        getTransform().concatenate(at);
    }

    /**
     * Get the current AffineTransform.
     *
     * @return the current transform
     */
    public AffineTransform getTransform() {
        if (transform == null) {
            transform = new AffineTransform();
        }
        return transform;
    }

    /**
     * Sets the current AffineTransform.
     */
    public void setTransform(AffineTransform baseTransform) {
        this.transform = baseTransform;
    }

    /**
     * Resets the current AffineTransform.
     */
    public void clearTransform() {
        transform = new AffineTransform();
    }

    /**
     * Returns the derived rotation from the current transform
     *
     * @return the derived rotation from the current transform
     */
    public int getDerivedRotation() {
        AffineTransform at = getTransform();
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        double shx = at.getShearX();
        double shy = at.getShearY();
        int rotation = 0;
        if (sx == 0 && sy == 0 && shx > 0 && shy < 0) {
            rotation = 270;
        } else if (sx < 0 && sy < 0 && shx == 0 && shy == 0) {
            rotation = 180;
        } else if (sx == 0 && sy == 0 && shx < 0 && shy > 0) {
            rotation = 90;
        } else {
            rotation = 0;
        }
        return rotation;
    }

    /** {@inheritDoc} */
    public Object clone() {
        AbstractData data = instantiate();
        data.color = this.color;
        data.backColor = this.backColor;
        data.fontName = this.fontName;
        data.fontSize = this.fontSize;
        data.lineWidth = this.lineWidth;
        data.dashArray = this.dashArray;
        data.transform = new AffineTransform(this.transform);
        return data;
    }

    /** {@inheritDoc} */
    public String toString() {
        return "color=" + color
            + ", backColor=" + backColor
            + ", fontName=" + fontName
            + ", fontSize=" + fontSize
            + ", lineWidth=" + lineWidth
            + ", dashArray=" + dashArray
            + ", transform=" + transform;
    }

}