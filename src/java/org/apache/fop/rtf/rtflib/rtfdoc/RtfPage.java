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

import java.io.IOException;
import java.io.Writer;

/** Specifies rtf control words.  Is the container for page attributes.
 *  Overrides okToWriteRtf.
 *  @author Christopher Scott, scottc@westinghouse.com
 */

public class RtfPage
extends RtfContainer {
    private final RtfAttributes attrib;

    /**RtfPage attributes*/
    /** constant for page width */
    public static final String PAGE_WIDTH = "paperw";
    /** constant for page height */
    public static final String PAGE_HEIGHT = "paperh";

    /** constant for top margin */
    public static final String MARGIN_TOP = "margt";
    /** constant for bottom margin */
    public static final String MARGIN_BOTTOM = "margb";
    /** constant for left margin */
    public static final String MARGIN_LEFT = "margl";
    /** constant for right margin */
    public static final String MARGIN_RIGHT = "margr";

    /** String array of RtfPage attributes */
    public static final String[] PAGE_ATTR = new String[]{
        PAGE_WIDTH, PAGE_HEIGHT, MARGIN_TOP, MARGIN_BOTTOM,
        MARGIN_LEFT, MARGIN_RIGHT
    };

    /**    RtfPage creates new page attributes with the parent container, the writer
           and the attributes*/
    RtfPage(RtfPageArea parent, Writer w, RtfAttributes attrs) throws IOException {
        super((RtfContainer)parent, w);
        attrib = attrs;
    }

        /**
         * RtfPage writes the attributes the attributes contained in the string
         * PAGE_ATTR, if not null
         * @throws IOException for I/O problems
         */
        protected void writeRtfContent() throws IOException {
        writeAttributes(attrib, PAGE_ATTR);

        if (attrib != null) {
            Object widthRaw = attrib.getValue(PAGE_WIDTH);
            Object heightRaw = attrib.getValue(PAGE_HEIGHT);

            if ((widthRaw instanceof Integer) && (heightRaw instanceof Integer)
                    && ((Integer) widthRaw).intValue() > ((Integer) heightRaw).intValue()) {
                writeControlWord("landscape");
            }
        }
    }

    /**
     * RtfPage - attributes accessor
     * @return attributes
     */
    public RtfAttributes getAttributes() {
        return attrib;
    }

    /**
     * RtfPage - is overwritten here because page attributes have no content
     * only attributes. RtfContainer is defined not to write when empty.
     * Therefore must make this true to print.
     * @return true
     */
    protected boolean okToWriteRtf() {
        return true;
    }

}
