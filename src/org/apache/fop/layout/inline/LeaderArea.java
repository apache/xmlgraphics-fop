/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 *
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */
package org.apache.fop.layout.inline;

import org.apache.fop.render.Renderer;
import org.apache.fop.layout.FontState;

public class LeaderArea extends InlineArea {

    int ruleThickness;
    int leaderLengthOptimum;
    int leaderPattern;
    int ruleStyle;

    float red, green, blue;
    public LeaderArea(FontState fontState, float red, float green,
                      float blue, String text, int leaderLengthOptimum,
                      int leaderPattern, int ruleThickness, int ruleStyle) {
        super(fontState, leaderLengthOptimum, red, green, blue);

        this.leaderPattern = leaderPattern;
        this.leaderLengthOptimum = leaderLengthOptimum;
        this.ruleStyle = ruleStyle;
        // following the xsl spec rule: if rule-style="none" set thickness to 0;
        // actually in pdf this doesn't work, because a very thin line is still shown
        // this is handled in the pdf renderer
        if (ruleStyle == org.apache.fop.fo.properties.RuleStyle.NONE) {
            ruleThickness = 0;
        }
        this.ruleThickness = ruleThickness;
    }

    public void render(Renderer renderer) {
        renderer.renderLeaderArea(this);
    }

    public int getRuleThickness() {
        return this.ruleThickness;
    }

    public int getRuleStyle() {
        return this.ruleStyle;
    }

    public int getLeaderPattern() {
        return this.leaderPattern;
    }

    public int getLeaderLength() {
        return this.contentRectangleWidth;
    }



}
