/*
   Copyright 2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * Created on 26/01/2004
 * $Id$
 */
package org.apache.fop.area;

import org.apache.fop.datastructs.Node;
import org.apache.fop.datastructs.SyncedNode;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.flow.FoPageSequence;

/**
 * The base class for all areas.  <code>Area</code> extends <code>Node</code>
 * because all areas will find themselves in a tree of some kind.
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Area extends SyncedNode implements Cloneable  {

    /** The page-sequence which generated this area. */
    protected FoPageSequence pageSeq = null;
    /** The FO node that generated this node. */
    protected FONode generatedBy = null;
    /** Current inline progression dimension.  May be unknown. */
    protected Integer iPDim = null;
    /** Maximum required inline progression dimension.  May be unknown. */
    protected Integer iPDimMax = null;
    /** Mimimum required inline progression dimension.  May be unknown. */
    protected Integer iPDimMin = null;
    /** Current block progression dimension.  May be unknown. */
    protected Integer bPDim = null;
    /** Maximum required block progression dimension.  May be unknown. */
    protected Integer bPDimMax = null;
    /** Mimimum required block progression dimension.  May be unknown. */
    protected Integer bPDimMin = null;
    
    /**
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param index of this in children of parent
     * @param sync the object on which this area is synchronized
     * @throws IndexOutOfBoundsException
     */
    public Area(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            int index,
            Object sync)
        throws IndexOutOfBoundsException {
        super(parent, index, sync);
        this.pageSeq = pageSeq;
        this.generatedBy = generatedBy;
    }

    /**
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     * @param parent <code>Node</code> of this
     * @param sync the object on which this area is synchronized
     * @throws IndexOutOfBoundsException
     */
    public Area(
            FoPageSequence pageSeq,
            FONode generatedBy,
            Node parent,
            Object sync) {
        super(parent, sync);
        this.pageSeq = pageSeq;
        this.generatedBy = generatedBy;
    }

    /**
     * Construct an <code>Area</code> which is the root of a tree, and is
     * synchronized on itself
     * @param pageSeq through which this area was generated
     * @param generatedBy the given <code>FONode</code> generated this
     */
    public Area(
            FoPageSequence pageSeq,
            FONode generatedBy) {
        super();
        this.pageSeq = pageSeq;
        this.generatedBy = generatedBy;
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
    /**
     * @return the bPDim
     */
    public Integer getBPDim() {
        synchronized (sync) {
            return bPDim;
        }
    }

    /**
     * @param dim to set
     */
    public void setBPDim(Integer dim) {
        synchronized (sync) {
            bPDim = dim;
        }
    }

    /**
     * @return the bPDimMax
     */
    public Integer getBPDimMax() {
        synchronized (sync) {
            return bPDimMax;
        }
    }

    /**
     * @param dimMax to set
     */
    public void setBPDimMax(Integer dimMax) {
        synchronized (sync) {
            bPDimMax = dimMax;
        }
    }

    /**
     * @return the bPDimMin
     */
    public Integer getBPDimMin() {
        synchronized (sync) {
            return bPDimMin;
        }
    }

    /**
     * @param dimMin to set
     */
    public void setBPDimMin(Integer dimMin) {
        synchronized (sync) {
            bPDimMin = dimMin;
        }
    }

    /**
     * @return the iPDim
     */
    public Integer getIPDim() {
        synchronized (sync) {
            return iPDim;
        }
    }

    /**
     * @param dim to set
     */
    public void setIPDim(Integer dim) {
        synchronized (sync) {
            iPDim = dim;
        }
    }

    /**
     * @return the iPDimMax
     */
    public Integer getIPDimMax() {
        synchronized(sync) {
            return iPDimMax;
        }
    }

    /**
     * @param dimMax to set
     */
    public void setIPDimMax(Integer dimMax) {
        synchronized (sync) {
            iPDimMax = dimMax;
        }
    }

    /**
     * @return the iPDimMin
     */
    public Integer getIPDimMin() {
        synchronized (sync) {
            return iPDimMin;
        }
    }

    /**
     * @param dimMin to set
     */
    public void setIPDimMin(Integer dimMin) {
        synchronized (sync) {
            iPDimMin = dimMin;
        }
    }

}
