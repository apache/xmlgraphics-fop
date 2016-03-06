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

package org.apache.fop.render.pdf;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.fop.pdf.PDFColorHandler;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFFilterList;
import org.apache.fop.pdf.PDFLinearization;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFPaintingState;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFResourceContext;
import org.apache.fop.pdf.PDFStream;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFTextUtil;
import org.apache.fop.pdf.PDFXObject;

/**
 * Generator class encapsulating all object references and state necessary to generate a
 * PDF content stream.
 */
public class PDFContentGenerator {

    /** Controls whether comments are written to the PDF stream. */
    protected static final boolean WRITE_COMMENTS = true;

    private PDFDocument document;
    private OutputStream outputStream;
    private PDFResourceContext resourceContext;

    /** the current stream to add PDF commands to */
    private PDFStream currentStream;

    private PDFColorHandler colorHandler;

    /** drawing state */
    protected PDFPaintingState currentState;
    /** Text generation utility holding the current font status */
    protected PDFTextUtil textutil;

    private boolean inMarkedContentSequence;
    private boolean inArtifactMode;
    private AffineTransform transform;

    /**
     * Main constructor. Creates a new PDF stream and additional helper classes for text painting
     * and state management.
     * @param document the PDF document
     * @param out the output stream the PDF document is generated to
     * @param resourceContext the resource context
     */
    public PDFContentGenerator(PDFDocument document, OutputStream out,
            PDFResourceContext resourceContext) {
        this.document = document;
        this.outputStream = out;
        this.resourceContext = resourceContext;
        this.currentStream = document.getFactory()
                .makeStream(PDFFilterList.CONTENT_FILTER, false);
        this.textutil = new PDFTextUtil() {
            protected void write(String code) {
                currentStream.add(code);
            }
            protected void write(StringBuffer code) {
                currentStream.add(code);
            }
        };

        this.currentState = new PDFPaintingState();
        this.colorHandler = new PDFColorHandler(document.getResources());
    }

    public AffineTransform getAffineTransform() {
        return transform;
    }

    /**
     * Returns the applicable resource context for the generator.
     * @return the resource context
     */
    public PDFDocument getDocument() {
        return this.document;
    }

    /**
     * Returns the output stream the PDF document is written to.
     * @return the output stream
     */
    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    /**
     * Returns the applicable resource context for the generator.
     * @return the resource context
     */
    public PDFResourceContext getResourceContext() {
        return this.resourceContext;
    }

    /**
     * Returns the {@link PDFStream} associated with this instance.
     * @return the PDF stream
     */
    public PDFStream getStream() {
        return this.currentStream;
    }

    /**
     * Returns the {@link PDFPaintingState} associated with this instance.
     * @return the PDF state
     */
    public PDFPaintingState getState() {
        return this.currentState;
    }

    /**
     * Returns the {@link PDFTextUtil} associated with this instance.
     * @return the text utility
     */
    public PDFTextUtil getTextUtil() {
        return this.textutil;
    }

    /**
     * Flushes all queued PDF objects ready to be written to the output stream.
     * @throws IOException if an error occurs while flushing the PDF objects
     */
    public void flushPDFDoc() throws IOException {
        if (document.isLinearizationEnabled()) {
            new PDFLinearization(document).outputPages(outputStream);
        }
        this.document.output(this.outputStream);
    }

    /**
     * Writes out a comment.
     * @param text text for the comment
     */
    protected void comment(String text) {
        if (WRITE_COMMENTS) {
            getStream().add("% " + text + "\n");
        }
    }

    /** Save graphics state. */
    protected void saveGraphicsState() {
        endTextObject();
        getState().save();
        getStream().add("q\n");
    }

    /** Save graphics state with optional layer. */
    protected void saveGraphicsState(String layer) {
        endTextObject();
        getState().save();
        maybeBeginLayer(layer);
        getStream().add("q\n");
    }

    /**
     * Save graphics state.
     * @param structElemType an element type
     * @param sequenceNum a sequence number
     */
    protected void saveGraphicsState(String structElemType, int sequenceNum) {
        endTextObject();
        getState().save();
        beginMarkedContentSequence(structElemType, sequenceNum);
        getStream().add("q\n");
    }

    /**
     * Begins a new marked content sequence (BDC or BMC). If {@code structElemType} is
     * null, a BMC operator with an "Artifact" tag is generated. Otherwise, a BDC operator
     * with {@code structElemType} as a tag is generated, and the given mcid stored in its
     * property list.
     *
     * @param structElemType the type of the associated structure element
     * @param mcid the marked content identifier
     */
    protected void beginMarkedContentSequence(String structElemType, int mcid) {
        beginMarkedContentSequence(structElemType, mcid, null);
    }

