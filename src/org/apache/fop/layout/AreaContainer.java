/* $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.layout;

// FOP
import org.apache.fop.render.Renderer;
import org.apache.fop.fo.properties.*;

// Java
import java.util.Vector;
import java.util.Enumeration;

public class AreaContainer extends Area {

    private int xPosition; // should be able to take value 'left' and 'right' too
    private int yPosition; // should be able to take value 'top' and 'bottom' too
    private int position;

    public AreaContainer(FontState fontState, int xPosition,
                         int yPosition, int allocationWidth, int maxHeight,
                         int position) {
        super(fontState, allocationWidth, maxHeight);
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.position = position;
	//setIsReferenceArea(true); // Should always be true!
    }

    public void render(Renderer renderer) {
        renderer.renderAreaContainer(this);
    }

    public int getPosition() {
        return position;
    }

    public int getXPosition() {
        //return xPosition + getPaddingLeft() + getBorderLeftWidth();
        return xPosition ;
    }

    public void setXPosition(int value) {
        xPosition = value;
    }

    public int getYPosition() {
        //return yPosition + getPaddingTop() + getBorderTopWidth();
        return yPosition ;
    }

    public int getCurrentYPosition() {
        return yPosition;
    }

    public void setYPosition(int value) {
        yPosition = value;
    }

    public void shiftYPosition(int value) {
        yPosition += value;
    }
}
