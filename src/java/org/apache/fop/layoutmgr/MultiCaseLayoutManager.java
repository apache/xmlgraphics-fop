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

package org.apache.fop.layoutmgr;

import org.apache.fop.area.Area;
import org.apache.fop.fo.FObj;

public class MultiCaseLayoutManager extends BlockStackingLayoutManager {

    public MultiCaseLayoutManager(FObj node) {
        super(node);
    }

    @Override
    public Keep getKeepTogether() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Keep getKeepWithNext() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Keep getKeepWithPrevious() {
        return Keep.KEEP_AUTO;
    }

    @Override
    public Area getParentArea(Area childArea) {
        return parentLayoutManager.getParentArea(childArea);
    }

    @Override
    public void addChildArea(Area childArea) {
        parentLayoutManager.addChildArea(childArea);
    }

    /** {@inheritDoc} */
    @Override
    public void addAreas(PositionIterator posIter, LayoutContext context) {
        AreaAdditionUtil.addAreas(this, posIter, context);
        flush();
    }

}
