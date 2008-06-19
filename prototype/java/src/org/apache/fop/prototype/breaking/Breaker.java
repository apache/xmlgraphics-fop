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

package org.apache.fop.prototype.breaking;

import java.util.List;

import org.apache.fop.prototype.TypographicElement;
import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.breaking.layout.LineLayout;
import org.apache.fop.prototype.knuth.KnuthElement;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * Base class for breaking a list of elements into parts (pages, lines...).
 */
abstract class Breaker<L extends Layout> {

    protected ActiveLayouts<L> layouts;

    private Breaker<?> parentBreaker;

    public void findBreaks(List<? extends TypographicElement> content) {
        initBreaking();
        for (TypographicElement e: content) {
            if (e instanceof KnuthElement) {
                KnuthElement k = (KnuthElement) e;
                if (k.isBox() || k.isGlue()) {
                    layouts.updateLayouts(k, this);
                } else if (k.isPenalty()) {
                    Penalty p = (Penalty) k;
                    if (p.getPenalty() < Penalty.INFINITE) {
                        considerLegalBreak(p);
                    }
                }
            } else {
                handleElement(e);
            }
        }
        endBreaking();
    }

    /**
     * @return the parentBreaker
     */
    Breaker<?> getParentBreaker() {
        return parentBreaker;
    }

    /**
     * @param parentBreaker the parentBreaker to set
     */
    void setParentBreaker(Breaker<?> parentBreaker) {
        this.parentBreaker = parentBreaker;
    }

    /** Performs optional necessary stuff before the breaking starts. */
    protected void initBreaking() { }

    /**
     * Returns the sub-layout of the given layout that holds progress information for this
     * kind of breaking. For page breaking this is the layout itself, for line breaking
     * this is the enclosed layout that holds line-level information.
     * 
     * @param layout a layout
     * @return the sub-layout corresponding to the kind of breaking performed by this
     * object.
     * @see LineLayout#getLineLayout()
     */
    abstract Layout getLayout(L layout);

    protected abstract void considerLegalBreak(Penalty p);

    void newLayoutsFound(LineBreaker breaker, ActiveLayouts<LineLayout> newLayouts) { }

    protected void handleElement(TypographicElement e) { }

    /** Performs optional operations once the breaking is finished. */
    protected void endBreaking() { }
}
