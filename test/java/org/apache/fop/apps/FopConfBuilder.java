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

package org.apache.fop.apps;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.fop.render.RendererConfigOption;

/**
 * A builder class for creating fop.xconf XML DOMs for test purposes. You can set all the necessary
 * fields inline and build the fop conf DOM into an {@link InputStream}.
 * <pre>
 * {@code
 *     new FopConfBuilder().setStrictValidation(true)
 *                         .startRendererBuilder(RendererConfBuilder.class)
 *                             .startFontsConfig()
 *                                 .startFont(null, null)
 *                                     .addTriplet("Gladiator", "normal", "normal")
 *                                 .endFont()
 *                             .endFontConfig()
 *                         .endRendererConfigBuilder().build()
 * }
 * </pre>
 */
public class FopConfBuilder implements FontConfigurator<FopConfBuilder> {

    private final Element root;
    private final Document fopConfDOM;
    private RendererConfBuilder currentRendererConfig;
    private FontsConfBuilder<FopConfBuilder> currentFontsConfig;

    /**
     * Constructs the FopConfBuilder and initializes the underlying DOM.
     */
    public FopConfBuilder() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            fopConfDOM = db.newDocument();
            root = fopConfDOM.createElement("fop");
            fopConfDOM.appendChild(root);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    private FopConfBuilder createElement(String elementName, String elementValue) {
        Element el = fopConfDOM.createElement(elementName);
        el.appendChild(fopConfDOM.createTextNode(elementValue));
        root.appendChild(el);
        return this;
    }

    /**
     * Set the &lt;font-base&gt; tag within the fop.xconf.
     *
     * @param fontBase the font base value
     * @return <b>this</b>
     */
    public FopConfBuilder setFontBaseURI(String fontBase) {
        return createElement("font-base", fontBase);
    }

    /**
     * Set the &lt;base&gt; tag within the fop.xconf.
     *
     * @param baseURI the base URI
     * @return <b>this</b>
     */
    public FopConfBuilder setBaseURI(String baseURI) {
        return createElement("base", baseURI);
    }

    /**
     * Set the &lt;strict-validation&gt; tag within the fop.xconf.
     *
     * @param validateStrictly true to enforce strict validation
     * @return <b>this</b>
     */
    public FopConfBuilder setStrictValidation(boolean validateStrictly) {
        return createElement("strict-validation", String.valueOf(validateStrictly));
    }

    /**
     * Set the &lt;accessibility&gt; tag within the fop.xconf.
     *
     * @param setAccessibility true to enable accessibility features
     * @return <b>this</b>
     */
    public FopConfBuilder setAccessibility(boolean setAccessibility) {
        return createElement("accessibility", String.valueOf(setAccessibility));
    }

    @Deprecated
    public FopConfBuilder setHyphenationBaseURI(String uri) {
        return createElement("hyphenation-base", uri);
    }

    /**
     * Set the &lt;source-resolution&gt; tag within the fop.xconf.
     *
     * @param srcRes the source resolution
     * @return <b>this</b>
     */
    public FopConfBuilder setSourceResolution(float srcRes) {
        return createElement("source-resolution", String.valueOf(srcRes));
    }

    /**
     * Set the &lt;target-resolution&gt; tag within the fop.xconf.
     *
     * @param targetRes the target resolution
     * @return <b>this</b>
     */
    public FopConfBuilder setTargetResolution(float targetRes) {
        return createElement("target-resolution", String.valueOf(targetRes));
    }

    /**
     * Set the &lt;break-indent-inheritance&gt; tag within the fop.xconf.
     *
     * @param value true to break indent inheritance
     * @return <b>this</b>
     */
    public FopConfBuilder setBreakIndentInheritance(boolean value) {
        return createElement("break-indent-inheritance", String.valueOf(value));
    }

    /**
     * Set the &lt;prefer-renderer&gt; tag within the fop.xconf.
     *
     * @param value true to prefer the renderer
     * @return <b>this</b>
     */
    public FopConfBuilder setPreferRenderer(boolean value) {
        return createElement("prefer-renderer", String.valueOf(value));
    }

    /**
     * Set the &lt;default-page-settings&gt; tag within the fop.xconf.
     *
     * @param height the height of the page
     * @param width the width of the page
     * @return <b>this</b>
     */
    public FopConfBuilder setDefaultPageSettings(float height, float width) {
        Element el = fopConfDOM.createElement("default-page-settings");
        el.setAttribute("height", String.valueOf(height));
        el.setAttribute("width", String.valueOf(width));
        root.appendChild(el);
        return this;
    }

