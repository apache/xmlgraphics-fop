/*
 * Copyright 2007 The Apache Software Foundation.
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

package org.apache.fop.layoutmgr.inline;

import org.apache.fop.layoutmgr.InlineKnuthSequence;
import org.apache.fop.layoutmgr.KnuthElement;
import org.apache.fop.layoutmgr.KnuthGlue;
import org.apache.fop.layoutmgr.KnuthPenalty;
import org.apache.fop.layoutmgr.KnuthSequence;
import org.apache.fop.traits.MinOptMax;
import org.apache.fop.fo.Constants;

/**
 * This class represents the Knuth representation of a paragraph
 */
public class KnuthParagraph extends InlineKnuthSequence {
    
    private LineLayoutManager lineLM;

    /** Number of elements to ignore at the beginning of the list. */ 
    private int ignoreAtStart = 0;
    /** Number of elements to ignore at the end of the list. */
    private int ignoreAtEnd = 0;

    // space at the end of the last line (in millipoints)
    private MinOptMax lineFiller;
    public int textAlignment;
    public int textAlignmentLast;
    // textIndent is 0 if the first line of this paragraph should not be indented
    // i.e. if it is not the first paragraph of a Block
    public int textIndent;
    public int lastLineEndIndent;
    public int lineWidth;
    public int maxFlaggedPenaltiesCount;
    private LineLayoutPossibilities llPoss;

    public KnuthParagraph(int alignment, int alignmentLast, int indent, int endIndent,
                          int maxFlaggedPenaltiesCount, LineLayoutManager lineLM) {
        super();
        textAlignment = alignment;
        textAlignmentLast = alignmentLast;
        textIndent = indent;
        lastLineEndIndent = endIndent;
        this.maxFlaggedPenaltiesCount = maxFlaggedPenaltiesCount;
        this.lineLM = lineLM;
    }
    
    /**
     * @return the lineFiller
     */
    public MinOptMax getLineFiller() {
        return lineFiller;
    }
    
    /**
     * @return the ignoreAtEnd
     */
    public int getIgnoreAtEnd() {
        return ignoreAtEnd;
    }
    
    /**
     * @return the ignoreAtStart
     */
    public int getIgnoreAtStart() {
        return ignoreAtStart;
    }
    
    /**
     * @return the lineLM
     */
    public LineLayoutManager getLineLayoutManager() {
        return lineLM;
    }

    
    /**
     * @return the llPoss
     */
    public LineLayoutPossibilities getLlPoss() {
        return llPoss;
    }

    
    /**
     * @param llPoss the llPoss to set
     */
    public void setLlPoss(LineLayoutPossibilities llPos) {
        this.llPoss = llPos;
    }

    public void startParagraph(int lw) {
        // this is an early value of the linewidth;
        // it is used as the max value of the lineFiller
        lineWidth = lw;
        startSequence();
    }

    public void startSequence() {
        // set the minimum amount of empty space at the end of the
        // last line
        if (textAlignment == Constants.EN_CENTER) {
            lineFiller = new MinOptMax(lastLineEndIndent); 
        } else {
            lineFiller = new MinOptMax(lastLineEndIndent, lastLineEndIndent, lineWidth); 
        }

        // add auxiliary elements at the beginning of the paragraph
        if (textAlignment == Constants.EN_CENTER && textAlignmentLast != Constants.EN_JUSTIFY) {
            this.add(new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                   null, false));
            ignoreAtStart++;
        }

        // add the element representing text indentation
        // at the beginning of the first paragraph
        if (textIndent != 0) {
            this.add(new KnuthInlineBox(textIndent, null, null, false));
            ignoreAtStart++;
        }
    }

    public KnuthSequence endSequence() {
        if (this.size() > ignoreAtStart) {
            if (textAlignment == Constants.EN_CENTER
                && textAlignmentLast != Constants.EN_JUSTIFY) {
                this.add(new KnuthGlue(0, 3 * LineLayoutManager.DEFAULT_SPACE_WIDTH, 0,
                                       null, false));
                this.add(new KnuthPenalty(lineFiller.opt, -KnuthElement.INFINITE,
                                          false, null, false));
                ignoreAtEnd = 2;
            } else if (textAlignmentLast != Constants.EN_JUSTIFY) {
                // add the elements representing the space
                // at the end of the last line
                // and the forced break
                this.add(new KnuthPenalty(0, KnuthElement.INFINITE, 
                                          false, null, false));
                this.add(new KnuthGlue(0, 
                        lineFiller.max - lineFiller.opt, 
                        lineFiller.opt - lineFiller.min, null, false));
                this.add(new KnuthPenalty(lineFiller.opt, -KnuthElement.INFINITE,
                                          false, null, false));
                ignoreAtEnd = 3;
            } else {
                // add only the element representing the forced break
                this.add(new KnuthPenalty(lineFiller.opt, -KnuthElement.INFINITE,
                                          false, null, false));
                ignoreAtEnd = 1;
            }
            return this;
        } else {
            this.clear();
            return null;
        }
    }

    /**
     * @return true if the sequence contains a box
     */
    public boolean containsBox() {
        for (int i = 0; i < this.size(); i++) {
            KnuthElement el = (KnuthElement) this.get(i);
            if (el.isBox()) {
                return true;
            }
        }
        return false;
    }
}