    /**
     * Begins a new marked content sequence (BDC or BMC). If {@code structElemType} is
     * null, a BMC operator with an "Artifact" tag is generated. Otherwise, a BDC operator
     * with {@code structElemType} as a tag is generated, and the given mcid and actual
     * text are stored in its property list.
     *
     * @param structElemType the type of the associated structure element
     * @param mcid the marked content identifier
     * @param actualText the replacement text for the marked content
     */
    protected void beginMarkedContentSequence(String structElemType, int mcid, String actualText) {
        assert !this.inMarkedContentSequence;
        assert !this.inArtifactMode;
        if (structElemType != null) {
            String actualTextProperty = actualText == null ? ""
                    : " /ActualText " + PDFText.escapeText(actualText);
            getStream().add(structElemType + " <</MCID " + String.valueOf(mcid)
                    + actualTextProperty + ">>\n"
                    + "BDC\n");
        } else {
            getStream().add("/Artifact\nBMC\n");
            this.inArtifactMode = true;
        }
        this.inMarkedContentSequence = true;
    }

    void endMarkedContentSequence() {
        getStream().add("EMC\n");
        this.inMarkedContentSequence = false;
        this.inArtifactMode = false;
    }

    /**
     * Restored the graphics state valid before the previous {@link #saveGraphicsState()}.
     * @param popState true if the state should also be popped, false if only the PDF command
     *           should be issued
     */
    protected void restoreGraphicsState(boolean popState) {
        endTextObject();
        getStream().add("Q\n");
        maybeEndLayer();
        if (popState) {
            getState().restore();
        }
    }

    /**
     * Same as {@link #restoreGraphicsState(boolean)}, with <code>true</code> as
     * a parameter.
     */
    protected void restoreGraphicsState() {
        restoreGraphicsState(true);
    }

    /**
     * Same as {@link #restoreGraphicsState()}, additionally ending the current
     * marked content sequence if any.
     */
    protected void restoreGraphicsStateAccess() {
        endTextObject();
        getStream().add("Q\n");
        if (this.inMarkedContentSequence) {
            endMarkedContentSequence();
        }
        getState().restore();
    }

    private void maybeBeginLayer(String layer) {
        if ((layer != null) && (layer.length() > 0)) {
            getState().setLayer(layer);
            beginOptionalContent(layer);
        }
    }

    private void maybeEndLayer() {
        if (getState().getLayerChanged()) {
            endOptionalContent();
        }
    }

    private int ocNameIndex;

    private void beginOptionalContent(String layerId) {
        String name;
        PDFReference layer = document.resolveExtensionReference(layerId);
        if (layer != null) {
            name = "oc" + ++ocNameIndex;
            document.getResources().addProperty(name, layer);
        } else {
            name = "unknown";
        }
        getStream().add("/OC /" + name + " BDC\n");
    }

    private void endOptionalContent() {
        getStream().add("EMC\n");
    }

    /** Indicates the beginning of a text object. */
    protected void beginTextObject() {
        if (!textutil.isInTextObject()) {
            textutil.beginTextObject();
        }
    }

    /**
     * Indicates the beginning of a marked-content text object.
     *
     * @param structElemType structure element type
     * @param mcid sequence number
     * @see #beginTextObject()
     * @see #beginMarkedContentSequence(String, int)
     */
    protected void beginTextObject(String structElemType, int mcid) {
        beginTextObject(structElemType, mcid, null);
    }

    /**
     * Indicates the beginning of a marked-content text object.
     *
     * @param structElemType structure element type
     * @param mcid sequence number
     * @param actualText the replacement text for the marked content
     * @see #beginTextObject()
     * @see #beginMarkedContentSequence(String, int, String))
     */
    protected void beginTextObject(String structElemType, int mcid, String actualText) {
        if (!textutil.isInTextObject()) {
            beginMarkedContentSequence(structElemType, mcid, actualText);
            textutil.beginTextObject();
        }
    }

    /** Indicates the end of a text object. */
    protected void endTextObject() {
        if (textutil.isInTextObject()) {
            textutil.endTextObject();
            if (this.inMarkedContentSequence) {
                endMarkedContentSequence();
            }
        }
    }

