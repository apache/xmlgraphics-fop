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
package org.apache.fop.layoutmgr.table;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlgraphics.java2d.color.ColorUtil;

import org.apache.fop.area.Block;
import org.apache.fop.area.Trait;
import org.apache.fop.traits.BorderProps;

public class OverPaintBorders {
    protected OverPaintBorders(Block curBlockArea) {
        List<Block> newBlocks = new ArrayList<Block>();
        List<Object> childAreas = new ArrayList<Object>(curBlockArea.getChildAreas());
        Collections.sort(childAreas, new SortBlocksByXOffset());
        mergeBordersOfType(newBlocks, childAreas, new int[]{Trait.BORDER_BEFORE, Trait.BORDER_AFTER});
        Collections.sort(childAreas, new SortBlocksByYOffset());
        mergeBordersOfType(newBlocks, childAreas, new int[]{Trait.BORDER_START, Trait.BORDER_END});
        for (Block borderBlock : newBlocks) {
            curBlockArea.addBlock(borderBlock);
        }
    }

     static class SortBlocksByXOffset implements Comparator<Object>, Serializable {
         private static final long serialVersionUID = 5368454957520223766L;
         public int compare(Object o1, Object o2) {
            Block b1 = (Block) o1;
            Block b2 = (Block) o2;
            Integer paddingStart1 = (Integer) b1.getTrait(Trait.PADDING_START);
            Integer paddingStart2 = (Integer) b2.getTrait(Trait.PADDING_START);
            int x1 = b1.getXOffset() - (paddingStart1 != null ? paddingStart1 : 0);
            int x2 = b2.getXOffset() - (paddingStart2 != null ? paddingStart2 : 0);
            if (x1 > x2) {
                return 1;
            } else if (x1 < x2) {
                return -1;
            } else {
                return Integer.compare(b1.getYOffset(), b2.getYOffset());
            }
        }
    }

    static class SortBlocksByYOffset implements Comparator<Object>, Serializable {
        private static final long serialVersionUID = -1166133555737149237L;
        public int compare(Object o1, Object o2) {
            Block b1 = (Block) o1;
            Block b2 = (Block) o2;
            Integer paddingStart1 = (Integer) b1.getTrait(Trait.PADDING_START);
            Integer paddingStart2 = (Integer) b2.getTrait(Trait.PADDING_START);
            int x1 = b1.getXOffset() - (paddingStart1 != null ? paddingStart1 : 0);
            int x2 = b2.getXOffset() - (paddingStart2 != null ? paddingStart2 : 0);
            if (b1.getYOffset() > b2.getYOffset()) {
                return 1;
            } else if (b1.getYOffset() < b2.getYOffset()) {
                return -1;
            } else {
                return Integer.compare(x1, x2);
            }
        }
    }

