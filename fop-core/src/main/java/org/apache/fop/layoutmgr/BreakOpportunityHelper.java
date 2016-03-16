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

package org.apache.fop.layoutmgr;

import org.apache.fop.fo.Constants;
import org.apache.fop.fo.properties.BreakPropertySet;
import org.apache.fop.util.BreakUtil;

/**
 * Helper implementations of the {@link BreakOpportunity} methods.
 */
public final class BreakOpportunityHelper {

    private BreakOpportunityHelper() { }

    /**
     * Returns the break opportunity before the given layout manager. There is a break
     * opportunity if the LM's FO has the break-before property set, or if there is a
     * break opportunity before its first child LM.
     *
     * @return the break-before value (Constants.EN_*)
     */
    public static int getBreakBefore(AbstractLayoutManager layoutManager) {
        int breakBefore = Constants.EN_AUTO;
        if (layoutManager.getFObj() instanceof BreakPropertySet) {
            breakBefore = ((BreakPropertySet) layoutManager.getFObj()).getBreakBefore();
        }
        LayoutManager childLM = layoutManager.getChildLM();
        // It is assumed this is only called when the first LM is active.
        if (childLM instanceof BreakOpportunity) {
            BreakOpportunity bo = (BreakOpportunity) childLM;
            breakBefore = BreakUtil.compareBreakClasses(breakBefore, bo.getBreakBefore());
        }
        return breakBefore;
    }

}
