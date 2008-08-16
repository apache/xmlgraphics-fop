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

import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.knuth.Glue;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
public class StandardLayout implements Layout, Cloneable {

    private class StandardLayoutClass implements LayoutClass {

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StandardLayoutClass)) {
                return false;
            } else {
                StandardLayoutClass o = (StandardLayoutClass) obj;
                return progress.getPartNumber() == o.getProgress().getPartNumber();
            }
        }

        private ProgressInfo getProgress() {
            return progress;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return progress.getPartNumber();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "Page " + progress.getPartNumber();
        }

    }

    protected Layout previous;

    protected LayoutClass layoutClass = new StandardLayoutClass();

    /** The demerits represented by this layout plus all of its predecessors. */
    protected double demerits;

    protected ProgressInfo progress;

    protected Collection<Alternative> alternatives;

    protected List<KnuthElement> elements;

    /** Dimension (ipd or bpd) of the part this layout fits in. */
    protected int dimension;

    private boolean isPage;

    /** Creates an empty layout starting on part 0. */
    StandardLayout(int dimension) {
        progress = new ProgressInfo();
        elements = new LinkedList<KnuthElement>();
        this.dimension = dimension;
    }

    StandardLayout(Layout previous, int partNumber, int dimension) {
        this(dimension);
        this.previous = previous;
        this.demerits = previous.getDemerits();
        this.progress.setPartNumber(partNumber);
    }

    /** {@inheritDoc} */
    @Override
    public StandardLayout clone(Collection<Alternative> alternatives) {
        try {
            StandardLayout c = (StandardLayout) super.clone();
            c.layoutClass = c.new StandardLayoutClass();
            c.progress = new ProgressInfo(progress);
            c.alternatives = alternatives;
            c.elements = new LinkedList<KnuthElement>(elements);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Layout getPrevious() {
        return previous;
    }

    @Override
    public void setPrevious(Layout previous) {
        this.previous = previous;
    }

    /** {@inheritDoc} */
    @Override
    public double getDemerits() {
        return demerits;
    }

    @Override
    public void setDemerits(double demerits) {
        this.demerits = demerits;
    }

    /** {@inheritDoc} */
    @Override
    public ProgressInfo getProgress() {
        return progress;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<Alternative> getAlternatives() {
        if (alternatives == null) {
            return null;
        } else {
            return Collections.unmodifiableCollection(alternatives);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<KnuthElement> getElements() {
        return Collections.unmodifiableList(elements);
    }

    /** {@inheritDoc} */
    @Override
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

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getProgress()).append('\n');
        for (KnuthElement e: elements) {
            s.append(e.getLabel());
            s.append('\n');
        }
        return s.toString();
    }

    @Override
    public int getDifference() {
        return dimension - progress.getTotalLength();
    }

    @Override
    public int getIPD(int bpd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public LayoutClass getLayoutClass() {
        return layoutClass;
    }

    @Override
    public boolean isPage() {
        return isPage;
    }

    @Override
    public void setPage() {
        isPage = true;
    }
}
