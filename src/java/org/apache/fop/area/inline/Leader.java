/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.area.inline;

import org.apache.fop.fo.Constants;

/**
 * This is a leader inline area.
 * This class is only used for leader with leader-pattern of rule.
 */
public class Leader extends InlineArea {

    // in the case of use content or dots this is replaced
    // with the set of inline areas
    // if space replaced with a space
    // otherwise this is a holder for a line

    private int ruleStyle = Constants.EN_SOLID;
    private int ruleThickness = 1000;

    /**
     * Create a new leader area.
     */
    public Leader() {
    }

    /**
     * Set the rule style of this leader area.
     *
     * @param style the rule style for the leader line
     */
    public void setRuleStyle(int style) {
        ruleStyle = style;
    }

    /**
     * Set the rule thickness of the rule in miilipoints.
     *
     * @param rt the rule thickness in millipoints
     */
    public void setRuleThickness(int rt) {
        ruleThickness = rt;
    }

    /**
     * Get the rule style of this leader.
     *
     * @return the rule style
     */
    public int getRuleStyle() {
        return ruleStyle;
    }

    /**
     * Get the rule thickness of the rule in miilipoints.
     *
     * @return the rule thickness in millipoints
     */
    public int getRuleThickness() {
        return ruleThickness;
    }

}

