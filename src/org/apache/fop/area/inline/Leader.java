/*
 * $Id$
 * Copyright (C) 2001-2002 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.fo.properties.RuleStyle;

/**
 * This is a leader inline area.
 * This class is only used for leader with leader-pattern of rule.
 */
public class Leader extends InlineArea {

    // in the case of use content or dots this is replaced
    // with the set of inline areas
    // if space replaced with a space
    // otherwise this is a holder for a line

    private int ruleStyle = RuleStyle.SOLID;
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

    /**
     * Render this leader in the current renderer.
     *
     * @param renderer the renderer to render this inline area
     */
    public void render(Renderer renderer) {
        renderer.renderLeader(this);
    }
}

