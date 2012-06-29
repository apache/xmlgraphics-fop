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

package org.apache.fop.svg;

import org.apache.batik.gvt.TextPainter;

/**
 * <p>Bridge class for the &lt;text> element.
 * This bridge will use the direct text painter if the text
 * for the element is simple.</p>
 *
 * <p>This work was authored by Keiron Liddle (keiron@aftexsw.com).</p>
 */
public class PDFTextElementBridge extends AbstractFOPTextElementBridge {

    /**
     * Constructs a new bridge for the &lt;text> element.
     *
     * @param textPainter the text painter to use
     */
    public PDFTextElementBridge(TextPainter textPainter) {
        super(textPainter);
    }
}

