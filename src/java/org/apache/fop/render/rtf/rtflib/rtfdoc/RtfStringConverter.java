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

package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.Writer;

/**  Converts java Strings according to RTF conventions
 *  @author Bertrand Delacretaz bdelacretaz@codeconsult.ch
 */

public class RtfStringConverter {
    private static final RtfStringConverter INSTANCE = new RtfStringConverter();

    private static final Map SPECIAL_CHARS;
    private static final Character DBLQUOTE = new Character('\"');
    private static final Character QUOTE = new Character('\'');
    private static final Character SPACE = new Character(' ');

    /** List of characters to escape with corresponding replacement strings */
    static {
        SPECIAL_CHARS = new HashMap();
        SPECIAL_CHARS.put(new Character('\t'), "tab");
        SPECIAL_CHARS.put(new Character('\n'), "line");
        SPECIAL_CHARS.put(new Character('\''), "rquote");
        SPECIAL_CHARS.put(new Character('\"'), "rdblquote");
        SPECIAL_CHARS.put(new Character('\\'), "\\");
        SPECIAL_CHARS.put(new Character('{'), "{");
        SPECIAL_CHARS.put(new Character('}'), "}");
    }

    /** singleton pattern */
    private RtfStringConverter() {
    }

    /**
     * use this to get an object of this class
     * @return the singleton instance
     */
    public static RtfStringConverter getInstance() {
        return INSTANCE;
    }

    /**
     * Write given String to given Writer, converting characters as required by
     * RTF spec
     * @param w Writer
     * @param str String to be written
     * @throws IOException for I/O problems
     */
    public void writeRtfString(Writer w, String str) throws IOException {
        if (str == null) {
            return;
        }

        // TODO: could be made more efficient (binary lookup, etc.)
        for (int i = 0; i < str.length(); i++) {
            final Character c = new Character(str.charAt(i));
            Character d;
            String replacement;
            if (i != 0) {
                d = new Character(str.charAt(i - 1));
            } else {
                d = new Character(str.charAt(i));
            }

            //This section modified by Chris Scott
            //add "smart" quote recognition
            if (c.equals((Object)DBLQUOTE) && d.equals((Object)SPACE)) {
                replacement = "ldblquote";
            } else if (c.equals((Object)QUOTE) && d.equals((Object)SPACE)) {
                replacement = "lquote";
            } else {
                replacement = (String)SPECIAL_CHARS.get(c);
            }

            if (replacement != null) {
                // RTF-escaped char
                w.write('\\');
                w.write(replacement);
                w.write(' ');
            } else if (c.charValue() > 127) {
                // write unicode representation - contributed by Michel Jacobson
                // <jacobson@idf.ext.jussieu.fr>
                w.write("\\u");
                w.write(Integer.toString((int)c.charValue()));
                w.write("\\\'3f");
            } else {
                // plain char that is understood by RTF natively
                w.write(c.charValue());
            }
        }
    }

}
