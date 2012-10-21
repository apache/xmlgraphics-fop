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

package org.apache.fop.area;

/**
 * The normal-flow-reference-area class.
 * Each span-reference-area contains one or more of these objects
 * See fo:region-body definition in the XSL Rec for more information.
 */
public class NormalFlow extends BlockParent {

    private static final long serialVersionUID = -3753538631016929004L;

    /**
     * Constructor.
     * @param ipd of Normal flow object
     */
    public NormalFlow(int ipd) {
        addTrait(Trait.IS_REFERENCE_AREA, Boolean.TRUE);
        setIPD(ipd);
    }

    /** {@inheritDoc} */
    public void addBlock(Block block) {
        super.addBlock(block);
        if (block.isStacked()) {
            bpd += block.getAllocBPD();
        }
    }
}

