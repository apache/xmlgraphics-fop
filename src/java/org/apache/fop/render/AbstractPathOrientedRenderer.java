/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.fop.render;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import org.apache.batik.parser.AWTTransformProducer;

import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.util.QName;

import org.apache.fop.area.Area;
import org.apache.fop.area.Block;
import org.apache.fop.area.BlockViewport;
import org.apache.fop.area.CTM;
import org.apache.fop.area.NormalFlow;
import org.apache.fop.area.RegionReference;
import org.apache.fop.area.RegionViewport;
import org.apache.fop.area.Trait;
import org.apache.fop.area.inline.ForeignObject;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.Viewport;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.extensions.ExtensionElementMapping;
import org.apache.fop.fonts.FontMetrics;
import org.apache.fop.traits.BorderProps;

/**
 * Abstract base class for renderers like PDF and PostScript where many painting operations
 * follow similar patterns which makes it possible to share some code.
 */
public abstract class AbstractPathOrientedRenderer extends PrintRenderer {

    /**
     * Handle block traits.
     * The block could be any sort of block with any positioning
     * so this should render the traits such as border and background
     * in its position.
     *
     * @param block the block to render the traits
     */
    protected void handleBlockTraits(Block block) {
        int borderPaddingStart = block.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = block.getBorderAndPaddingWidthBefore();
        
        float startx = currentIPPosition / 1000f;
        float starty = currentBPPosition / 1000f;
        float width = block.getIPD() / 1000f;
        float height = block.getBPD() / 1000f;

        /* using start-indent now
        Integer spaceStart = (Integer) block.getTrait(Trait.SPACE_START);
        if (spaceStart != null) {
            startx += spaceStart.floatValue() / 1000f;
        }*/
        startx += block.getStartIndent() / 1000f;
        startx -= block.getBorderAndPaddingWidthStart() / 1000f;

        width += borderPaddingStart / 1000f;
        width += block.getBorderAndPaddingWidthEnd() / 1000f;
        height += borderPaddingBefore / 1000f;
        height += block.getBorderAndPaddingWidthAfter() / 1000f;

        drawBackAndBorders(block, startx, starty,
            width, height);
    }

    /**
     * Handle the traits for a region
     * This is used to draw the traits for the given page region.
     * (See Sect. 6.4.1.2 of XSL-FO spec.)
     * @param region the RegionViewport whose region is to be drawn
     */
    protected void handleRegionTraits(RegionViewport region) {
        Rectangle2D viewArea = region.getViewArea();
        RegionReference referenceArea = region.getRegionReference();
        float startx = (float)(viewArea.getX() / 1000f);
        float starty = (float)(viewArea.getY() / 1000f);
        float width = (float)(viewArea.getWidth() / 1000f);
        float height = (float)(viewArea.getHeight() / 1000f);

        // adjust the current position according to region borders and padding
        currentBPPosition = referenceArea.getBorderAndPaddingWidthBefore();
        currentIPPosition = referenceArea.getBorderAndPaddingWidthStart();
        // draw background (traits are in the RegionViewport)
        // and borders (traits are in the RegionReference)
        drawBackAndBorders(region, referenceArea, startx, starty, width, height);
    }

    /**
     * Draw the background and borders.
     * This draws the background and border traits for an area given
     * the position.
     *
     * @param area the area to get the traits from
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area area,
                    float startx, float starty,
                    float width, float height) {
        drawBackAndBorders(area, area, startx, starty, width, height);
    }

    /**
     * Draw the background and borders.
     * This draws the background and border traits for an area given
     * the position.
     *
     * @param backgroundArea the area to get the background traits from
     * @param borderArea the area to get the border traits from
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     */
    protected void drawBackAndBorders(Area backgroundArea, Area borderArea,
                    float startx, float starty,
                    float width, float height) {
        // draw background then border

        BorderProps bpsBefore = (BorderProps)borderArea.getTrait(Trait.BORDER_BEFORE);
        BorderProps bpsAfter = (BorderProps)borderArea.getTrait(Trait.BORDER_AFTER);
        BorderProps bpsStart = (BorderProps)borderArea.getTrait(Trait.BORDER_START);
        BorderProps bpsEnd = (BorderProps)borderArea.getTrait(Trait.BORDER_END);

        drawBackground(startx, starty, width, height,
                (Trait.Background) backgroundArea.getTrait(Trait.BACKGROUND), 
                bpsBefore, bpsAfter, bpsStart, bpsEnd);
        drawBorders(startx, starty, width, height,
                bpsBefore, bpsAfter, bpsStart, bpsEnd);        
    }

