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

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

package org.apache.fop.rtf.rtflib.rtfdoc;

import java.util.Iterator;
import java.util.StringTokenizer;

/**  Collapses whitespace of an RtfContainer that contains RtfText elements
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

class WhitespaceCollapser {
    private static final String SPACE = " ";
    private boolean lastEndSpace = true;

    /** remove extra whitespace in RtfText elements that are inside c */
    WhitespaceCollapser(RtfContainer c) {
        // process all texts
        for (Iterator it = c.getChildren().iterator(); it.hasNext();) {
            RtfText current = null;
            final Object kid = it.next();
            if (kid instanceof RtfText) {
                current = (RtfText)kid;
                processText(current);
            } else {
                // if there is something between two texts, it counts for a space
                lastEndSpace = true;
            }
        }
    }

    /** process one RtfText from our container */
    private void processText(RtfText txt) {
        final String orig = txt.getText();

        // tokenize the text based on whitespace and regenerate it so as
        // to collapse multiple spaces into one
        if (orig != null && orig.length() > 0) {
            final boolean allSpaces = orig.trim().length() == 0;
            final boolean endSpace = allSpaces
                                     || Character.isWhitespace(orig.charAt(orig.length() - 1));
            final boolean beginSpace = Character.isWhitespace(orig.charAt(0));
            final StringBuffer sb = new StringBuffer(orig.length());

            // if text contains spaces only, keep at most one
            if (allSpaces) {
                if (!lastEndSpace) {
                    sb.append(SPACE);
                }
            } else {
                // TODO to be compatible with different Locales, should use Character.isWhitespace
                // instead of this limited list
                boolean first = true;
                final StringTokenizer stk = new StringTokenizer(txt.getText(), " \t\n\r");
                while (stk.hasMoreTokens()) {
                    if (first && beginSpace && !lastEndSpace) {
                        sb.append(SPACE);
                    }
                    first = false;

                    sb.append(stk.nextToken());
                    if (stk.hasMoreTokens() || endSpace) {
                        sb.append(SPACE);
                    }
                }
            }

            txt.setText(sb.toString());
            lastEndSpace = endSpace;
        }
    }
}