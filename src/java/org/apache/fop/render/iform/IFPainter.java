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

package org.apache.fop.render.iform;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import org.xml.sax.ContentHandler;

public interface IFPainter {

    //for foreign content and extensions
    ContentHandler getContentHandler();

    void startDocument();
    void endDocument();

    void startDocumentHeader();
    void endDocumentHeader();

    void startPageSequence(String id);
    void endPageSequence();

    void startPage(int index, String name);
    void endPage();

    void startPageHeader();
    void endPageHeader();

    void startPageContent();
    void endPageContent();

    void startPageTrailer();
    void addTarget(String name, int x, int y);
    void endPageTrailer();

    void startBox(AffineTransform transform, Dimension size, boolean clip);
    void startBox(String transform, Dimension size, boolean clip);
    //For transform, something like Batik's org.apache.batik.parser.TransformListHandler/Parser can be used
    void endBox();

    void setFont(String family, String style, Integer weight, String variant, Integer size, String color);
    //All of setFont()'s parameters can be null if no state change is necessary
    void drawText(int[] x, int[] y, String text);
    void drawRect(Rectangle rect, String fill, String stroke);
    void drawImage(String uri, Rectangle rect); //external images
    void startImage(Rectangle rect); //followed by a SAX stream (SVG etc.)
    void endImage();
    //etc. etc.
}
