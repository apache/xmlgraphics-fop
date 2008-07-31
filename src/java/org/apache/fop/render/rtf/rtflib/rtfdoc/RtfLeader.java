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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

/**
 * Generates the leader in RTF.
 */
public class RtfLeader extends RtfContainer {

    /*
     * Format : \tqr \style \tx## { \pard \format \tab }
     *  ## represents the width \style represents the style (tldot, tlth, ...)
     * \format represents standard formats (color, fontsize, ...)
     *
     *
     * \pard \zwnj {\fsxx <format> } \zwnj
     *
     * <format>: \\ulcN Underline color. \\uld Dotted underline. \\uldash Dashed
     * underline. \\uldashd Dash-dotted underline. \\uldashdd Dash-dot-dotted
     * underline. \\uldb Double underline. \\ulhwave Heavy wave underline.
     * \\ulldash Long dashed underline. \\ulnone Stops all underlining. \\ulth
     * Thick underline. \\ulthd Thick dotted underline. \\ulthdash Thick dashed
     * underline. \\ulthdashd Thick dash-dotted underline. \\ulthdashdd Thick
     * dash-dot-dotted underline. \\ulthldash Thick long dashed underline.
     * \\ululdbwave Double wave underline.
     */

    private RtfAttributes attrs = null;

    /** Private attribute: tab style */
    public static final String LEADER_TABLEAD = "tablead";

    /** Private attribute: tab usage indicator */
    public static final String LEADER_USETAB = "tabuse";

    /** Private attribute: leader width */
    public static final String LEADER_WIDTH = "lwidth";

    // +++++++++++++++ Styles Underline ++++++++++++++++++++++

    /** Dotted underline */
    public static final String LEADER_DOTTED = "uld"; // dotted

    /** Dashed underline */
    public static final String LEADER_MIDDLEDOTTED = "uldash"; // dashed

    /** Heavy wave underline */
    public static final String LEADER_HYPHENS = "ulhwave"; // groove

    /** Dash-dot-dotted underline */
    public static final String LEADER_UNDERLINE = "ulthdashdd"; // ridge

    /** Double underline */
    public static final String LEADER_EQUAL = "uldb"; // double

    /** Thick underline */
    public static final String LEADER_THICK = "ulth"; // solid

    // +++++++++++++++ Styles Tabulator +++++++++++++++++++++++

    /** Leader dots */
    public static final String LEADER_TAB_DOTTED = "tldot"; // dotted

    /** Leader middle dots */
    public static final String LEADER_TAB_MIDDLEDOTTED = "tlmdot"; // dashed

    /** Leader hyphens */
    public static final String LEADER_TAB_HYPHENS = "tlhyph"; // groove

    /** Leader underline */
    public static final String LEADER_TAB_UNDERLINE = "tlul"; // ridge

    /** Leader equal sign */
    public static final String LEADER_TAB_EQUAL = "tleq"; // double

    /** Leader thick line */
    public static final String LEADER_TAB_THICK = "tlth"; // solid

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /** Resets to default properties */
    public static final String LEADER_IGNORE_STYLE = "pard";

    /** Font size determines rule thickness */
    public static final String LEADER_RULE_THICKNESS = "fs"; // thickness = fontsize

    /** Expansion or compression of the space between characters in twips */
    public static final String LEADER_PATTERN_WIDTH = "expndtw";

    /** Zero-width break opportunity */
    public static final String LEADER_ZERO_WIDTH = "zwbo";

    /** Standard leader width */
    public static final int LEADER_STANDARD_WIDTH = 30;

    /** Move up 4 half-points */
    public static final String LEADER_UP = "up4";

    /** Negative expansion */
    public static final String LEADER_EXPAND = "expnd-2"; // negative value
                                                            // for compression

    /** Tab */
    public static final String LEADER_TAB_VALUE = "tab";

    /** Right-aligned tab */
    public static final String LEADER_TAB_RIGHT = "tqr";

    /** Tab width */
    public static final String LEADER_TAB_WIDTH = "tx";

    RtfLeader(RtfContainer parent, Writer w, RtfAttributes attrs) throws IOException {
        super(parent, w);
        this.attrs = attrs;
    }

    /** {@inheritDoc} */
    protected void writeRtfContent() throws IOException {

        int thickness = LEADER_STANDARD_WIDTH;
        String tablead = null;
        String tabwidth = null;
        for (Iterator it = attrs.nameIterator(); it.hasNext();) {
            final String name = (String)it.next();
            if (attrs.isSet(name)) {
                if (name.equals(LEADER_TABLEAD)) {
                    tablead = attrs.getValue(LEADER_TABLEAD).toString();
                } else if (name.equals(LEADER_WIDTH)) {
                    tabwidth = attrs.getValue(LEADER_WIDTH).toString();
                }
            }
        }

        if (attrs.getValue(LEADER_RULE_THICKNESS) != null) {
            thickness += Integer.parseInt(attrs.getValue(LEADER_RULE_THICKNESS).toString())
                            / 1000 * 2;
            attrs.unset(LEADER_RULE_THICKNESS);
        }

        //Remove private attributes
        attrs.unset(LEADER_WIDTH);
        attrs.unset(LEADER_TABLEAD);

        // If leader is 100% we use a tabulator, because its more
        // comfortable, specially for the table of content
        if (attrs.getValue(LEADER_USETAB) != null) {
            attrs.unset(LEADER_USETAB);
            writeControlWord(LEADER_TAB_RIGHT);

            if (tablead != null) {
                writeControlWord(tablead);
            }
            writeControlWord(LEADER_TAB_WIDTH + tabwidth);

            writeGroupMark(true);

            writeControlWord(LEADER_IGNORE_STYLE);
            writeAttributes(attrs, null);
            writeControlWord(LEADER_EXPAND);
            writeControlWord(LEADER_TAB_VALUE);

            writeGroupMark(false);

        }
        // Using white spaces with different underline formats
        else {
            writeControlWord(LEADER_IGNORE_STYLE);
            writeControlWord(LEADER_ZERO_WIDTH);
            writeGroupMark(true);

            writeControlWord(LEADER_RULE_THICKNESS + thickness);

            writeControlWord(LEADER_UP);

            super.writeAttributes(attrs, null);
            if (tablead != null) {
                writeControlWord(tablead);
            }

            // Calculation for the necessary amount of white spaces
            // Depending on font-size 15 -> 1cm = 7,5 spaces
            // TODO for rule-thickness this has to be done better

            for (double d = (Integer.parseInt(tabwidth) / 560) * 7.5; d >= 1; d--) {
                RtfStringConverter.getInstance().writeRtfString(writer, " ");
            }

            writeGroupMark(false);
            writeControlWord(LEADER_ZERO_WIDTH);
        }
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return false;
    }

}
