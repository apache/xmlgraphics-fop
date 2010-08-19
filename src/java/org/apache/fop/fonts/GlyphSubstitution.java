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

// CSOFF: LineLengthCheck

/**
 * The <code>GlyphSubstitution</code> interface is implemented by a font related object
 * that supports the determination of glyph substitution information based on script and
 * language of the corresponding character content.
 * @author Glenn Adams
 */
public interface GlyphSubstitution {

    /**
     * Perform glyph substitutions. If no substitution applies, then returns the unmodified input sequence.
     * @param gs sequence to map to output glyph sequence
     * @param script the script associated with the characters corresponding to the glyph sequence
     * @param language the language associated with the characters corresponding to the glyph sequence
     * @return resulting glyph sequence, where each 'glyph' in the returned sequence has been mapped
     * (or not) by substitution
     */
    GlyphSequence substitute ( GlyphSequence gs, String script, String language );

}