    private void mergeBordersOfType(List<Block> newBlocks, List<?> childAreas, int[] borderTraits) {
        Map<Integer, Map<Point, Block>> mergeMap = new HashMap<Integer, Map<Point, Block>>();
        for (int traitType : borderTraits) {
            mergeMap.put(traitType, null);
        }
        for (Object child : childAreas) {
            Block childBlock = (Block) child;
            BorderProps startBps = (BorderProps) childBlock.getTrait(Trait.BORDER_START);
            BorderProps endBps = (BorderProps) childBlock.getTrait(Trait.BORDER_END);
            BorderProps beforeBps = (BorderProps) childBlock.getTrait(Trait.BORDER_BEFORE);
            BorderProps afterBps = (BorderProps) childBlock.getTrait(Trait.BORDER_AFTER);
            for (int traitType : borderTraits) {
                Block currBlock = childBlock;
                BorderProps borderProps = (BorderProps) currBlock.getTrait(traitType);
                if (borderProps == null) {
                    continue;
                }
                Map<Point, Block> currTraitMap = mergeMap.get(traitType);
                Point endPoint = getEndMiddlePoint(currBlock, traitType, startBps, endBps, beforeBps, afterBps);
                BorderProps bpsCurr = (BorderProps) currBlock.getTrait(traitType);
                Block prevBlock = null;
                if (currTraitMap == null) {
                    currTraitMap = new HashMap<Point, Block>();
                    mergeMap.put(traitType, currTraitMap);
                } else {
                    Point startPoint = getStartMiddlePoint(currBlock, traitType, startBps, endBps, beforeBps, afterBps);
                    for (Map.Entry<Point, Block> entry : currTraitMap.entrySet()) {
                        Point prevEndPoint = entry.getKey();
                        boolean isVertical = traitType == Trait.BORDER_START || traitType == Trait.BORDER_END;
                        boolean isHorizontal = traitType == Trait.BORDER_BEFORE || traitType == Trait.BORDER_AFTER;
                        if ((isHorizontal && prevEndPoint.y == startPoint.y && prevEndPoint.x >= startPoint.x)
                                || (isVertical && prevEndPoint.x == startPoint.x && prevEndPoint.y >= startPoint.y)) {
                            Block prevBlockCurr = entry.getValue();
                            currTraitMap.remove(prevEndPoint);
                            BorderProps bpsPrev = (BorderProps) prevBlockCurr.getTrait(traitType);
                            if (canMergeBorders(bpsPrev, bpsCurr)) {
                                prevBlock = prevBlockCurr;
                            }
                            break;
                        }
                    }
                }
                Block borderBlock;
                if (prevBlock != null && newBlocks.contains(prevBlock)) {
                    borderBlock = prevBlock;
                } else {
                    borderBlock = new Block();
                    borderBlock.addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
                    borderBlock.setPositioning(Block.ABSOLUTE);
                    borderBlock.setBidiLevel(currBlock.getBidiLevel());
                    newBlocks.add(borderBlock);
                    BorderProps prevBeforeBps = (BorderProps) currBlock.getTrait(Trait.BORDER_BEFORE);
                    int prevBefore = prevBeforeBps != null ? prevBeforeBps.width : 0;
                    Integer prevPaddingStart = (Integer) currBlock.getTrait(Trait.PADDING_START);
                    Integer prevPaddingEnd = (Integer) currBlock.getTrait(Trait.PADDING_END);
                    Integer prevPaddingBefore = (Integer) currBlock.getTrait(Trait.PADDING_BEFORE);
                    Integer prevPaddingAfter = (Integer) currBlock.getTrait(Trait.PADDING_AFTER);
                    if (traitType == Trait.BORDER_START) {
                        borderBlock.setYOffset(currBlock.getYOffset() + prevBefore);
                        borderBlock.setXOffset(currBlock.getXOffset()
                                - (prevPaddingStart != null ? prevPaddingStart : 0));
                    } else if (traitType == Trait.BORDER_END) {
                        borderBlock.setYOffset(currBlock.getYOffset() + prevBefore);
                        borderBlock.setXOffset(currBlock.getXOffset()
                                - (prevPaddingStart != null ? prevPaddingStart : 0));
                        borderBlock.setIPD(currBlock.getIPD()
                                + (prevPaddingStart != null ? prevPaddingStart : 0)
                                + (prevPaddingEnd != null ? prevPaddingEnd : 0));
                    } else if (traitType == Trait.BORDER_BEFORE) {
                        borderBlock.setYOffset(currBlock.getYOffset());
                        borderBlock.setXOffset(currBlock.getXOffset()
                                - (prevPaddingStart != null ? prevPaddingStart : 0));
                    } else if (traitType == Trait.BORDER_AFTER) {
                        borderBlock.setYOffset(currBlock.getYOffset() + prevBefore);
                        borderBlock.setXOffset(currBlock.getXOffset()
                                - (prevPaddingStart != null ? prevPaddingStart : 0));
                        borderBlock.setBPD(currBlock.getBPD()
                                + (prevPaddingBefore != null ? prevPaddingBefore : 0)
                                + (prevPaddingAfter != null ? prevPaddingAfter : 0));
                    }
                }
                Integer paddingEnd = (Integer) currBlock.getTrait(Trait.PADDING_END);
                Integer paddingAfter = (Integer) currBlock.getTrait(Trait.PADDING_AFTER);
                if (traitType == Trait.BORDER_BEFORE || traitType == Trait.BORDER_AFTER) {
                    int newEndPoint = currBlock.getXOffset() + currBlock.getIPD()
                            + (paddingEnd != null ? paddingEnd : 0);
                    borderBlock.setIPD(newEndPoint - borderBlock.getXOffset());
                } else if (traitType == Trait.BORDER_START || traitType == Trait.BORDER_END) {
                    int newEndPoint = currBlock.getYOffset() + currBlock.getBPD()
                            + currBlock.getBorderAndPaddingWidthBefore()
                            + (paddingAfter != null ? paddingAfter : 0);
                    borderBlock.setBPD(newEndPoint - borderBlock.getYOffset());
                }
                BorderProps newBps = new BorderProps(bpsCurr.style, bpsCurr.width, 0, 0,
                        bpsCurr.color, bpsCurr.getMode());
                borderBlock.addTrait(traitType, newBps);
                currBlock = borderBlock;
                currTraitMap.put(endPoint, currBlock);
            }
        }
    }

