/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.area.inline;

import org.apache.fop.render.Renderer;

public class Leader extends Stretch {

    // pattern, length min opt max

    // in the case of use content or dots this is replaced
    // with the set of inline areas
    // if space replaced with a space
    // otherwise this is a holder for a line

    public static final int DOTTED = 0;
    public static final int DASHED = 1;
    public static final int SOLID = 2;
    public static final int DOUBLE = 3;
    public static final int GROOVE = 4;
    public static final int RIDGE = 5;

    int ruleStyle = SOLID;
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
