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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

/*
 * This file is part of the RTF library of the FOP project, which was originally
 * created by Bertrand Delacretaz <bdelacretaz@codeconsult.ch> and by other
 * contributors to the jfor project (www.jfor.org), who agreed to donate jfor to
 * the FOP project.
 */

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * <p>Collapses whitespace of an RtfContainer that contains RtfText elements.</p>
 *
 * <p>This work was authored by Bertrand Delacretaz (bdelacretaz@codeconsult.ch).</p>
 */

final class WhitespaceCollapser {

    private static final String SPACE = " ";
    private boolean lastEndSpace = true;

    /**
     * Remove extra whitespace in RtfText elements that are inside container.
     * @param c the container
     */
    WhitespaceCollapser(RtfContainer c) {
        // process all texts
        for (Iterator it = c.getChildren().iterator(); it.hasNext();) {
            final Object kid = it.next();
            if (kid instanceof RtfText) {
                RtfText current = (RtfText)kid;
                processText(current);
            } else if (kid instanceof RtfString) {
                RtfString current = (RtfString)kid;
                processString(current);
            } else {
                // if there is something between two texts, it counts for a space
                lastEndSpace = true;
            }
        }
    }

    /** @return last end space */
    public boolean getLastEndSpace() {
        return lastEndSpace;
    }

    /** process one RtfText from our container */
    private void processText(RtfText txt) {
        final String newString = processString(txt.getText());
        if (newString != null) {
            txt.setText(newString);
        }
    }

    /** process one RtfString from our container */
    private void processString(RtfString txt) {
        final String newString = processString(txt.getText());
        if (newString != null) {
            txt.setText(newString);
        }
    }

    /** process one String */
    private String processString(String txt) {
        final String orig = txt;

        // tokenize the text based on whitespace and regenerate it so as
        // to collapse multiple spaces into one
        if (orig == null) {
            return null;
        } else if (orig.length() > 0) {
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
                final StringTokenizer stk = new StringTokenizer(txt, " \t\n\r");
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

            lastEndSpace = endSpace;
            return sb.toString();
        } else {
            return "";
        }
    }
}
