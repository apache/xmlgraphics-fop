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

package org.apache.fop.prototype.breaking.layout;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.fop.prototype.knuth.Glue;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
public class Layout {

    protected Layout previous;

    /** The demerits represented by this layout plus all of its predecessors. */
    protected double demerits;

    protected ProgressInfo progress;

    protected Collection<Layout> alternatives;

    protected List<KnuthElement> elements;

    /** Creates an empty layout starting on part 0. */
    public Layout() {
        previous = null;
        demerits = 0;
        progress = new ProgressInfo();
        elements = new LinkedList<KnuthElement>();
    }

    /** Creates a deep copy of the given layout. */
    Layout(Layout other) {
        previous = other.previous;
        demerits = other.demerits;
        progress = new ProgressInfo(other.progress);
        if (other.alternatives != null) {
            alternatives = new LinkedList<Layout>(other.alternatives);
        }
        elements = new LinkedList<KnuthElement>(other.elements);
    }

    public static Layout createLayoutForNewPart(Layout previous, double demerits,
            Collection<Layout> alternatives) {
        Layout l = new Layout();
        l.previous = previous;
        l.demerits = demerits;
        l.progress.setPartNumber(previous.getProgress().getPartNumber() + 1);
        l.alternatives = alternatives;
        return l;
    }

    Layout copy() {
        return new Layout(this);
    }

    public Layout getPrevious() {
        return previous;
    }

    /** Returns the demerits represented by this layout plus all of its predecessors. */
    public double getDemerits() {
        return demerits;
    }

    public ProgressInfo getProgress() {
        return progress;
    }

    public Collection<Layout> getAlternatives() {
        return Collections.unmodifiableCollection(alternatives);
    }

    public List<KnuthElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public void addElement(KnuthElement e) {
        if (e.isBox() || !elements.isEmpty()) {
            if (e.isGlue()) {
                Glue g = (Glue) e;
                progress.add(g.getLength(), g.getStretch(), g.getShrink());
            } else {
                progress.add(e.getLength());
            }
            elements.add(e);
        }
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (KnuthElement e: elements) {
            s.append(e.getLabel());
            s.append('\n');
        }
        return s.toString();
    }
}
