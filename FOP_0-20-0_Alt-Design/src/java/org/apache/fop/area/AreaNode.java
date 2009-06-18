/*
 *
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created on 1/05/2004
 * $Id$
 */
package org.apache.fop.area;

import java.util.logging.Logger;

import org.apache.fop.datastructs.Node;
import org.apache.fop.datastructs.SyncedNode;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * A pseudo-area base class for all areas.  It contains no actual area.
 * Sub-classed by <code>Page</code>, which maintains meta-information on
 * all areas within a page.
 * @author pbw
 * @version $Revision$ $Name$
 */
public abstract class AreaNode extends SyncedNode implements Cloneable {

    /** The page-sequence which generated this area. */
    protected FoPageSequence pageSeq = null;
    /** The FO node that generated this node. */
    protected FONode generatedBy = null;
    protected Logger log;

    private void setup(FoPageSequence pageSeq, FONode generatedBy) {
        this.pageSeq = pageSeq;
        this.generatedBy = generatedBy;
        log = generatedBy.getLogger();
    }
    
    /**
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param index of this in children of parent
     * @param sync the object on which this area is synchronized
     * @throws IndexOutOfBoundsException
     */
    public AreaNode(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            int index,
            Object sync)
        throws IndexOutOfBoundsException {
        super(parent, index, sync);
        setup(pageSeq, generatedBy);
    }

    /**
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param sync the object on which this area is synchronized
     * @throws IndexOutOfBoundsException
     */
    public AreaNode(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(parent, sync);
        setup(pageSeq, generatedBy);
    }

    /**
     * Construct an <code>AreaNode</code> which is the root of a tree, and is
     * synchronized on itself
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     */
    public AreaNode(
            FoPageSequence pageSeq,
            FONode generatedBy) {
        super();
        setup(pageSeq, generatedBy);
    }

    /**
     * @return the generatedBy
     */
    public FONode getGeneratedBy() {
        synchronized (sync) {
            return generatedBy;
        }
    }
    /**
     * @param generatedBy to set
     */
    public void setGeneratedBy(FONode generatedBy) {
        synchronized (sync) {
            this.generatedBy = generatedBy;
        }
    }
}
