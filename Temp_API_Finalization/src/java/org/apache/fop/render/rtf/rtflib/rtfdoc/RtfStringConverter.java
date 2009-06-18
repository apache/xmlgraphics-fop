/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
