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

import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.knuth.KnuthElement;


/**
 * TODO javadoc
 */
public class LineLayout extends StandardLayout {

    private Layout blockLayout;

    private class LineLayoutClass implements LayoutClass {

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LineLayoutClass)) {
                return false;
            } else {
                LineLayoutClass o = (LineLayoutClass) obj;
                return blockLayout.getLayoutClass().equals(o.getBlockLayout().getLayoutClass())
                        && blockLayout.getProgress().equals(o.getBlockLayout().getProgress());
            }
        }

        Layout getBlockLayout() {
            return blockLayout;
        }

        @Override
        public int hashCode() {
            return blockLayout.getLayoutClass().hashCode() + blockLayout.getProgress().hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return "(" + blockLayout.getLayoutClass().toString() + ")"
                    + blockLayout.getProgress().toString() + ","
                    + blockLayout.getProgress().getPartNumber();
        }
    }

    /**
     * Creates an empty layout starting on line 0.
     *
     * @param blockLayout the block-level layout preceding this one. That is, the layout
     * containing the elements preceding this line-level element.
     */
    public LineLayout(Layout blockLayout) {
        super(blockLayout.getIPD(blockLayout.getProgress().getTotalLength()));
        this.blockLayout = blockLayout;
        this.layoutClass = new LineLayoutClass();
    }

    /**
     * @param demerits
     * @param partNumber
     * @param dimension
     */
    public LineLayout(Layout blockLayout, int partNumber) {
        this(blockLayout);
        this.progress.setPartNumber(partNumber);
    }

    /** {@inheritDoc} */
    @Override
    public LineLayout clone(Collection<Alternative> alternatives) {
        throw new UnsupportedOperationException("clone does not apply to LineLayout");
    }

    public Layout getBlockLayout() {
        return blockLayout;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(blockLayout.toString());
        s.append('[');
        for (KnuthElement e: getElements()) {
            s.append(e.getLabel());
        }
        s.append(']');
        return s.toString();
    }

    @Override
    public double getDemerits() {
        return blockLayout.getDemerits(); // TODO really?
    }

    @Override
    public int getIPD(int bpd) {
        throw new UnsupportedOperationException();
    }

}
