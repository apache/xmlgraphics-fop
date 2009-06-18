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
 * Created on 12/07/2004
 * $Id$
 */
package org.apache.fop.area;

import java.awt.geom.AffineTransform;

import org.apache.fop.datastructs.Node;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.flow.FoPageSequence;
import org.apache.fop.fo.properties.WritingMode;


/**
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class BlockReferenceArea extends BlockArea implements
        ReferenceArea {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";
    /**
     * @param pageSeq
     * @param generatedBy
     * @param parent
     * @param sync
     */
    public BlockReferenceArea(FoPageSequence pageSeq, FONode generatedBy,
            Node parent, Object sync) {
        super(pageSeq, generatedBy, parent, sync);
        // TODO Auto-generated constructor stub
    }
    /** The writing-mode of the parent of the generating FO.  This may
     * differ from the writing mode of the generating FO if this is a
     * <code>reference-area</code>. */
    protected int frameWritingMode;
    /** True if the <code>writing-mode</code> of the frames of this area is
     * horizontal.  May differ from contentIsHorizontal if this is a
     * <code>reference-area</code>. */
    protected boolean frameIsHorizontal = true;
    /** True if the the <code>writing-mode</code> of the frames of this area is
     * left-to-right.  May differ from contentIsHorizontal if this is a
     * <code>reference-area</code>. */
    protected boolean frameLeftToRight = true;
    /** The rotation trait for the framing rectangles of this area */
    protected int frameRotation;
    /** Rotation from content to frame.  One of 0, 90, 180, 270. */
    protected int rotateToFrame;
    /** Rotation from frame to content. One of 0, 90, 180, 270. */
    protected int rotateToContent;

    protected void setup() {
        try {
            frameWritingMode =
                ((FONode)generatedBy.getParent()).getWritingMode();
            frameIsHorizontal = WritingMode.isHorizontal(frameWritingMode);
            frameLeftToRight = WritingMode.isLeftToRight(frameWritingMode);
            frameRotation =
                ((FONode)generatedBy.getParent()).getRefOrientation();
        } catch (PropertyException e) {
            throw new RuntimeException(e.getMessage());
        }
        rotateToFrame = frameRotation - contentRotation;
        if (rotateToFrame == 0) {
            rotateToContent = 0;
        } else {
            if (rotateToFrame < 0) {
                rotateToContent = -rotateToFrame;
                rotateToFrame +=360;
            } else {
                rotateToContent = 360 - rotateToFrame;
            }
        }
        setupFrames();
    }

    public void setCoordTransformer(AffineTransform aftx) {
        // Do nothing.  The co-ordinate transformer is gnerated dynamically
        ;
    }

    /* (non-Javadoc)
     * @see org.apache.fop.area.ReferenceArea#getCoordTransformer()
     */
    public AffineTransform getCoordTransformer() {
        AffineTransform transform = null;
        if (rotateToFrame == 0) {
            return null;
        }
        ContentRectangle content = getContent();
        // In general, x and y will be zero.  They DO NOT represent the
        // offset from the frame(s) to the content.
        double x = content.getX();
        double y = content.getY();
        double width = content.getWidth();
        double height = content.getHeight();
        switch (rotateToFrame) {
        case 90:
            // Translate the rotation back to the origin point of the contents
            // Correct for the translation to the left of the origin point
            // of what was the height.  No need to correct for what was the
            // width, which has become the height.  Reverse any translation
            // performed for the x,y values.
            transform = AffineTransform.getTranslateInstance(x - height, y);
            // Rotate around 0,0 by +90 degrees
            transform.rotate(Math.PI * 0.5);
            // Translate to a 0,0 origin point for the contents (generally
            // unnecessary).
            transform.translate(-x, -y);
            return transform;
        case 180:
            // Translate the rotation back to the origin point of the contents
            // Correct for the translation above the origin point of what was
            // the height, and the translation to the left of the origin point
            // of what was the width.  Reverse any translation
            // performed for the x,y values.
            transform =
                AffineTransform.getTranslateInstance(x - width, y - height);
            // Rotate around 0,0 by +180 degrees
            transform.rotate(Math.PI);
            // Translate to a 0,0 origin point for the contents (generally
            // unnecessary).
            transform.translate(-x, -y);
            return transform;
        case 270:
            // Translate the rotation back to the origin point of the contents
            // Correct for the translation above the origin point
            // of what was the width.  No need to correct for what was the
            // height, which has become the width.  Reverse any translation
            // performed for the x,y values.
            transform = AffineTransform.getTranslateInstance(x, y - width);
            // Rotate around 0,0 by +90 degrees
            transform.rotate(Math.PI * 1.5);
            // Translate to a 0,0 origin point for the contents (generally
            // unnecessary).
            transform.translate(-x, -y);
            return transform;
        default:
            throw new RuntimeException(
                    "Invalid rotation specified: " + rotateToFrame);
        }
    }

}