    /**
     * Concatenates the given transformation matrix with the current one.
     * @param transform the transformation matrix (in points)
     */
    public void concatenate(AffineTransform transform) {
        this.transform = transform;
        if (!transform.isIdentity()) {
            getState().concatenate(transform);
            getStream().add(CTMHelper.toPDFString(transform, false) + " cm\n");
        }
    }

    /**
     * Intersects the current clip region with the given rectangle.
     * @param rect the clip rectangle
     */
    public void clipRect(Rectangle rect) {
        StringBuffer sb = new StringBuffer();
        sb.append(format(rect.x / 1000f)).append(' ');
        sb.append(format(rect.y / 1000f)).append(' ');
        sb.append(format(rect.width / 1000f)).append(' ');
        sb.append(format(rect.height / 1000f)).append(" re W n\n");
        add(sb.toString());
    }

    /**
     * Adds content to the stream.
     * @param content the PDF content
     */
    public void add(String content) {
        getStream().add(content);
    }

    /**
     * Formats a float value (normally coordinates in points) as Strings.
     * @param value the value
     * @return the formatted value
     */
    public static final String format(float value) {
        return PDFNumber.doubleOut(value);
    }

    /**
     * Sets the current line width in points.
     * @param width line width in points
     */
    public void updateLineWidth(float width) {
        if (getState().setLineWidth(width)) {
            //Only write if value has changed WRT the current line width
            getStream().add(format(width) + " w\n");
        }
    }

    /**
     * Sets the current character spacing (Tc) value.
     * @param value the Tc value (in unscaled text units)
     */
    public void updateCharacterSpacing(float value) {
        if (getState().setCharacterSpacing(value)) {
            getStream().add(format(value) + " Tc\n");
        }
    }

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply
     * @param fill true to set the fill color, false for the foreground color
     * @param stream the PDFStream to write the PDF code to
     */
    public void setColor(Color col, boolean fill, PDFStream stream) {
        assert stream != null;
        StringBuffer sb = new StringBuffer();
        setColor(col, fill, sb);
        stream.add(sb.toString());
    }

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply
     * @param fill true to set the fill color, false for the foreground color
     */
    public void setColor(Color col, boolean fill) {
        setColor(col, fill, getStream());
    }

    /**
     * Establishes a new foreground or fill color. In contrast to updateColor
     * this method does not check the PDFState for optimization possibilities.
     * @param col the color to apply
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    protected void setColor(Color col, boolean fill, StringBuffer pdf) {
        if (pdf != null) {
            colorHandler.establishColor(pdf, col, fill);
        } else {
            setColor(col, fill, getStream());
        }
    }

    /**
     * Establishes a new foreground or fill color.
     * @param col the color to apply (null skips this operation)
     * @param fill true to set the fill color, false for the foreground color
     * @param pdf StringBuffer to write the PDF code to, if null, the code is
     *     written to the current stream.
     */
    public void updateColor(Color col, boolean fill, StringBuffer pdf) {
        if (col == null) {
            return;
        }
        boolean update = false;
        if (fill) {
            update = getState().setBackColor(col);
        } else {
            update = getState().setColor(col);
        }

        if (update) {
            setColor(col, fill, pdf);
        }
    }

    /**
     * Places a previously registered image at a certain place on the page.
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj the image XObject
     */
    public void placeImage(float x, float y, float w, float h, PDFXObject xobj) {
        saveGraphicsState();
        add(format(w) + " 0 0 "
                          + format(-h) + " "
                          + format(x) + " "
                          + format(y + h)
                          + " cm\n" + xobj.getName() + " Do\n");
        restoreGraphicsState();
    }

    public void placeImage(AffineTransform at, String stream) {
        saveGraphicsState();
        concatenate(at);
        add(stream);
        restoreGraphicsState();
    }

    /**
     * Places a previously registered image at a certain place on the page,
     * bracketing it as a marked-content sequence.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param w width for image
     * @param h height for image
     * @param xobj the image XObject
     * @param structElemType structure element type
     * @param mcid sequence number
     * @see #beginMarkedContentSequence(String, int)
     */
    public void placeImage(float x, float y, float w, float h, PDFXObject xobj,
            String structElemType, int mcid) {
        saveGraphicsState(structElemType, mcid);
        add(format(w) + " 0 0 "
                          + format(-h) + " "
                          + format(x) + " "
                          + format(y + h)
                          + " cm\n" + xobj.getName() + " Do\n");
        restoreGraphicsStateAccess();
    }

}