    /**
     * Draw the background.
     * This draws the background given the position and the traits.
     *
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     * @param back the background traits
     * @param bpsBefore the border-before traits
     * @param bpsAfter the border-after traits
     * @param bpsStart the border-start traits
     * @param bpsEnd the border-end traits
     */
    protected void drawBackground(float startx, float starty,
                    float width, float height,
                    Trait.Background back,
                    BorderProps bpsBefore, BorderProps bpsAfter,
                    BorderProps bpsStart, BorderProps bpsEnd) {
        if (back != null) {
            endTextObject();

            //Calculate padding rectangle
            float sx = startx;
            float sy = starty;
            float paddRectWidth = width;
            float paddRectHeight = height;
            if (bpsStart != null) {
                sx += bpsStart.width / 1000f;
                paddRectWidth -= bpsStart.width / 1000f;
            }
            if (bpsBefore != null) {
                sy += bpsBefore.width / 1000f;
                paddRectHeight -= bpsBefore.width / 1000f;
            }
            if (bpsEnd != null) {
                paddRectWidth -= bpsEnd.width / 1000f;
            }
            if (bpsAfter != null) {
                paddRectHeight -= bpsAfter.width / 1000f;
            }

            if (back.getColor() != null) {
                updateColor(back.getColor(), true);
                fillRect(sx, sy, paddRectWidth, paddRectHeight);
            }
            if (back.getImageInfo() != null) {
                ImageSize imageSize = back.getImageInfo().getSize(); 
                saveGraphicsState();
                clipRect(sx, sy, paddRectWidth, paddRectHeight);
                int horzCount = (int)((paddRectWidth 
                        * 1000 / imageSize.getWidthMpt()) + 1.0f); 
                int vertCount = (int)((paddRectHeight 
                        * 1000 / imageSize.getHeightMpt()) + 1.0f); 
                if (back.getRepeat() == EN_NOREPEAT) {
                    horzCount = 1;
                    vertCount = 1;
                } else if (back.getRepeat() == EN_REPEATX) {
                    vertCount = 1;
                } else if (back.getRepeat() == EN_REPEATY) {
                    horzCount = 1;
                }
                //change from points to millipoints
                sx *= 1000;
                sy *= 1000;
                if (horzCount == 1) {
                    sx += back.getHoriz();
                }
                if (vertCount == 1) {
                    sy += back.getVertical();
                }
                for (int x = 0; x < horzCount; x++) {
                    for (int y = 0; y < vertCount; y++) {
                        // place once
                        Rectangle2D pos;
                        // Image positions are relative to the currentIP/BP
                        pos = new Rectangle2D.Float(sx - currentIPPosition 
                                                        + (x * imageSize.getWidthMpt()),
                                                    sy - currentBPPosition
                                                        + (y * imageSize.getHeightMpt()),
                                                        imageSize.getWidthMpt(),
                                                        imageSize.getHeightMpt());
                        drawImage(back.getURL(), pos);
                    }
                }
                
                restoreGraphicsState();
            }
        }
    }

    /**
     * Draw the borders.
     * This draws the border traits given the position and the traits.
     *
     * @param startx the start x position
     * @param starty the start y position
     * @param width the width of the area
     * @param height the height of the area
     * @param bpsBefore the border-before traits
     * @param bpsAfter the border-after traits
     * @param bpsStart the border-start traits
     * @param bpsEnd the border-end traits
     */
    protected void drawBorders(float startx, float starty,
                    float width, float height,
                    BorderProps bpsBefore, BorderProps bpsAfter,
                    BorderProps bpsStart, BorderProps bpsEnd) {
        Rectangle2D.Float borderRect = new Rectangle2D.Float(startx, starty, width, height);
        drawBorders(borderRect, bpsBefore, bpsAfter, bpsStart, bpsEnd);
    }

