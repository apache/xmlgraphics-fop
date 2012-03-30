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

package org.apache.fop.render.ps.fonts;

import java.io.IOException;

import org.apache.fop.fonts.truetype.TTFGlyphOutputStream;
import org.apache.fop.fonts.truetype.TTFOutputStream;
import org.apache.fop.fonts.truetype.TTFTableOutputStream;
import org.apache.xmlgraphics.ps.PSGenerator;

/**
 * Implements TTFOutputStream and streams font tables to a PostScript file.
 */
public class PSTTFOutputStream implements TTFOutputStream {
    /** The wrapper class for PSGenerator */
    private final PSTTFGenerator ttfGen;

    /**
     * Constructor - assigns a PSGenerator to stream the font.
     * @param gen PSGenerator.
     */
    public PSTTFOutputStream(PSGenerator gen) {
        this.ttfGen = new PSTTFGenerator(gen);
    }

    /** {@inheritDoc} */
    public void startFontStream() throws IOException {
        ttfGen.write("/sfnts[");
    }

    /** {@inheritDoc} */
    public TTFTableOutputStream getTableOutputStream() {
        return new PSTTFTableOutputStream(ttfGen);
    }

    /** {@inheritDoc} */
    public TTFGlyphOutputStream getGlyphOutputStream() {
        return new PSTTFGlyphOutputStream(ttfGen);
    }

    /** {@inheritDoc} */
    public void endFontStream() throws IOException {
        ttfGen.writeln("] def");
    }

}
