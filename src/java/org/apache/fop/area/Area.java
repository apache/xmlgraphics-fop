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

/**
 * @author pbw
 * @version $Revision$ $Name$
 */
/**
 * @author pbw
 * @version $Revision$ $Name$
 */
/**
 * The base class for all areas.  <code>Area</code> extends <code>Node</code>
 * because all areas will find themselves in a tree of some kind.
 * @author pbw
 * @version $Revision$ $Name$
 */
public class Area extends SyncedNode implements Cloneable  {

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
     * @param parent <code>Node</code> of this
     * @param index of this in children of parent
     * @throws IndexOutOfBoundsException
     */
    public Area(Node parent, int index, Object areaSync)
        throws IndexOutOfBoundsException {
        super(parent, index, areaSync);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param parent <code>Node</code> of this
     * @throws IndexOutOfBoundsException
     */
    public Area(Node parent, Object areaSync)
        throws IndexOutOfBoundsException {
        super(parent, areaSync);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return the bPDim
     */
    public Integer getBPDim() {
        return bPDim;
    }

    /**
     * @param dim to set
     */
    public void setBPDim(Integer dim) {
        bPDim = dim;
    }

    /**
     * @return the bPDimMax
     */
    public Integer getBPDimMax() {
        return bPDimMax;
    }

    /**
     * @param dimMax to set
     */
    public void setBPDimMax(Integer dimMax) {
        bPDimMax = dimMax;
    }

    /**
     * @return the bPDimMin
     */
    public Integer getBPDimMin() {
        return bPDimMin;
    }

    /**
     * @param dimMin to set
     */
    public void setBPDimMin(Integer dimMin) {
        bPDimMin = dimMin;
    }

    /**
     * @return the iPDim
     */
    public Integer getIPDim() {
        return iPDim;
    }

    /**
     * @param dim to set
     */
    public void setIPDim(Integer dim) {
        iPDim = dim;
    }

    /**
     * @return the iPDimMax
     */
    public Integer getIPDimMax() {
        return iPDimMax;
    }

    /**
     * @param dimMax to set
     */
    public void setIPDimMax(Integer dimMax) {
        iPDimMax = dimMax;
    }

    /**
     * @return the iPDimMin
     */
    public Integer getIPDimMin() {
        return iPDimMin;
    }

    /**
     * @param dimMin to set
     */
    public void setIPDimMin(Integer dimMin) {
        iPDimMin = dimMin;
    }

}
