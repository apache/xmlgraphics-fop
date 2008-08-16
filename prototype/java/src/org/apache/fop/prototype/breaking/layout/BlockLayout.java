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

import org.apache.fop.prototype.breaking.PageDimensions;

/**
 * TODO javadoc
 */
public class BlockLayout extends StandardLayout {

    private PageDimensions partDims;

    /**
     * @param dimension
     */
    public BlockLayout(PageDimensions partDims) {
        super(partDims.getBpd());
        this.partDims = partDims;
    }

    /**
     * @param previous
     * @param alternatives
     * @param dimension
     */
    public BlockLayout(Layout previous, int partNumber, PageDimensions partDims) {
        super(previous, partNumber, partDims.getBpd());
        this.partDims = partDims;
    }

    /** {@inheritDoc} */
    @Override
    public int getIPD(int bpd) {
        return partDims.getIpd();
    }

}
