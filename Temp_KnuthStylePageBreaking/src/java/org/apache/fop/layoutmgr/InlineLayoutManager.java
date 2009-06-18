/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.InlineLevel;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginInline;
import org.apache.fop.fo.properties.SpaceProperty;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.traits.SpaceVal;

/**
 * LayoutManager for objects which stack children in the inline direction,
 * such as Inline or Line
 */
public class InlineLayoutManager extends InlineStackingLayoutManager 
                                         implements InlineLevelLayoutManager {
    private InlineLevel fobj;

    private CommonMarginInline inlineProps = null;
    private CommonBorderPaddingBackground borderProps = null;

    /**
     * Create an inline layout manager.
     * This is used for fo's that create areas that
     * contain inline areas.
     *
     * @param node the formatting object that creates the area
     */
    // The node should be FObjMixed
    public InlineLayoutManager(InlineLevel node) {
        super(node);
        fobj = node;
    }
    
    /**
     * @see org.apache.fop.layoutmgr.AbstractLayoutManager#initProperties()
     */
    protected void initProperties() {
        inlineProps = fobj.getCommonMarginInline();
        borderProps = fobj.getCommonBorderPaddingBackground();

        int iPad = borderProps.getPadding(CommonBorderPaddingBackground.BEFORE, false);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.BEFORE,
                                             false);
        iPad += borderProps.getPadding(CommonBorderPaddingBackground.AFTER, false);
        iPad += borderProps.getBorderWidth(CommonBorderPaddingBackground.AFTER, false);
        extraBPD = new MinOptMax(iPad);
    }

    protected MinOptMax getExtraIPD(boolean bNotFirst, boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        iBP += borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return new MinOptMax(iBP);
    }


    protected boolean hasLeadingFence(boolean bNotFirst) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.START,
                                           bNotFirst);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.START,
                                            bNotFirst);
        return (iBP > 0);
    }

    protected boolean hasTrailingFence(boolean bNotLast) {
        int iBP = borderProps.getPadding(CommonBorderPaddingBackground.END, bNotLast);
        iBP += borderProps.getBorderWidth(CommonBorderPaddingBackground.END, bNotLast);
        return (iBP > 0);
    }

    protected SpaceProperty getSpaceStart() {
        return inlineProps.spaceStart;
    }
    protected SpaceProperty getSpaceEnd() {
        return inlineProps.spaceEnd;
    }
    
    /**
     * Return value indicating whether the next area to be generated could
     * start a new line. This should only be called in the "START" condition
     * if a previous inline BP couldn't end the line.
     * Return true if any space-start, border-start or padding-start, else
     * propagate to first child LM
     */
    public boolean canBreakBefore(LayoutContext context) {
        if (new SpaceVal(inlineProps.spaceStart).getSpace().min > 0 || hasLeadingFence(false)) {
            return true;
        }
        return super.canBreakBefore(context);
    }
    
    protected void setTraits(boolean bNotFirst, boolean bNotLast) {
        
        // Add border and padding to current area and set flags (FIRST, LAST ...)
        TraitSetter.setBorderPaddingTraits(getCurrentArea(),
                                           borderProps, bNotFirst, bNotLast);

        if (borderProps != null) {
            TraitSetter.addBorders(getCurrentArea(), borderProps);
            TraitSetter.addBackground(getCurrentArea(), borderProps);
        }
    }

    public LinkedList getNextKnuthElements(LayoutContext lc, int alignment) {
        InlineLevelLayoutManager curLM;

        // the list returned by child LM
        LinkedList returnedList;
        KnuthElement returnedElement;

        // the list which will be returned to the parent LM
        LinkedList returnList = new LinkedList();

        SpaceSpecifier leadingSpace = lc.getLeadingSpace();

        if (lc.startsNewArea()) {
            // First call to this LM in new parent "area", but this may
            // not be the first area created by this inline
            childLC = new LayoutContext(lc);
            if (getSpaceStart() != null) {
                lc.getLeadingSpace().addSpace(new SpaceVal(getSpaceStart()));
            }

            // Check for "fence"
            if (hasLeadingFence(!lc.isFirstArea())) {
                // Reset leading space sequence for child areas
                leadingSpace = new SpaceSpecifier(false);
            }
            // Reset state variables
            clearPrevIPD(); // Clear stored prev content dimensions
        }

        while ((curLM = (InlineLevelLayoutManager) getChildLM()) != null) {
            // get KnuthElements from curLM
            returnedList = curLM.getNextKnuthElements(lc, alignment);
            if (returnedList != null) {
                // "wrap" the Position stored in each element of returnedList
                ListIterator listIter = returnedList.listIterator();
                while (listIter.hasNext()) {
                    returnedElement = (KnuthElement) listIter.next();
                    returnedElement.setPosition
                        (new NonLeafPosition(this,
                                             returnedElement.getPosition()));
                    returnList.add(returnedElement);
                }
                return returnList;
            } else {
                // curLM returned null because it finished;
                // just iterate once more to see if there is another child
            }
        }
        setFinished(true);
        return null;
    }

    /*
    public KnuthElement addALetterSpaceTo(KnuthElement element) {
        NonLeafPosition savedPos = (NonLeafPosition) element.getPosition();
        element.setPosition(savedPos.getPosition());

        KnuthElement newElement
            = ((InlineLevelLayoutManager)
               element.getLayoutManager()).addALetterSpaceTo(element);
        newElement.setPosition
            (new NonLeafPosition(this, newElement.getPosition()));
        element.setPosition(savedPos);
        return newElement;
    }

    public void getWordChars(StringBuffer sbChars, Position pos) {
        Position newPos = ((NonLeafPosition) pos).getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).getWordChars(sbChars, newPos);
    }

    public void hyphenate(Position pos, HyphContext hc) {
        Position newPos = ((NonLeafPosition) pos).getPosition();
        ((InlineLevelLayoutManager)
         newPos.getLM()).hyphenate(newPos, hc);
    }

    public boolean applyChanges(List oldList) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (((NonLeafPosition) oldElement.getPosition()).getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        boolean bSomethingChanged = false;
        while(oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            currLM = (InlineLevelLayoutManager) oldElement.getLayoutManager();
            // initialize prevLM
            if (prevLM == null) {
                prevLM = currLM;
            }

            if (currLM != prevLM || !oldListIterator.hasNext()) {
                if (oldListIterator.hasNext()) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()))
                        || bSomethingChanged;
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldList.size()))
                        || bSomethingChanged;
                } else {
                    bSomethingChanged
                        = prevLM.applyChanges(oldList.subList(fromIndex, oldListIterator.previousIndex()))
                        || bSomethingChanged;
                    bSomethingChanged
                        = currLM.applyChanges(oldList.subList(oldListIterator.previousIndex(), oldList.size()))
                        || bSomethingChanged;
                }
            }
        }

        // "wrap" again the Positions stored in the elements
        oldListIterator = oldList.listIterator();
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (new NonLeafPosition(this, oldElement.getPosition()));
        }
        return bSomethingChanged;
    }

    public LinkedList getChangedKnuthElements(List oldList, int flaggedPenalty, int alignment) {
        // "unwrap" the Positions stored in the elements
        ListIterator oldListIterator = oldList.listIterator();
        KnuthElement oldElement;
        while (oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            oldElement.setPosition
                (((NonLeafPosition) oldElement.getPosition()).getPosition());
        }
        // reset the iterator
        oldListIterator = oldList.listIterator();

        KnuthElement returnedElement;
        LinkedList returnedList = new LinkedList();
        LinkedList returnList = new LinkedList();
        InlineLevelLayoutManager prevLM = null;
        InlineLevelLayoutManager currLM;
        int fromIndex = 0;

        while(oldListIterator.hasNext()) {
            oldElement = (KnuthElement) oldListIterator.next();
            currLM = (InlineLevelLayoutManager) oldElement.getLayoutManager();
            if (prevLM == null) {
                prevLM = currLM;
            }

            if (currLM != prevLM || !oldListIterator.hasNext()) {
                if (oldListIterator.hasNext()) {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex,
                                          oldListIterator.previousIndex()),
                          flaggedPenalty, alignment));
                    prevLM = currLM;
                    fromIndex = oldListIterator.previousIndex();
                } else if (currLM == prevLM) {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex, oldList.size()),
                          flaggedPenalty, alignment));
                } else {
                    returnedList.addAll
                        (prevLM.getChangedKnuthElements
                         (oldList.subList(fromIndex,
                                          oldListIterator.previousIndex()),
                          flaggedPenalty, alignment));
                    returnedList.addAll
                        (currLM.getChangedKnuthElements
                         (oldList.subList(oldListIterator.previousIndex(),
                                          oldList.size()),
                          flaggedPenalty, alignment));
                }
            }
        }

        // "wrap" the Position stored in each element of returnedList
        ListIterator listIter = returnedList.listIterator();
        while (listIter.hasNext()) {
            returnedElement = (KnuthElement) listIter.next();
            returnedElement.setPosition
                (new NonLeafPosition(this, returnedElement.getPosition()));
            returnList.add(returnedElement);
        }
        return returnList;
    }*/
}

