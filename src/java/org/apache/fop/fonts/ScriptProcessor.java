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

package org.apache.fop.fonts;

import java.util.HashMap;
import java.util.Map;

// CSOFF: InnerAssignmentCheck
// CSOFF: LineLengthCheck

/**
 * Abstract script processor base class for which an implementation of the substitution and positioning methods
 * must be supplied.
 * @author Glenn Adams
 */
public abstract class ScriptProcessor {

    private final String script;

    private static Map processors = new HashMap();

    /**
     * Instantiate a script processor.
     * @param script a script identifier
     */
    protected ScriptProcessor ( String script ) {
        if ( ( script == null ) || ( script.length() == 0 ) ) {
            throw new IllegalArgumentException ( "script must be non-empty string" );
        } else {
            this.script = script;
        }
    }

    /** @return script identifier */
    public String getScript() {
        return script;
    }

    /**
     * Perform substitution processing using a specific set of lookup tables.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param lookups a mapping from lookup specifications to glyph subtables to use for substitution processing
     * @return the substituted (output) glyph sequence
     */
    public abstract GlyphSequence substitute ( GlyphSequence gs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups );

    /**
     * Perform positioning processing using a specific set of lookup tables.
     * @param gs an input glyph sequence
     * @param script a script identifier
     * @param language a language identifier
     * @param lookups a mapping from lookup specifications to glyph subtables to use for positioning processing
     * @return the substituted (output) glyph sequence
     */
    public abstract int[] position ( GlyphSequence gs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups );

    /**
     * Obtain script processor instance associated with specified script.
     * @param script a script identifier
     * @return a script processor instance or null if none found
     */
    public static synchronized ScriptProcessor getInstance ( String script ) {
        ScriptProcessor sp = null;
        assert processors != null;
        if ( ( sp = (ScriptProcessor) processors.get ( script ) ) == null ) {
            processors.put ( script, sp = createProcessor ( script ) );
        }
        return sp;
    }

    // [TBD] - rework to provide more configurable binding between script name and script processor constructor
    private static ScriptProcessor createProcessor ( String script ) {
        ScriptProcessor sp = null;
        if ( "arab".equals ( script ) ) {
            sp = new ArabicScriptProcessor ( script );
        } else {
            sp = new DefaultScriptProcessor ( script );
        }
        return sp;
    }

}
