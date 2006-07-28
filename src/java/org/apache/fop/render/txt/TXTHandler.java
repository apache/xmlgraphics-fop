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

package org.apache.fop.render.txt;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.datatypes.CompoundDatatype;
import org.apache.fop.datatypes.Length;
import org.apache.fop.datatypes.PercentBaseContext;
import org.apache.fop.fo.Constants;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.expr.NumericProperty;
import org.apache.fop.fo.expr.RelativeNumericProperty;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.Table;
import org.apache.fop.fo.flow.TableCell;
import org.apache.fop.fo.flow.TableColumn;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.properties.CommonAbsolutePosition;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonFont;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.apache.fop.fo.properties.FixedLength;
import org.apache.fop.fo.properties.Property;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.layoutmgr.BlockLayoutManager;

/**
 * Handler for formatting objects in case of rendering to txt.
 * 
 * This handler gets page-sequence, modifies formatting objects and return them
 * to superclass. So areas are generated from modified FO. Idea of modifying is
 * to quantize FO properties, making them divisible by width of char or height 
 * of char.
 */
public class TXTHandler extends AreaTreeHandler {

    /** Percent base context. Needed for line-height. */
    private static final PercentBaseContext CONTEXT 
        = new BlockLayoutManager(new Block(null));

    /** Modified font size in millipoints. */
    private static final int MODIFIED_FONT_SIZE = 10000;

    /** Quantum for each side (BEFORE, AFTER, START, END). */
    private final int[] quantum = {TXTRenderer.CHAR_HEIGHT,
            TXTRenderer.CHAR_HEIGHT, TXTRenderer.CHAR_WIDTH,
            TXTRenderer.CHAR_WIDTH};

    /** Keeps overpatching for each side. */
    private int[] overPatching = new int[4];

    /**
     * Keeps last overpatching for each side. Needed for selective modifying of
     * start-indent and end-indent.
     */
    private int[] lastOverPatching = new int[4];

    /**
     * Constructs a newly allocated <code>TXTHandler</code> object.
     * 
     * @param userAgent FOUserAgent
     * @param stream OutputStream
     * @throws FOPException if the RenderPagesModel cannot be created
     */
    public TXTHandler(FOUserAgent userAgent, OutputStream stream)
            throws FOPException {
        super(userAgent, MimeConstants.MIME_PLAIN_TEXT, stream);
    }

    /**
     * Sets a component <code>CP_LENGTH</code> of <code>cd</code> to
     * <code>value</code>.

     * @param cd  CompoundDatatype
     * @param value  new integer value
     */
    private static void setLength(CompoundDatatype cd, int value) {
        cd.setComponent(Constants.CP_LENGTH, new FixedLength(value), true);
    }

    /**
     * Sets components <code>CP_MINIMUM, CP_OPTIMUM, CP_MAXIMUM</code> of
     * <code>cd</code> to <code>p</code>.
     * 
     * @param cd instance of CompoundDatatype for modifying.
     * @param p  property for setting.
     */
    private static void setMinOptMax(CompoundDatatype cd, Property p) {
        cd.setComponent(Constants.CP_MINIMUM, p, true);
        cd.setComponent(Constants.CP_OPTIMUM, p, true);
        cd.setComponent(Constants.CP_MAXIMUM, p, true);
    }

    /**
     * Modifies border of side. If there is no border of given side, does
     * nothing, otherwise sets border-width to half of char width or char height
     * depending on side. <p> 
     * Difference between values of new border-width and old border-width is 
     * saved in <code>lastOverPatching</code>.
     * 
     * @param side side to modify.
     * @param bpb instance of CommonBorderPaddingBackground for modifying.
     */
    private void modifyBorder(int side, CommonBorderPaddingBackground bpb) {
        CommonBorderPaddingBackground.BorderInfo bi = bpb.getBorderInfo(side);

        if (bi != null) {
            int width = bpb.getBorderWidth(side, false);
            setLength(bi.getWidth(), quantum[side] / 2);
            lastOverPatching[side] += bpb.getBorderWidth(side, false) - width;
        }
    }

    /**
     * Modifies padding of side. First rounds padding to nearest integer,
     * divisible by char width or char height depending on side. If border of
     * given side is available, modifies padding in such a way, so sum of border
     * width and padding will be divisible by char width or char height,
     * depending on side. <p>
     * Difference between values of new padding and old padding is saved 
     * in <code>lastOverPatching</code>.
     * 
     * @param side side to modify.
     * @param bpb instance of CommonBorderPaddingBackground for modifying.
     */
    private void modifyPadding(int side, CommonBorderPaddingBackground bpb) {
        int oldPadding = bpb.getPadding(side, false, null);
        int newPadding = Helper.round(oldPadding, quantum[side]);
        if (bpb.getBorderInfo(side) != null) {
            newPadding = Math.max(newPadding, quantum[side])
                    - bpb.getBorderWidth(side, false);
        }

        setLength(bpb.getPaddingLengthProperty(side), newPadding);
        lastOverPatching[side] += newPadding - oldPadding;
    }

