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

import java.util.Map;

// CSOFF: LineLengthCheck

/**
 * Default script processor, which performs the identity substitution and produces no positioning information.
 * @author Glenn Adams
 */
public class DefaultScriptProcessor extends ScriptProcessor {

    DefaultScriptProcessor ( String script ) {
        super ( script );
    }

    /** {@inheritDoc} */
    public GlyphSequence substitute ( GlyphSequence gs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups ) {
        return gs;
    }

    /** {@inheritDoc} */
    public int[] position ( GlyphSequence cs, String script, String language, Map/*<LookupSpec,GlyphSubtable[]>*/ lookups ) {
        return null;
    }

}