    /**
     * Sets whether the fonts cache is used or not.
     *
     * @param enableFontCaching true to enable font data caching.
     * @return <b>this</b>
     */
    public FopConfBuilder useCache(boolean enableFontCaching) {
        return createElement("use-cache", String.valueOf(enableFontCaching));
    }

    /**
     * Starts a renderer specific config builder.
     *
     * @param mimeType the MIME type of the builder
     * @return the renderer config builder
     */
    public <T extends RendererConfBuilder> T startRendererConfig(Class<T> rendererConfigClass) {
        try {
            currentRendererConfig = rendererConfigClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        currentRendererConfig.init(this, fopConfDOM);
        return rendererConfigClass.cast(currentRendererConfig);
    }

    /**
     * Ends a renderer specific config builder.
     *
     * @return <b>this</b>
     */
    private FopConfBuilder endRendererConfig() {
        Element renderersEl = fopConfDOM.createElement("renderers");
        renderersEl.appendChild(currentRendererConfig.rendererEl);
        root.appendChild(renderersEl);
        currentRendererConfig = null;
        return this;
    }

    /**
     * Starts a fonts config builder, for configuring the fonts handling system within FOP i.e.
     * the &lt;fonts&gt; element.
     *
     * @return the fop config builder
     */
    public FontsConfBuilder<FopConfBuilder> startFontsConfig() {
        currentFontsConfig = new FontsConfBuilder<FopConfBuilder>(this);
        currentFontsConfig.setFopConfDOM(fopConfDOM);
        return currentFontsConfig;
    }

    /**
     * Ends the fonts config builder.
     *
     * @return <b>this</b>
     */
    public FopConfBuilder endFontsConfig() {
        root.appendChild(currentFontsConfig.fontsEl);
        currentFontsConfig = null;
        return this;
    }

    /**
     * Converts the underlying DOM into an {@link InputStream} for building.
     *
     * @return an {@link InputStream}
     */
    public InputStream build() {
        try {
            Source src = new DOMSource(fopConfDOM);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Result res = new StreamResult(baos);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(src, res);
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(OutputStream out) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e1) {
            throw new RuntimeException(e1);
        }
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        try {
            transformer.transform(new DOMSource(fopConfDOM),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract static class RendererConfBuilder implements FontConfigurator<RendererConfBuilder> {

        private Element rendererEl;

        private FopConfBuilder fopConfBuilder;

        private Document fopConfDOM;

        private final String mimeType;

        private FontsConfBuilder<RendererConfBuilder> fontsConfBuilder;

        protected RendererConfBuilder(String mimeType) {
            this.mimeType = mimeType;
        }

        private void init(FopConfBuilder fopConfBuilder, Document fopConfDOM) {
            this.fopConfBuilder = fopConfBuilder;
            this.fopConfDOM = fopConfDOM;
            rendererEl = fopConfDOM.createElement("renderer");
            rendererEl.setAttribute("mime", mimeType);
        }

        protected final Element createElement(String name) {
            return createElement(name, rendererEl);
        }

        protected final Element createElement(String name, Element parent) {
            Element el = fopConfDOM.createElement(name);
            parent.appendChild(el);
            return el;
        }

        protected final Element createTextElement(String name, String value) {
            return createTextElement(name, value, rendererEl);
        }

        protected final Element createTextElement(RendererConfigOption option, String value) {
            return createTextElement(option.getName(), value, rendererEl);
        }

        protected final Element createTextElement(String name, String value, Element parent) {
            Element el = createElement(name, parent);
            el.setTextContent(value);
            return el;
        }

        /**
         * Starts a fonts config builder, for configuring the fonts handling system within FOP i.e.
         * the &lt;fonts&gt; element.
         *
         * @return the fonts config builder
         */
        public final FontsConfBuilder<RendererConfBuilder> startFontsConfig() {
            fontsConfBuilder = new FontsConfBuilder<RendererConfBuilder>(this);
            fontsConfBuilder.setFopConfDOM(fopConfBuilder.fopConfDOM);
            return fontsConfBuilder;
        }

        /**
         * Ends the fonts config builder.
         *
         * @return <b>this</b>
         */
        public final RendererConfBuilder endFontsConfig() {
            rendererEl.appendChild(fontsConfBuilder.fontsEl);
            fontsConfBuilder = null;
            return this;
        }

        /**
         * Ends the renderer specific config.
         *
         * @return the parent
         */
        public final FopConfBuilder endRendererConfig() {
            return fopConfBuilder.endRendererConfig();
        }

        public void dump() {
            fopConfBuilder.dump();
        }

        public void dump(OutputStream out) {
            fopConfBuilder.dump(out);
        }
    }

    public static final class FontsConfBuilder<P extends FontConfigurator<P>> {
        private Element fontsEl;
        private final P parent;
        private Document fopConfDOM;
        private Element fontSubstitutions;
        private FontTripletInfo<P> currentTripletInfo;

        private FontsConfBuilder(P parent) {
            this.parent = parent;
        }

        private void setFopConfDOM(Document fopConfDOM) {
            this.fopConfDOM = fopConfDOM;
            fontsEl = fopConfDOM.createElement("fonts");
        }

        /**
         * Add &lt;auto-detect&gt; to find fonts.
         *
         * @return <b>this</b>
         */
        public FontsConfBuilder<P> addAutoDetect() {
            fontsEl.appendChild(fopConfDOM.createElement("auto-detect"));
            return this;
        }

        /**
         * Add a &lt;directory&gt; for specifying a directory to check fonts in.
         *
         * @param directory the directory to find fonts within
         * @param recursive true to recurse through sub-directories
         * @return <b>this</b>
         */
        public FontsConfBuilder<P> addDirectory(String directory, boolean recursive) {
            Element dir = fopConfDOM.createElement("directory");
            dir.setAttribute("recursive", String.valueOf(recursive));
            dir.setTextContent(directory);
            fontsEl.appendChild(dir);
            return this;
        }

        /**
         * Create a font &lt;substitution&gt;.
         *
         * @param fromFamily from font family name
         * @param fromStyle from font style
         * @param fromWeight from font weight
         * @param toFamily to font family name
         * @param toStyle to font style
         * @param toWeight to font weight
         * @return <b>this</b>
         */
        public P substituteFonts(String fromFamily, String fromStyle,
                String fromWeight, String toFamily, String toStyle, String toWeight) {
            if (fontSubstitutions == null) {
                fontSubstitutions = fopConfDOM.createElement("substitutions");
            }
            Element fontSubEl = fopConfDOM.createElement("substitution");
            fontSubEl.appendChild(createSubstitutionEl("from", fromFamily, fromStyle, fromWeight));
            fontSubEl.appendChild(createSubstitutionEl("to", toFamily, toStyle, toWeight));
            fontSubstitutions.appendChild(fontSubEl);
            fontsEl.appendChild(fontSubstitutions);
            return parent;
        }

        private Element createSubstitutionEl(String elName, String family, String style,
                String weight) {
            Element element = fopConfDOM.createElement(elName);
            addAttribute(element, "font-family", family);
            addAttribute(element, "font-style", style);
            addAttribute(element, "font-weight", weight);
            return element;
        }

        private void addAttribute(Element fontSub, String attName, String attValue) {
            if (attName != null && attValue != null) {
                fontSub.setAttribute(attName, attValue);
            }
        }

        /**
         * Start a &lt;font&gt; configuration element.
         *
         * @param metricsURL the URL to the metrics resource
         * @param embedURL the URL to the font resource
         * @return <b>this</b>
         */
        public FontTripletInfo<P> startFont(String metricsURL, String embedURL) {
            currentTripletInfo = new FontTripletInfo<P>(this, metricsURL, embedURL);
            return currentTripletInfo;
        }

        private FontsConfBuilder<P> endFontTriplet(Element el) {
            fontsEl.appendChild(el);
            currentTripletInfo = null;
            return this;
        }

        /**
         * Ends a font configuration element .
         *
         * @return the parent
         */
        public P endFontConfig() {
            return parent.endFontsConfig();
        }

        public final class FontTripletInfo<T> {
            private final Element fontEl;
            private final FontsConfBuilder<P> parent;

            private FontTripletInfo(FontsConfBuilder<P> parent,
                    String metricsURL, String embedURL) {
                this.parent = parent;
                fontEl = fopConfDOM.createElement("font");
                addAttribute(fontEl, "metrics-url", metricsURL);
                addAttribute(fontEl, "embed-url", embedURL);
            }

            /**
             * Add triplet information to a font.
             *
             * @param name the font name
             * @param style the font style
             * @param weight the font weight
             * @return <b>this</b>
             */
            public FontTripletInfo<T> addTriplet(String name, String style, String weight) {
                Element tripletEl = fopConfDOM.createElement("font-triplet");
                addAttribute(tripletEl, "name", name);
                addAttribute(tripletEl, "style", style);
                addAttribute(tripletEl, "weight", weight);
                fontEl.appendChild(tripletEl);
                return this;
            }

            /**
             * Ends the font configuration element.
             *
             * @return the parent
             */
            public FontsConfBuilder<P> endFont() {
                return parent.endFontTriplet(fontEl);
            }
        }
    }
}
