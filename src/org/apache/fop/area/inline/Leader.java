/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.fo.properties.RuleStyle;

public class Leader extends Stretch {

    // pattern, length min opt max

    // in the case of use content or dots this is replaced
    // with the set of inline areas
    // if space replaced with a space
    // otherwise this is a holder for a line

    int ruleStyle = RuleStyle.SOLID;
    int ruleThickness = 1000;

    public Leader() {

    }

    public void setRuleStyle(int style) {
        ruleStyle = style;
    }

    public void setRuleThickness(int rt) {
        ruleThickness = rt;
    }

    public int getRuleStyle() {
        return ruleStyle;
    }

    public int getRuleThickness() {
        return ruleThickness;
    }

    public void render(Renderer renderer) {
        renderer.renderLeader(this);
    }
}