    /**
     * Modifies borders and paddings of <code>bpb</code>.
     * 
     * @param bpb instance of CommonBorderPaddingBackground for modifying.
     */
    private void modifyBPB(CommonBorderPaddingBackground bpb) {
        modifyBorder(CommonBorderPaddingBackground.BEFORE, bpb);
        modifyBorder(CommonBorderPaddingBackground.AFTER, bpb);
        modifyBorder(CommonBorderPaddingBackground.START, bpb);
        modifyBorder(CommonBorderPaddingBackground.END, bpb);

        modifyPadding(CommonBorderPaddingBackground.BEFORE, bpb);
        modifyPadding(CommonBorderPaddingBackground.AFTER, bpb);
        modifyPadding(CommonBorderPaddingBackground.START, bpb);
        modifyPadding(CommonBorderPaddingBackground.END, bpb);
    }

    /**
     * Rounds optimum value of <code>space</code> to nearest integer,
     * divisible by <code>q</code>.
     * 
     * @param space instance of SpaceProperty.
     * @param q integer.
     */
    private void modifySpace(SpaceProperty space, int q) {
        int value = space.getOptimum(null).getLength().getValue();
        setMinOptMax(space, new FixedLength(Helper.round(value, q)));
    }

    /**
     * @param length instance of Length.
     * @param q integer.
     * @return instance of Length, having value nearest to value of
     *         <code>length</code>, and divisible by <code>q</code>.
     */
    private Length roundLength(Length length, int q) {
        int x = Helper.round(length.getValue(), q);
        return new FixedLength(x);
    }

    /**
     * @param length instance of Length.
     * @param q integer.
     * @return instance of Length, having minimal value, greater value of
     *         <code>length</code>, and divisible by <code>q</code>.
     */
    private Length ceilLength(Length length, int q) {
        int x = Helper.ceil(length.getValue(), q);
        return new FixedLength(x);
    }

    /**
     * Modifies indent for given side. Summarizes value of indent and modifing
     * error (i.e. overPatching). Rounds result to nearest integer, divisible by
     * quantum.
     * 
     * @param indent Length to modify.
     * @param side an integer, representing side.
     * @return modified Length.
     */
    private Length modifyIndent(Length indent, int side) {
        if (indent instanceof NumericProperty) {
            overPatching[side] += lastOverPatching[side];
        }
        int newValue = indent.getValue() + overPatching[side];
        newValue = Helper.round(newValue, quantum[side]);
        return new FixedLength(newValue);
    }

    /**
     * Modifies Common Margin Properties-Block:
     * <ul>
     * <li>margin-top, margin-left, margin-bottom, margin-right
     * <li>start-indent, end-indent
     * <li>space-before, space-after.
     * </ul>
     * 
     * @param cmb instance of CommonMarginBlock to modify.
     */
    private void modifyCommonMarginBlock(CommonMarginBlock cmb) {
        cmb.marginTop = roundLength(cmb.marginTop, TXTRenderer.CHAR_HEIGHT);
        cmb.marginBottom = roundLength(cmb.marginBottom,
                TXTRenderer.CHAR_HEIGHT);
        cmb.marginLeft = roundLength(cmb.marginLeft, TXTRenderer.CHAR_WIDTH);
        cmb.marginRight = roundLength(cmb.marginRight, TXTRenderer.CHAR_WIDTH);

        modifySpace(cmb.spaceBefore, TXTRenderer.CHAR_HEIGHT);
        modifySpace(cmb.spaceAfter, TXTRenderer.CHAR_HEIGHT);

        if (!(cmb.startIndent instanceof RelativeNumericProperty)) {
            cmb.startIndent = modifyIndent(cmb.startIndent,
                    CommonBorderPaddingBackground.START);
        }
        if (!(cmb.endIndent instanceof RelativeNumericProperty)) {
            cmb.endIndent = modifyIndent(cmb.endIndent,
                    CommonBorderPaddingBackground.END);
        }
    }

