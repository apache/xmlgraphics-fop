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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.prototype.breaking.layout.Layout;
import org.apache.fop.prototype.knuth.KnuthElement;
import org.apache.fop.prototype.knuth.Penalty;


/**
 * Base class for breaking a list of elements into parts (pages, lines...).
 */
public class KnuthIterator<L extends Layout> {

    protected ActiveLayouts<L> layouts;

    private Iterator<KnuthElement> contentIter;

    protected LegalBreakHandler<L> legalBreakHandler;

    public KnuthIterator(List<KnuthElement> content,
            LegalBreakHandler<L> legalbreakHandler) {
        this.contentIter = content.iterator();
        this.legalBreakHandler = legalbreakHandler;
    }

    public void initialize(ActiveLayouts<L> previousLayouts) {
        this.layouts = previousLayouts;
    }

    public boolean hasNext() {
        return contentIter.hasNext();
    }

    public Collection<CompletedPart> next() {
        assert hasNext();
        do {
            KnuthElement e = contentIter.next();
            if (e.isBox() || e.isGlue()) {
                layouts.updateLayouts(e);
            } else if (e.isPenalty()) {
                Penalty p = (Penalty) e;
                if (p.getPenalty() < Penalty.INFINITE) {
                    return legalBreakHandler.considerBreak(p, layouts);
                }
            }
        } while (contentIter.hasNext());
        return null;
    }

}
