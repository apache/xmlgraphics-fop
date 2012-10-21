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

import org.apache.batik.extension.svg.BatikFlowTextElementBridge;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
import org.apache.fop.fonts.FontInfo;

/**
 * Element Bridge for Batik's flow text extension, so those texts can be painted using
 * PDF primitives.
 */
public class PDFBatikFlowTextElementBridge extends BatikFlowTextElementBridge {

    private PDFTextPainter textPainter;
    
    /**
     * Main Constructor.
     * @param fontInfo the font directory
     */
    public PDFBatikFlowTextElementBridge(FontInfo fontInfo) {
        this.textPainter = new PDFFlowExtTextPainter(fontInfo);
    }
    
    /** {@inheritDoc} */
    protected GraphicsNode instantiateGraphicsNode() {
        GraphicsNode node = super.instantiateGraphicsNode();
        if (node != null) {
            //Set our own text painter
            ((TextNode)node).setTextPainter(getTextPainter());
        }
        return node;
    }

    /**
     * Returns the text painter used by this bridge.
     * @return the text painter
     */
    public TextPainter getTextPainter() {
        return this.textPainter;
    }
    
}