    /**
     * Modifies fo:table attributes:
     * <ul>
     * <li>Common Margin Properties Block
     * <li>Common Border, Padding, and Background Properties
     * <li>columns.
     * </ul>
     * 
     * @param table Table to modify.
     */
    private void modifyTable(Table table) {
        CommonMarginBlock cmb = table.getCommonMarginBlock();
        if (table.getBorderCollapse() == Constants.EN_COLLAPSE) {
            // If border-collapse == "collapse", add space-after in order to
            // impove interaction with other FO.
            int value = cmb.spaceAfter.getOptimum(null).getLength().getValue();
            value += TXTRenderer.CHAR_HEIGHT;
            setMinOptMax(cmb.spaceAfter, new FixedLength(value));
        }
        modifyCommonMarginBlock(cmb);

        modifyBPB(table.getCommonBorderPaddingBackground());

        // modify all table-columns
        List columnList = table.getColumns();
        Iterator iter = columnList.iterator();
        while (iter.hasNext()) {
            modifyTableColumn((TableColumn) iter.next());
        }
    }

    /**
     * Modifies fo:table-column attributes:
     * <ul>
     * <li>width.
     * </ul>
     * 
     * @param column TableColumn to modify.
     */
    private void modifyTableColumn(TableColumn column) {
        column.setColumnWidth(ceilLength(column.getColumnWidth(), 
                TXTRenderer.CHAR_WIDTH));
    }

    /**
     * Modifies padding of fo:table-cell.
     * 
     * @param side side.
     * @param bpb instance of CommonBorderPaddingBackground to modify.
     */
    private void modifyCellPadding(int side, CommonBorderPaddingBackground bpb) {
        if (bpb.getBorderInfo(side) == null) {
            int oldPadding = bpb.getPadding(side, false, null);
            int newPadding = oldPadding + quantum[side] / 2;
            setLength(bpb.getPaddingLengthProperty(side), newPadding);
        }
    }

    /**
     * Modifies table-cell properties:
     * <ul>
     * <li>Common Border, Padding, and Background Properties.
     * </ul>
     * 
     * @param c TableCell to modify.
     */
    private void modifyTableCell(TableCell c) {
        CommonBorderPaddingBackground bpb = c
                .getCommonBorderPaddingBackground();
        modifyBPB(bpb);
        modifyCellPadding(CommonBorderPaddingBackground.BEFORE, bpb);
        modifyCellPadding(CommonBorderPaddingBackground.AFTER, bpb);
        modifyCellPadding(CommonBorderPaddingBackground.START, bpb);
        modifyCellPadding(CommonBorderPaddingBackground.END, bpb);
    }

    /**
     * Modifies Common Absolute Position Properties:
     * <ul>
     * <li>left
     * <li>top.
     * </ul>
     * 
     * @param cap CommonAbsolutePosition to modify.
     */
    private void modifyCommonAbsolutePosition(CommonAbsolutePosition cap) {
        if (cap.absolutePosition == Constants.EN_ABSOLUTE) {
            cap.left = roundLength(cap.left, TXTRenderer.CHAR_WIDTH);
            cap.top = roundLength(cap.top, TXTRenderer.CHAR_HEIGHT);
        }
    }

    /**
     * Modifies line-height property. Sets a value of line-height to max(char
     * height; lowest integer, divisible by char height).
     * 
     * @param lineHeight SpaceProperty to modify.
     */
    private void modifyLineHeight(SpaceProperty lineHeight) {
        Property p = lineHeight.getOptimum(null);
        int value = p.getLength().getValue(CONTEXT);

        int height = TXTRenderer.CHAR_HEIGHT;
        int newValue = Math.max(Helper.floor(value, height), height);
        setMinOptMax(lineHeight, new FixedLength(newValue));
    }

    /**
     * Modifies Common Font Properties:
     * <ul>
     * <li>font-family = Courier;
     * <li>font-size = MODIFIED_FONT_SIZE;
     * <li>font-stretch = EN_NORMAL;
     * <li>font-weight = EN_NORMAL.
     * </ul>
     * 
     * @param cf the font to modify.
     */
    private void modifyCommonFont(CommonFont cf) {
        if (cf != null) {
            cf.overrideFontFamily("Courier");
            cf.fontSize = new FixedLength(MODIFIED_FONT_SIZE);
            cf.fontStretch = Constants.EN_NORMAL;
            cf.fontWeight = Constants.EN_NORMAL;
        }
    }

    /**
     * Modifies fo:block:
     * <ul>
     * <li>Common Border, Padding, and Background Properties
     * <li>Common Margin Properties-Block
     * <li>Common Font Properties
     * <li>line-height.
     * </ul>
     * 
     * @param block Block to modify.
     */
    private void modifyBlock(Block block) {
        modifyBPB(block.getCommonBorderPaddingBackground());
        modifyCommonMarginBlock(block.getCommonMarginBlock());
        modifyCommonFont(block.getCommonFont());
        modifyLineHeight(block.getLineHeight());
    }

    /**
     * Modifies fo:block-container:
     * <ul>
     * <li>Common Border, Padding, and Background Properties
     * <li>Common Margin Properties-Block
     * <li>Common Absolute Position Properties.
     * </ul>
     * 
     * @param bc BlockContainer to modify.
     */
    private void modifyBlockContainer(BlockContainer bc) {
        modifyBPB(bc.getCommonBorderPaddingBackground());
        modifyCommonMarginBlock(bc.getCommonMarginBlock());
        modifyCommonAbsolutePosition(bc.getCommonAbsolutePosition());
    }