    private boolean canMergeBorders(BorderProps bpsPrev, BorderProps bpsCurr) {
        return bpsPrev.style == bpsCurr.style
                && ColorUtil.isSameColor(bpsPrev.color, bpsCurr.color)
                && bpsPrev.width == bpsCurr.width
                && bpsPrev.getMode() == bpsPrev.getMode()
                && bpsPrev.getRadiusEnd() == 0
                && bpsCurr.getRadiusStart() == 0;
    }

    private Point getEndMiddlePoint(Block block, int borderTrait, BorderProps startBps,
                                    BorderProps endBps, BorderProps beforeBps, BorderProps afterBps) {
        int x;
        int y;
        if (borderTrait == Trait.BORDER_START) {
            Integer paddingStart = (Integer) block.getTrait(Trait.PADDING_START);
            x = block.getXOffset()
                    - (paddingStart != null ? paddingStart : 0)
                    - BorderProps.getClippedWidth(startBps);
            y = block.getYOffset() + block.getBPD()
                    + block.getBorderAndPaddingWidthBefore()
                    + block.getBorderAndPaddingWidthAfter();
        } else if (borderTrait == Trait.BORDER_END) {
            Integer paddingEnd = (Integer) block.getTrait(Trait.PADDING_END);
            x = block.getXOffset() + block.getIPD()
                    + (paddingEnd != null ? paddingEnd : 0)
                    + BorderProps.getClippedWidth(endBps);
            y = block.getYOffset() + block.getBPD()
                    + block.getBorderAndPaddingWidthBefore()
                    + block.getBorderAndPaddingWidthAfter();
        } else if (borderTrait == Trait.BORDER_AFTER) {
            Integer paddingEnd = (Integer) block.getTrait(Trait.PADDING_END);
            x = block.getXOffset() + block.getIPD()
                    + (paddingEnd != null ? paddingEnd : 0)
                    + BorderProps.getClippedWidth(endBps);
            Integer paddingAfter = (Integer) block.getTrait(Trait.PADDING_AFTER);
            y = block.getYOffset() + block.getBPD()
                    + block.getBorderAndPaddingWidthBefore()
                    + (paddingAfter != null ? paddingAfter : 0)
                    + BorderProps.getClippedWidth(afterBps);
        } else if (borderTrait == Trait.BORDER_BEFORE) {
            Integer paddingEnd = (Integer) block.getTrait(Trait.PADDING_END);
            x = block.getXOffset() + block.getIPD()
                    + (paddingEnd != null ? paddingEnd : 0)
                    + BorderProps.getClippedWidth(endBps);
            y = block.getYOffset()
                    + BorderProps.getClippedWidth(beforeBps);
        } else {
            throw new IllegalArgumentException("Invalid trait: " + borderTrait);
        }
        return new Point(x, y);
    }

    private Point getStartMiddlePoint(Block block, int borderTrait, BorderProps startBps, BorderProps endBps,
                                      BorderProps beforeBps, BorderProps afterBps) {
        int x;
        int y;
        if (borderTrait == Trait.BORDER_START) {
            Integer paddingStart = (Integer) block.getTrait(Trait.PADDING_START);
            x = block.getXOffset()
                    - (paddingStart != null ? paddingStart : 0)
                    - BorderProps.getClippedWidth(startBps);
            y = block.getYOffset();
        } else if (borderTrait == Trait.BORDER_BEFORE) {
            x = block.getXOffset() - block.getBorderAndPaddingWidthStart();
            y = block.getYOffset()
                    + BorderProps.getClippedWidth(beforeBps);
        } else if (borderTrait == Trait.BORDER_END) {
            Integer paddingEnd = (Integer) block.getTrait(Trait.PADDING_END);
            x = block.getXOffset() + block.getIPD()
                    + (paddingEnd != null ? paddingEnd : 0)
                    + BorderProps.getClippedWidth(endBps);
            y = block.getYOffset();
        } else if (borderTrait == Trait.BORDER_AFTER) {
            x = block.getXOffset() - block.getBorderAndPaddingWidthStart();
            Integer paddingAfter = (Integer) block.getTrait(Trait.PADDING_AFTER);
            y = block.getYOffset() + block.getBorderAndPaddingWidthBefore()
                    + block.getBPD()
                    + (paddingAfter != null ? paddingAfter : 0)
                    + BorderProps.getClippedWidth(afterBps);
        } else {
            throw new IllegalArgumentException("Invalid trait: " + borderTrait);
        }
        return new Point(x, y);
    }
}