    private static final int BEFORE = 0;
    private static final int END = 1;
    private static final int AFTER = 2;
    private static final int START = 3;

    /**
     * Draws borders.
     * @param borderRect the border rectangle
     * @param bpsBefore the border specification on the before side
     * @param bpsAfter the border specification on the after side
     * @param bpsStart the border specification on the start side
     * @param bpsEnd the border specification on the end side
     */    
    protected void drawBorders(Rectangle2D.Float borderRect, 
            BorderProps bpsBefore, BorderProps bpsAfter, BorderProps bpsStart, BorderProps bpsEnd) {
        //TODO generalize each of the four conditions into using a parameterized drawBorder()
        boolean[] border = new boolean[] {
                (bpsBefore != null), (bpsEnd != null), 
                (bpsAfter != null), (bpsStart != null)};
        float startx = borderRect.x;
        float starty = borderRect.y;
        float width = borderRect.width;
        float height = borderRect.height;
        float[] borderWidth = new float[] {
            (border[BEFORE] ? bpsBefore.width / 1000f : 0.0f),
            (border[END] ? bpsEnd.width / 1000f : 0.0f),
            (border[AFTER] ? bpsAfter.width / 1000f : 0.0f),
            (border[START] ? bpsStart.width / 1000f : 0.0f)};
        float[] clipw = new float[] {
            BorderProps.getClippedWidth(bpsBefore) / 1000f,    
            BorderProps.getClippedWidth(bpsEnd) / 1000f,    
            BorderProps.getClippedWidth(bpsAfter) / 1000f,    
            BorderProps.getClippedWidth(bpsStart) / 1000f};
        starty += clipw[BEFORE];
        height -= clipw[BEFORE];
        height -= clipw[AFTER];
        startx += clipw[START];
        width -= clipw[START];
        width -= clipw[END];
        
        boolean[] slant = new boolean[] {
            (border[START] && border[BEFORE]),
            (border[BEFORE] && border[END]),
            (border[END] && border[AFTER]),
            (border[AFTER] && border[START])};
        if (bpsBefore != null) {
            endTextObject();

            float sx1 = startx;
            float sx2 = (slant[BEFORE] ? sx1 + borderWidth[START] - clipw[START] : sx1);
            float ex1 = startx + width;
            float ex2 = (slant[END] ? ex1 - borderWidth[END] + clipw[END] : ex1);
            float outery = starty - clipw[BEFORE];
            float clipy = outery + clipw[BEFORE];
            float innery = outery + borderWidth[BEFORE];

            saveGraphicsState();
            moveTo(sx1, clipy);
            float sx1a = sx1;
            float ex1a = ex1;
            if (bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[START];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[END];
                }
                lineTo(sx1a, outery);
                lineTo(ex1a, outery);
            }
            lineTo(ex1, clipy);
            lineTo(ex2, innery);
            lineTo(sx2, innery);
            closePath();
            clip();
            drawBorderLine(sx1a, outery, ex1a, innery, true, true, 
                    bpsBefore.style, bpsBefore.color);
            restoreGraphicsState();
        }
        if (bpsEnd != null) {
            endTextObject();

            float sy1 = starty;
            float sy2 = (slant[END] ? sy1 + borderWidth[BEFORE] - clipw[BEFORE] : sy1);
            float ey1 = starty + height;
            float ey2 = (slant[AFTER] ? ey1 - borderWidth[AFTER] + clipw[AFTER] : ey1);
            float outerx = startx + width + clipw[END];
            float clipx = outerx - clipw[END];
            float innerx = outerx - borderWidth[END];
            
            saveGraphicsState();
            moveTo(clipx, sy1);
            float sy1a = sy1;
            float ey1a = ey1;
            if (bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[BEFORE];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[AFTER];
                }
                lineTo(outerx, sy1a);
                lineTo(outerx, ey1a);
            }
            lineTo(clipx, ey1);
            lineTo(innerx, ey2);
            lineTo(innerx, sy2);
            closePath();
            clip();
            drawBorderLine(innerx, sy1a, outerx, ey1a, false, false, bpsEnd.style, bpsEnd.color);
            restoreGraphicsState();
        }
        if (bpsAfter != null) {
            endTextObject();

            float sx1 = startx;
            float sx2 = (slant[START] ? sx1 + borderWidth[AFTER] - clipw[AFTER] : sx1);
            float ex1 = startx + width;
            float ex2 = (slant[AFTER] ? ex1 - borderWidth[END] + clipw[END] : ex1);
            float outery = starty + height + clipw[AFTER];
            float clipy = outery - clipw[AFTER];
            float innery = outery - borderWidth[AFTER];

            saveGraphicsState();
            moveTo(ex1, clipy);
            float sx1a = sx1;
            float ex1a = ex1;
            if (bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsStart != null && bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                    sx1a -= clipw[START];
                }
                if (bpsEnd != null && bpsEnd.mode == BorderProps.COLLAPSE_OUTER) {
                    ex1a += clipw[END];
                }
                lineTo(ex1a, outery);
                lineTo(sx1a, outery);
            }
            lineTo(sx1, clipy);
            lineTo(sx2, innery);
            lineTo(ex2, innery);
            closePath();
            clip();
            drawBorderLine(sx1a, innery, ex1a, outery, true, false, bpsAfter.style, bpsAfter.color);
            restoreGraphicsState();
        }
        if (bpsStart != null) {
            endTextObject();

            float sy1 = starty;
            float sy2 = (slant[BEFORE] ? sy1 + borderWidth[BEFORE] - clipw[BEFORE] : sy1);
            float ey1 = sy1 + height;
            float ey2 = (slant[START] ? ey1 - borderWidth[AFTER] + clipw[AFTER] : ey1);
            float outerx = startx - clipw[START];
            float clipx = outerx + clipw[START];
            float innerx = outerx + borderWidth[START];

            saveGraphicsState();
            moveTo(clipx, ey1);
            float sy1a = sy1;
            float ey1a = ey1;
            if (bpsStart.mode == BorderProps.COLLAPSE_OUTER) {
                if (bpsBefore != null && bpsBefore.mode == BorderProps.COLLAPSE_OUTER) {
                    sy1a -= clipw[BEFORE];
                }
                if (bpsAfter != null && bpsAfter.mode == BorderProps.COLLAPSE_OUTER) {
                    ey1a += clipw[AFTER];
                }
                lineTo(outerx, ey1a);
                lineTo(outerx, sy1a);
            }
            lineTo(clipx, sy1);
            lineTo(innerx, sy2);
            lineTo(innerx, ey2);
            closePath();
            clip();
            drawBorderLine(outerx, sy1a, innerx, ey1a, false, true, bpsStart.style, bpsStart.color);
            restoreGraphicsState();
        }
    }
    
    /** 
     * Common method to render the background and borders for any inline area.
     * The all borders and padding are drawn outside the specified area.
     * @param area the inline area for which the background, border and padding is to be
     * rendered
     */
    protected void renderInlineAreaBackAndBorders(InlineArea area) {
        float borderPaddingStart = area.getBorderAndPaddingWidthStart() / 1000f;
        float borderPaddingBefore = area.getBorderAndPaddingWidthBefore() / 1000f;
        float bpwidth = borderPaddingStart 
                + (area.getBorderAndPaddingWidthEnd() / 1000f);
        float bpheight = borderPaddingBefore
                + (area.getBorderAndPaddingWidthAfter() / 1000f);
        
        float height = area.getBPD() / 1000f;
        if (height != 0.0f || bpheight != 0.0f && bpwidth != 0.0f) {
            float x = currentIPPosition / 1000f;
            float y = (currentBPPosition + area.getOffset()) / 1000f;
            float width = area.getIPD() / 1000f;
            drawBackAndBorders(area, x, y - borderPaddingBefore
                                , width + bpwidth
                                , height + bpheight);
        }
    }
    
    private static final QName FOX_TRANSFORM
            = new QName(ExtensionElementMapping.URI, "fox:transform");
    
    /** {@inheritDoc} */
    protected void renderBlockViewport(BlockViewport bv, List children) {
        // clip and position viewport if necessary

        // save positions
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        CTM ctm = bv.getCTM();
        int borderPaddingStart = bv.getBorderAndPaddingWidthStart();
        int borderPaddingBefore = bv.getBorderAndPaddingWidthBefore();
        //This is the content-rect
        float width = (float)bv.getIPD() / 1000f;
        float height = (float)bv.getBPD() / 1000f;

        if (bv.getPositioning() == Block.ABSOLUTE
                || bv.getPositioning() == Block.FIXED) {

            //For FIXED, we need to break out of the current viewports to the
            //one established by the page. We save the state stack for restoration
            //after the block-container has been painted. See below.
            List breakOutList = null;
            if (bv.getPositioning() == Block.FIXED) {
                breakOutList = breakOutOfStateStack();
            }
            
            AffineTransform positionTransform = new AffineTransform();
            positionTransform.translate(bv.getXOffset(), bv.getYOffset());
            
            //"left/"top" (bv.getX/YOffset()) specify the position of the content rectangle
            positionTransform.translate(-borderPaddingStart, -borderPaddingBefore);

            //Free transformation for the block-container viewport
            String transf;
            transf = bv.getForeignAttributeValue(FOX_TRANSFORM);
            if (transf != null) {
                AffineTransform freeTransform = AWTTransformProducer.createAffineTransform(transf);
                positionTransform.concatenate(freeTransform);
            }

            saveGraphicsState();
            //Viewport position
            concatenateTransformationMatrix(mptToPt(positionTransform));
            
            //Background and borders
            float bpwidth = (borderPaddingStart + bv.getBorderAndPaddingWidthEnd()) / 1000f;
            float bpheight = (borderPaddingBefore + bv.getBorderAndPaddingWidthAfter()) / 1000f;
            drawBackAndBorders(bv, 0, 0, width + bpwidth, height + bpheight);

            //Shift to content rectangle after border painting
            AffineTransform contentRectTransform = new AffineTransform();
            contentRectTransform.translate(borderPaddingStart, borderPaddingBefore);
            concatenateTransformationMatrix(mptToPt(contentRectTransform));
            
            //Clipping
            if (bv.getClip()) {
                clipRect(0f, 0f, width, height);
            }

            saveGraphicsState();
            //Set up coordinate system for content rectangle
            AffineTransform contentTransform = ctm.toAffineTransform();
            concatenateTransformationMatrix(mptToPt(contentTransform));
            
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);

            restoreGraphicsState();
            restoreGraphicsState();
            
            if (breakOutList != null) {
                restoreStateStackAfterBreakOut(breakOutList);
            }
            
            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
        } else {

            currentBPPosition += bv.getSpaceBefore();

            //borders and background in the old coordinate system
            handleBlockTraits(bv);

            //Advance to start of content area
            currentIPPosition += bv.getStartIndent();

            CTM tempctm = new CTM(containingIPPosition, currentBPPosition);
            ctm = tempctm.multiply(ctm);
            
            //Now adjust for border/padding
            currentBPPosition += borderPaddingBefore;

            Rectangle2D clippingRect = null;
            if (bv.getClip()) {
                clippingRect = new Rectangle(currentIPPosition, currentBPPosition, 
                        bv.getIPD(), bv.getBPD());
            }
            
            startVParea(ctm, clippingRect);
            currentIPPosition = 0;
            currentBPPosition = 0;
            renderBlocks(bv, children);
            endVParea();

            currentIPPosition = saveIP;
            currentBPPosition = saveBP;
            
            currentBPPosition += (int)(bv.getAllocBPD());
        }
    }

    /** {@inheritDoc} */
    protected void renderReferenceArea(Block block) {
        // save position and offset
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        //Establish a new coordinate system
        AffineTransform at = new AffineTransform();
        at.translate(currentIPPosition, currentBPPosition);
        at.translate(block.getXOffset(), block.getYOffset());
        at.translate(0, block.getSpaceBefore());
        
        if (!at.isIdentity()) {
            saveGraphicsState();
            concatenateTransformationMatrix(mptToPt(at));
        }

        currentIPPosition = 0;
        currentBPPosition = 0;
        handleBlockTraits(block);

        List children = block.getChildAreas();
        if (children != null) {
            renderBlocks(block, children);
        }

        if (!at.isIdentity()) {
            restoreGraphicsState();
        }
        
        // stacked and relative blocks effect stacking
        currentIPPosition = saveIP;
        currentBPPosition = saveBP;
    }
    
    /** {@inheritDoc} */
    protected void renderFlow(NormalFlow flow) {
        // save position and offset
        int saveIP = currentIPPosition;
        int saveBP = currentBPPosition;

        //Establish a new coordinate system
        AffineTransform at = new AffineTransform();
        at.translate(currentIPPosition, currentBPPosition);
        
        if (!at.isIdentity()) {
            saveGraphicsState();
            concatenateTransformationMatrix(mptToPt(at));
        }

        currentIPPosition = 0;
        currentBPPosition = 0;
        super.renderFlow(flow);
        
        if (!at.isIdentity()) {
            restoreGraphicsState();
        }
        
        // stacked and relative blocks effect stacking
        currentIPPosition = saveIP;
        currentBPPosition = saveBP;
    }
    
    /**
     * Concatenates the current transformation matrix with the given one, therefore establishing
     * a new coordinate system.
     * @param at the transformation matrix to process (coordinates in points)
     */
    protected abstract void concatenateTransformationMatrix(AffineTransform at);
    
    /**
     * Render an inline viewport.
     * This renders an inline viewport by clipping if necessary.
     * @param viewport the viewport to handle
     */
    public void renderViewport(Viewport viewport) {

        float x = currentIPPosition / 1000f;
        float y = (currentBPPosition + viewport.getOffset()) / 1000f;
        float width = viewport.getIPD() / 1000f;
        float height = viewport.getBPD() / 1000f;
        // TODO: Calculate the border rect correctly. 
        float borderPaddingStart = viewport.getBorderAndPaddingWidthStart() / 1000f;
        float borderPaddingBefore = viewport.getBorderAndPaddingWidthBefore() / 1000f;
        float bpwidth = borderPaddingStart 
                + (viewport.getBorderAndPaddingWidthEnd() / 1000f);
        float bpheight = borderPaddingBefore
                + (viewport.getBorderAndPaddingWidthAfter() / 1000f);

        drawBackAndBorders(viewport, x, y, width + bpwidth, height + bpheight);

        if (viewport.getClip()) {
            saveGraphicsState();

            clipRect(x + borderPaddingStart, y + borderPaddingBefore, width, height);
        }
        super.renderViewport(viewport);

        if (viewport.getClip()) {
            restoreGraphicsState();
        }
    }

    /**
     * Restores the state stack after a break out.
     * @param breakOutList the state stack to restore.
     */
    protected abstract void restoreStateStackAfterBreakOut(List breakOutList);
    
    /**
     * Breaks out of the state stack to handle fixed block-containers.
     * @return the saved state stack to recreate later
     */
    protected abstract List breakOutOfStateStack();

    /** Saves the graphics state of the rendering engine. */
    protected abstract void saveGraphicsState();
    
    /** Restores the last graphics state of the rendering engine. */
    protected abstract void restoreGraphicsState();

    /** Indicates the beginning of a text object. */
    protected abstract void beginTextObject();
    
    /** Indicates the end of a text object. */
    protected abstract void endTextObject();
    
    /**
     * Paints the text decoration marks.
     * @param fm Current typeface
     * @param fontsize Current font size
     * @param inline inline area to paint the marks for
     * @param baseline position of the baseline
     * @param startx start IPD
     */
    protected void renderTextDecoration(FontMetrics fm, int fontsize, InlineArea inline, 
                    int baseline, int startx) {
        boolean hasTextDeco = inline.hasUnderline() 
                || inline.hasOverline() 
                || inline.hasLineThrough();
        if (hasTextDeco) {
            endTextObject();
            float descender = fm.getDescender(fontsize) / 1000f;
            float capHeight = fm.getCapHeight(fontsize) / 1000f;
            float halfLineWidth = (descender / -8f) / 2f;
            float endx = (startx + inline.getIPD()) / 1000f;
            if (inline.hasUnderline()) {
                Color ct = (Color) inline.getTrait(Trait.UNDERLINE_COLOR);
                float y = baseline - descender / 2f;
                drawBorderLine(startx / 1000f, (y - halfLineWidth) / 1000f, 
                        endx, (y + halfLineWidth) / 1000f, 
                        true, true, Constants.EN_SOLID, ct);
            }
            if (inline.hasOverline()) {
                Color ct = (Color) inline.getTrait(Trait.OVERLINE_COLOR);
                float y = (float)(baseline - (1.1 * capHeight));
                drawBorderLine(startx / 1000f, (y - halfLineWidth) / 1000f, 
                        endx, (y + halfLineWidth) / 1000f, 
                        true, true, Constants.EN_SOLID, ct);
            }
            if (inline.hasLineThrough()) {
                Color ct = (Color) inline.getTrait(Trait.LINETHROUGH_COLOR);
                float y = (float)(baseline - (0.45 * capHeight));
                drawBorderLine(startx / 1000f, (y - halfLineWidth) / 1000f, 
                        endx, (y + halfLineWidth) / 1000f, 
                        true, true, Constants.EN_SOLID, ct);
            }
        }
    }

    /** Clip using the current path. */
    protected abstract void clip();
        
    /**
     * Clip using a rectangular area.
     * @param x the x coordinate (in points)
     * @param y the y coordinate (in points)
     * @param width the width of the rectangle (in points)
     * @param height the height of the rectangle (in points)
     */
    protected abstract void clipRect(float x, float y, float width, float height);
    
    /**
     * Moves the current point to (x, y), omitting any connecting line segment. 
     * @param x x coordinate
     * @param y y coordinate
     */
    protected abstract void moveTo(float x, float y);
    
    /**
     * Appends a straight line segment from the current point to (x, y). The 
     * new current point is (x, y). 
     * @param x x coordinate
     * @param y y coordinate
     */
    protected abstract void lineTo(float x, float y);
    
    /**
     * Closes the current subpath by appending a straight line segment from 
     * the current point to the starting point of the subpath.
     */
    protected abstract void closePath();
    
    /**
     * Fill a rectangular area.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    protected abstract void fillRect(float x, float y, float width, float height);

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     */
    protected abstract void updateColor(Color col, boolean fill);
    
    /**
     * Draw an image at the indicated location.
     * @param url the URI/URL of the image
     * @param pos the position of the image
     * @param foreignAttributes an optional Map with foreign attributes, may be null
     */
    protected abstract void drawImage(String url, Rectangle2D pos, Map foreignAttributes);
    
    /**
     * Draw an image at the indicated location.
     * @param url the URI/URL of the image
     * @param pos the position of the image
     */
    protected final void drawImage(String url, Rectangle2D pos) {
        drawImage(url, pos, null);
    }
    
    /**
     * Draw a border segment of an XSL-FO style border.
     * @param x1 starting x coordinate
     * @param y1 starting y coordinate
     * @param x2 ending x coordinate
     * @param y2 ending y coordinate
     * @param horz true for horizontal border segments, false for vertical border segments
     * @param startOrBefore true for border segments on the start or before edge, 
     *                      false for end or after.
     * @param style the border style (one of Constants.EN_DASHED etc.)
     * @param col the color for the border segment
     */
    protected abstract void drawBorderLine(float x1, float y1, float x2, float y2, 
            boolean horz, boolean startOrBefore, int style, Color col);

    /** {@inheritDoc} */
    public void renderForeignObject(ForeignObject fo, Rectangle2D pos) {
        endTextObject();
        Document doc = fo.getDocument();
        String ns = fo.getNameSpace();
        renderDocument(doc, ns, pos, fo.getForeignAttributes());
    }
    
}
