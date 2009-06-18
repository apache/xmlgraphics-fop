/*
 * Copyright 1999-2004,2006 The Apache Software Foundation.
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
     * Set the rule style of this leader area.
     * @param style the rule style for the leader area (XSL enum values)
     */
    public void setRuleStyle(String style) {
        if ("dotted".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_DOTTED);
        } else if ("dashed".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_DASHED);
        } else if ("solid".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_SOLID);
        } else if ("double".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_DOUBLE);
        } else if ("groove".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_GROOVE);
        } else if ("ridge".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_RIDGE);
        } else if ("none".equalsIgnoreCase(style)) {
            setRuleStyle(Constants.EN_NONE);
        }
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

    /** @return the rule style as string */
    public String getRuleStyleAsString() {
        switch (getRuleStyle()) {
        case Constants.EN_DOTTED: return "dotted";
        case Constants.EN_DASHED: return "dashed";
        case Constants.EN_SOLID: return "solid";
        case Constants.EN_DOUBLE: return "double";
        case Constants.EN_GROOVE: return "groove";
        case Constants.EN_RIDGE: return "ridge";
        case Constants.EN_NONE: return "none";
        default:
            throw new IllegalStateException("Unsupported rule style: " + getRuleStyle());
        }
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