    /**
     * Modifies fo:inline:
     * <ul>
     * <li>Common Font Properties
     * </ul>
     * 
     * @param inline Inline to modify.
     */
    private void modifyInline(Inline inline) {
        modifyCommonFont(inline.getCommonFont());
    }

    /**
     * Modifies FOText:
     * <ul>
     * <li>Common Font Properties
     * </ul>
     * 
     * @param text FOText to modify.
     */
    private void modifyFOText(FOText text) {
        modifyCommonFont(text.getCommonFont());
    }

    /**
     * Modifies fo:external-graphic:
     * <ul>
     * <li>Common Border, Padding, and Background Properties
     * <li>line-height.
     * </ul>
     * 
     * @param eg ExternalGraphic to modify.
     */
    private void modifyExternalGraphic(ExternalGraphic eg) {
        modifyBPB(eg.getCommonBorderPaddingBackground());
        modifyLineHeight(eg.getLineHeight());
    }

    /**
     * Modifies fo:list-block:
     * <ul>
     * <li>Common Border, Padding, and Background Properties
     * <li>Common Margin Properties-Block.
     * </ul>
     * 
     * @param lb ListBlock to modify.
     */
    private void modifyListBlock(ListBlock lb) {
        modifyBPB(lb.getCommonBorderPaddingBackground());
        modifyCommonMarginBlock(lb.getCommonMarginBlock());
    }

    /**
     * Modifies fo:list-item:
     * <ul>
     * <li>Common Border, Padding, and Background Properties
     * <li>Common Margin Properties-Block.
     * </ul>
     * <p>
     * Make refinement for fo:list-item-label and fo:list-item-body.
     * 
     * @param li ListItem to modify.
     */
    private void modifyListItem(ListItem li) {
        modifyBPB(li.getCommonBorderPaddingBackground());
        modifyCommonMarginBlock(li.getCommonMarginBlock());
        refinement(li.getLabel());
        refinement(li.getBody());
    }
    
    /**
     * Does refinement for particular node. Modifies node's properties and
     * refines its children recursively.
     * 
     * @param node the node to refine.
     */
    private void refinement(FONode node) {
        int[] saveOverPatching = (int[]) overPatching.clone();
        Arrays.fill(lastOverPatching, 0);

        if (node instanceof Block) {
            modifyBlock((Block) node);
        } else if (node instanceof BlockContainer) {
            modifyBlockContainer((BlockContainer) node);
        } else if (node instanceof Inline) {
            modifyInline((Inline) node);
        } else if (node instanceof FOText) {
            modifyFOText((FOText) node);
        } else if (node instanceof Table) {
            modifyTable((Table) node);
            Arrays.fill(overPatching, 0);
        } else if (node instanceof TableCell) {
            modifyTableCell((TableCell) node);
        } else if (node instanceof ExternalGraphic) {
            modifyExternalGraphic((ExternalGraphic) node);
        } else if (node instanceof ListBlock) {
            modifyListBlock((ListBlock) node);
        } else if (node instanceof ListItem) {
            modifyListItem((ListItem) node);
        } else if (node instanceof PageNumber) {
            modifyCommonFont(((PageNumber) node).getCommonFont());
        }

        Iterator it = node.getChildNodes();
        if (it != null) {
            while (it.hasNext()) {
                refinement((FONode) it.next());
            }
        }
        overPatching = saveOverPatching;
    }

    /**
     * Run refinement for:
     * <ul>
     * <li>mainflow (xsl-region-body)
     * <li>staticflow (xsl-region-before, xsl-region-after, xsl-region-start,
     * xsl-region-end).
     * </ul>
     * 
     * @param pageSequence PageSequence to refine.
     */
    public void endPageSequence(PageSequence pageSequence) {
        Arrays.fill(overPatching, 0);

        refinement(pageSequence.getMainFlow());

        if (pageSequence.getStaticContent("xsl-region-before") != null) {
            refinement(pageSequence.getStaticContent("xsl-region-before"));
        }
        if (pageSequence.getStaticContent("xsl-region-after") != null) {
            refinement(pageSequence.getStaticContent("xsl-region-after"));
        }
        if (pageSequence.getStaticContent("xsl-region-start") != null) {
            refinement(pageSequence.getStaticContent("xsl-region-start"));
        }
        if (pageSequence.getStaticContent("xsl-region-end") != null) {
            refinement(pageSequence.getStaticContent("xsl-region-end"));
        }

        super.endPageSequence(pageSequence);
    }
}
