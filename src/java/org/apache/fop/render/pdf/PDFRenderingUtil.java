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

import java.awt.color.ICC_Profile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;
import org.apache.xmlgraphics.xmp.Metadata;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicAdapter;
import org.apache.xmlgraphics.xmp.schemas.XMPBasicSchema;

import org.apache.fop.accessibility.Accessibility;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fo.extensions.xmp.XMPMetadata;
import org.apache.fop.pdf.PDFAMode;
import org.apache.fop.pdf.PDFArray;
import org.apache.fop.pdf.PDFConformanceException;
import org.apache.fop.pdf.PDFDictionary;
import org.apache.fop.pdf.PDFDocument;
import org.apache.fop.pdf.PDFEmbeddedFile;
import org.apache.fop.pdf.PDFEmbeddedFiles;
import org.apache.fop.pdf.PDFEncryptionManager;
import org.apache.fop.pdf.PDFEncryptionParams;
import org.apache.fop.pdf.PDFFileSpec;
import org.apache.fop.pdf.PDFICCBasedColorSpace;
import org.apache.fop.pdf.PDFICCStream;
import org.apache.fop.pdf.PDFInfo;
import org.apache.fop.pdf.PDFLayer;
import org.apache.fop.pdf.PDFMetadata;
import org.apache.fop.pdf.PDFName;
import org.apache.fop.pdf.PDFNames;
import org.apache.fop.pdf.PDFNavigator;
import org.apache.fop.pdf.PDFNull;
import org.apache.fop.pdf.PDFNumber;
import org.apache.fop.pdf.PDFOutputIntent;
import org.apache.fop.pdf.PDFPage;
import org.apache.fop.pdf.PDFPageLabels;
import org.apache.fop.pdf.PDFReference;
import org.apache.fop.pdf.PDFSetOCGStateAction;
import org.apache.fop.pdf.PDFText;
import org.apache.fop.pdf.PDFTransitionAction;
import org.apache.fop.pdf.PDFXMode;
import org.apache.fop.pdf.Version;
import org.apache.fop.pdf.VersionController;
import org.apache.fop.render.pdf.extensions.PDFActionExtension;
import org.apache.fop.render.pdf.extensions.PDFArrayExtension;
import org.apache.fop.render.pdf.extensions.PDFCollectionEntryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryAttachment;
import org.apache.fop.render.pdf.extensions.PDFDictionaryExtension;
import org.apache.fop.render.pdf.extensions.PDFDictionaryType;
import org.apache.fop.render.pdf.extensions.PDFEmbeddedFileAttachment;
import org.apache.fop.render.pdf.extensions.PDFObjectType;
import org.apache.fop.render.pdf.extensions.PDFPageExtension;
import org.apache.fop.render.pdf.extensions.PDFReferenceExtension;

import static org.apache.fop.render.pdf.PDFEncryptionOption.ENCRYPTION_PARAMS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ACCESSCONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ANNOTATIONS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_ASSEMBLEDOC;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_COPY_CONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_EDIT_CONTENT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_FILLINFORMS;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_PRINT;
import static org.apache.fop.render.pdf.PDFEncryptionOption.NO_PRINTHQ;
import static org.apache.fop.render.pdf.PDFEncryptionOption.OWNER_PASSWORD;
import static org.apache.fop.render.pdf.PDFEncryptionOption.USER_PASSWORD;


/**
 * Utility class which enables all sorts of features that are not directly connected to the
 * normal rendering process.
 */
class PDFRenderingUtil {

    /** logging instance */
    private static Log log = LogFactory.getLog(PDFRenderingUtil.class);

    private FOUserAgent userAgent;

    /** the PDF Document being created */
    private PDFDocument pdfDoc;

    private PDFRendererOptionsConfig rendererConfig;

    /** the ICC stream used as output profile by this document for PDF/A and PDF/X functionality. */
    private PDFICCStream outputProfile;

    /** the default sRGB color space. */
    private PDFICCBasedColorSpace sRGBColorSpace;

    PDFRenderingUtil(FOUserAgent userAgent) {
        this.userAgent = userAgent;
        initialize();
    }

    private void initialize() {
        rendererConfig = PDFRendererOptionsConfig.DEFAULT.merge(createFromUserAgent(userAgent));
        if (rendererConfig.getPDFAMode().isLevelA()) {
            // PDF/A Level A requires tagged PDF
            userAgent.getRendererOptions().put(Accessibility.ACCESSIBILITY, Boolean.TRUE);
        }
    }

    private static  PDFRendererOptionsConfig createFromUserAgent(FOUserAgent userAgent) {
        Map<PDFRendererOption, Object> properties
                = new EnumMap<PDFRendererOption, Object>(PDFRendererOption.class);
        for (PDFRendererOption option : PDFRendererOption.values()) {
            Object value = userAgent.getRendererOption(option);
            properties.put(option, option.parse(value));
        }
        PDFEncryptionParams encryptionConfig = new EncryptionParamsBuilder().createParams(userAgent);
        return new PDFRendererOptionsConfig(properties, encryptionConfig);
    }

    void mergeRendererOptionsConfig(PDFRendererOptionsConfig config) {
        rendererConfig = rendererConfig.merge(config);
    }

    private void updateInfo() {
        PDFInfo info = pdfDoc.getInfo();
        info.setCreator(userAgent.getCreator());
        info.setCreationDate(userAgent.getCreationDate());
        info.setAuthor(userAgent.getAuthor());
        info.setTitle(userAgent.getTitle());
        info.setSubject(userAgent.getSubject());
        info.setKeywords(userAgent.getKeywords());
    }

    private void updatePDFProfiles() {
        pdfDoc.getProfile().setPDFAMode(rendererConfig.getPDFAMode());
        pdfDoc.getProfile().setPDFXMode(rendererConfig.getPDFXMode());
    }

    private void addsRGBColorSpace() throws IOException {
        if (rendererConfig.getDisableSRGBColorSpace()) {
            if (rendererConfig.getPDFAMode() != PDFAMode.DISABLED
                    || rendererConfig.getPDFXMode() != PDFXMode.DISABLED
                    || rendererConfig.getOutputProfileURI() != null) {
                throw new IllegalStateException("It is not possible to disable the sRGB color"
                        + " space if PDF/A or PDF/X functionality is enabled or an"
                        + " output profile is set!");
            }
        } else {
            if (this.sRGBColorSpace != null) {
                return;
            }
            //Map sRGB as default RGB profile for DeviceRGB
            this.sRGBColorSpace = PDFICCBasedColorSpace.setupsRGBAsDefaultRGBColorSpace(pdfDoc);
        }
    }

    private void addDefaultOutputProfile() throws IOException {
        if (this.outputProfile != null) {
            return;
        }
        ICC_Profile profile;
        InputStream in = null;
        URI outputProfileUri = rendererConfig.getOutputProfileURI();
        if (outputProfileUri != null) {
            this.outputProfile = pdfDoc.getFactory().makePDFICCStream();
            in = userAgent.getResourceResolver().getResource(rendererConfig.getOutputProfileURI());
            try {
                profile = ColorProfileUtil.getICC_Profile(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            this.outputProfile.setColorSpace(profile, null);
        } else {
            //Fall back to sRGB profile
            outputProfile = sRGBColorSpace.getICCStream();
        }
    }

    /**
     * Adds an OutputIntent to the PDF as mandated by PDF/A-1 when uncalibrated color spaces
     * are used (which is true if we use DeviceRGB to represent sRGB colors).
     * @throws IOException in case of an I/O problem
     */
    private void addPDFA1OutputIntent() throws IOException {
        addDefaultOutputProfile();

        String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
        PDFOutputIntent outputIntent = pdfDoc.getFactory().makeOutputIntent();
        outputIntent.setSubtype(PDFOutputIntent.GTS_PDFA1);
        outputIntent.setDestOutputProfile(this.outputProfile);
        outputIntent.setOutputConditionIdentifier(desc);
        outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
        pdfDoc.getRoot().addOutputIntent(outputIntent);
    }

    /**
     * Adds an OutputIntent to the PDF as mandated by PDF/X when uncalibrated color spaces
     * are used (which is true if we use DeviceRGB to represent sRGB colors).
     * @throws IOException in case of an I/O problem
     */
    private void addPDFXOutputIntent() throws IOException {
        addDefaultOutputProfile();

        String desc = ColorProfileUtil.getICCProfileDescription(this.outputProfile.getICCProfile());
        int deviceClass = this.outputProfile.getICCProfile().getProfileClass();
        if (deviceClass != ICC_Profile.CLASS_OUTPUT) {
            throw new PDFConformanceException(pdfDoc.getProfile().getPDFXMode() + " requires that"
                    + " the DestOutputProfile be an Output Device Profile. "
                    + desc + " does not match that requirement.");
        }
        PDFOutputIntent outputIntent = pdfDoc.getFactory().makeOutputIntent();
        outputIntent.setSubtype(PDFOutputIntent.GTS_PDFX);
        outputIntent.setDestOutputProfile(this.outputProfile);
        outputIntent.setOutputConditionIdentifier(desc);
        outputIntent.setInfo(outputIntent.getOutputConditionIdentifier());
        pdfDoc.getRoot().addOutputIntent(outputIntent);
    }

    public void renderXMPMetadata(XMPMetadata metadata) {
        Metadata docXMP = metadata.getMetadata();
        Metadata fopXMP = PDFMetadata.createXMPFromPDFDocument(pdfDoc);
        //Merge FOP's own metadata into the one from the XSL-FO document
        fopXMP.mergeInto(docXMP);
        XMPBasicAdapter xmpBasic = XMPBasicSchema.getAdapter(docXMP);
        //Metadata was changed so update metadata date
        xmpBasic.setMetadataDate(new java.util.Date());
        PDFMetadata.updateInfoFromMetadata(docXMP, pdfDoc.getInfo());

        PDFMetadata pdfMetadata = pdfDoc.getFactory().makeMetadata(
                docXMP, metadata.isReadOnly());
        pdfDoc.getRoot().setMetadata(pdfMetadata);
    }

    public void generateDefaultXMPMetadata() {
        if (pdfDoc.getRoot().getMetadata() == null) {
            //If at this time no XMP metadata for the overall document has been set, create it
            //from the PDFInfo object.
            Metadata xmp = PDFMetadata.createXMPFromPDFDocument(pdfDoc);
            PDFMetadata pdfMetadata = pdfDoc.getFactory().makeMetadata(
                    xmp, true);
            pdfDoc.getRoot().setMetadata(pdfMetadata);
        }
    }

    public void renderDictionaryExtension(PDFDictionaryAttachment attachment, PDFPage currentPage) {
        PDFDictionaryExtension extension = attachment.getExtension();
        PDFDictionaryType type = extension.getDictionaryType();
        if (type == PDFDictionaryType.Action) {
            addNavigatorAction(extension);
        } else if (type == PDFDictionaryType.Layer) {
            addLayer(extension);
        } else if (type == PDFDictionaryType.Navigator) {
            addNavigator(extension);
        } else {
            renderDictionaryExtension(extension, currentPage);
        }
    }

    public void addLayer(PDFDictionaryExtension extension) {
        assert extension.getDictionaryType() == PDFDictionaryType.Layer;
        String id = extension.getProperty(PDFDictionaryExtension.PROPERTY_ID);
        if ((id != null) && (id.length() > 0)) {
            PDFLayer layer = pdfDoc.getFactory().makeLayer(id);
            layer.setResolver(new PDFLayer.Resolver(layer, extension) {
                public void performResolution() {
                    PDFDictionaryExtension extension = (PDFDictionaryExtension) getExtension();
                    Object name = extension.findEntryValue("Name");
                    Object intent = extension.findEntryValue("Intent");
                    Object usage = makeDictionary(extension.findEntryValue("Usage"));
                    getLayer().populate(name, intent, usage);
                }
            });
        }
    }

    public void addNavigatorAction(PDFDictionaryExtension extension) {
        assert extension.getDictionaryType() == PDFDictionaryType.Action;
        String id = extension.getProperty(PDFDictionaryExtension.PROPERTY_ID);
        if ((id != null) && (id.length() > 0)) {
            String type = extension.getProperty(PDFActionExtension.PROPERTY_TYPE);
            if (type != null) {
                if (type.equals("SetOCGState")) {
                    PDFSetOCGStateAction action = pdfDoc.getFactory().makeSetOCGStateAction(id);
                    action.setResolver(new PDFSetOCGStateAction.Resolver(action, extension) {
                        public void performResolution() {
                            PDFDictionaryExtension extension = (PDFDictionaryExtension) getExtension();
                            Object state = makeArray(extension.findEntryValue("State"));
                            Object preserveRB = extension.findEntryValue("PreserveRB");
                            Object nextAction = makeDictionaryOrArray(extension.findEntryValue("Next"));
                            getAction().populate(state, preserveRB, nextAction);
                        }
                    });
                } else if (type.equals("Trans")) {
                    PDFTransitionAction action = pdfDoc.getFactory().makeTransitionAction(id);
                    action.setResolver(new PDFTransitionAction.Resolver(action, extension) {
                        public void performResolution() {
                            PDFDictionaryExtension extension = (PDFDictionaryExtension) getExtension();
                            Object transition = makeDictionary(extension.findEntryValue("Trans"));
                            Object nextAction = makeDictionaryOrArray(extension.findEntryValue("Next"));
                            getAction().populate(transition, nextAction);
                        }
                    });
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }

    public void addNavigator(PDFDictionaryExtension extension) {
        assert extension.getDictionaryType() == PDFDictionaryType.Navigator;
        String id = extension.getProperty(PDFDictionaryExtension.PROPERTY_ID);
        if ((id != null) && (id.length() > 0)) {
            PDFNavigator navigator = pdfDoc.getFactory().makeNavigator(id);
            navigator.setResolver(new PDFNavigator.Resolver(navigator, extension) {
                public void performResolution() {
                    PDFDictionaryExtension extension = (PDFDictionaryExtension) getExtension();
                    Object nextAction = makeDictionary(extension.findEntryValue("NA"));
                    Object next = makeDictionary(extension.findEntryValue("Next"));
                    Object prevAction = makeDictionary(extension.findEntryValue("PA"));
                    Object prev = makeDictionary(extension.findEntryValue("Prev"));
                    Object duration = extension.findEntryValue("Dur");
                    getNavigator().populate(nextAction, next, prevAction, prev, duration);
                }
            });
        }
    }

    private Object makeArray(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof PDFReferenceExtension) {
            return resolveReference((PDFReferenceExtension) value);
        } else if (value instanceof List<?>) {
            return populateArray(new PDFArray(), (List<?>) value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Object populateArray(PDFArray array, List<?> entries) {
        for (PDFCollectionEntryExtension entry : (List<PDFCollectionEntryExtension>) entries) {
            PDFObjectType type = entry.getType();
            if (type == PDFObjectType.Array) {
                array.add(makeArray(entry.getValue()));
            } else if (type == PDFObjectType.Boolean) {
                array.add(entry.getValueAsBoolean());
            } else if (type == PDFObjectType.Dictionary) {
                array.add(makeDictionary(entry.getValue()));
            } else if (type == PDFObjectType.Name) {
                array.add(new PDFName(entry.getValueAsString()));
            } else if (type == PDFObjectType.Number) {
                array.add(new PDFNumber(entry.getValueAsNumber()));
            } else if (type == PDFObjectType.Reference) {
                array.add(resolveReference((PDFReferenceExtension) entry));
            } else if (type == PDFObjectType.String) {
                array.add(entry.getValue());
            }
        }
        return array;
    }

    private Object makeDictionary(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof PDFReferenceExtension) {
            return resolveReference((PDFReferenceExtension) value);
        } else if (value instanceof List<?>) {
            return populateDictionary(new PDFDictionary(), (List<?>) value);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Object populateDictionary(PDFDictionary dictionary, List<?> entries) {
        for (PDFCollectionEntryExtension entry : (List<PDFCollectionEntryExtension>) entries) {
            PDFObjectType type = entry.getType();
            String key = entry.getKey();
            if (type == PDFObjectType.Array) {
                dictionary.put(key, makeArray(entry.getValue()));
            } else if (type == PDFObjectType.Boolean) {
                dictionary.put(key, entry.getValueAsBoolean());
            } else if (type == PDFObjectType.Dictionary) {
                dictionary.put(key, makeDictionary(entry.getValue()));
            } else if (type == PDFObjectType.Name) {
                dictionary.put(key, new PDFName(entry.getValueAsString()));
            } else if (type == PDFObjectType.Number) {
                dictionary.put(key, new PDFNumber(entry.getValueAsNumber()));
            } else if (type == PDFObjectType.Reference) {
                dictionary.put(key, resolveReference((PDFReferenceExtension) entry));
            } else if (type == PDFObjectType.String) {
                dictionary.put(key, entry.getValue());
            }
        }
        return dictionary;
    }

    private Object makeDictionaryOrArray(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof PDFReferenceExtension) {
            return resolveReference((PDFReferenceExtension) value);
        } else if (value instanceof List<?>) {
            if (hasKeyedEntry((List<?>) value)) {
                return populateDictionary(new PDFDictionary(), (List<?>) value);
            } else {
                return populateArray(new PDFArray(), (List<?>) value);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    private boolean hasKeyedEntry(List<?> entries) {
        for (PDFCollectionEntryExtension entry : (List<PDFCollectionEntryExtension>) entries) {
            if (entry.getKey() != null) {
                return true;
            }
        }
        return false;
    }

    public void renderDictionaryExtension(PDFDictionaryExtension extension, PDFPage currentPage) {
        PDFDictionaryType type = extension.getDictionaryType();
        if (type == PDFDictionaryType.Catalog) {
            augmentDictionary(pdfDoc.getRoot(), extension);
        } else if (type == PDFDictionaryType.Page) {
            assert extension instanceof PDFPageExtension;
            if (((PDFPageExtension) extension).matchesPageNumber(currentPage.getPageIndex() + 1)) {
                augmentDictionary(currentPage, extension);
            }
        } else if (type == PDFDictionaryType.Info) {
            PDFInfo info = pdfDoc.getInfo();
            for (PDFCollectionEntryExtension entry : extension.getEntries()) {
                info.put(entry.getKey(), entry.getValueAsString());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    private PDFDictionary augmentDictionary(PDFDictionary dictionary, PDFDictionaryExtension extension) {
        for (PDFCollectionEntryExtension entry : extension.getEntries()) {
            if (entry instanceof PDFDictionaryExtension) {
                dictionary.put(entry.getKey(),
                        augmentDictionary(new PDFDictionary(dictionary), (PDFDictionaryExtension) entry));
            } else if (entry instanceof PDFArrayExtension) {
                dictionary.put(entry.getKey(), augmentArray(new PDFArray(dictionary), (PDFArrayExtension) entry));
            } else {
                augmentDictionary(dictionary, entry);
            }
        }
        return dictionary;
    }

    private void augmentDictionary(PDFDictionary dictionary, PDFCollectionEntryExtension entry) {
        PDFObjectType type = entry.getType();
        String key = entry.getKey();
        if (type == PDFObjectType.Boolean) {
            dictionary.put(key, entry.getValueAsBoolean());
        } else if (type == PDFObjectType.Name) {
            dictionary.put(key, new PDFName(entry.getValueAsString()));
        } else if (type == PDFObjectType.Number) {
            dictionary.put(key, new PDFNumber(entry.getValueAsNumber()));
        } else if (type == PDFObjectType.Reference) {
            assert entry instanceof PDFReferenceExtension;
            dictionary.put(key, resolveReference((PDFReferenceExtension) entry));
        } else if (type == PDFObjectType.String) {
            dictionary.put(key, entry.getValueAsString());
        } else {
            throw new IllegalStateException();
        }
    }

    private Object resolveReference(PDFReferenceExtension entry) {
        PDFReference reference = (PDFReference) entry.getResolvedReference();
        if (reference == null) {
            reference = pdfDoc.resolveExtensionReference(entry.getReferenceId());
            if (reference != null) {
                entry.setResolvedReference(reference);
            }
            return reference;
        }
        return PDFNull.INSTANCE;
    }

    private PDFArray augmentArray(PDFArray array, PDFArrayExtension extension) {
        for (PDFCollectionEntryExtension entry : extension.getEntries()) {
            if (entry instanceof PDFDictionaryExtension) {
                array.add(augmentDictionary(new PDFDictionary(array), (PDFDictionaryExtension) entry));
            } else if (entry instanceof PDFArrayExtension) {
                array.add(augmentArray(new PDFArray(array), (PDFArrayExtension) entry));
            } else {
                augmentArray(array, entry);
            }
        }
        return array;
    }

    private void augmentArray(PDFArray array, PDFCollectionEntryExtension entry) {
        PDFObjectType type = entry.getType();
        if (type == PDFObjectType.Boolean) {
            array.add(entry.getValueAsBoolean());
        } else if (type == PDFObjectType.Name) {
            array.add(new PDFName(entry.getValueAsString()));
        } else if (type == PDFObjectType.Number) {
            array.add(new PDFNumber(entry.getValueAsNumber()));
        } else if (type == PDFObjectType.Reference) {
            assert entry instanceof PDFReferenceExtension;
            array.add(resolveReference((PDFReferenceExtension) entry));
        } else if (type == PDFObjectType.String) {
            array.add(entry.getValueAsString());
        } else {
            throw new IllegalStateException();
        }
    }

    public PDFDocument setupPDFDocument(OutputStream out) throws IOException {
        if (this.pdfDoc != null) {
            throw new IllegalStateException("PDFDocument already set up");
        }

        String producer = userAgent.getProducer() != null ? userAgent.getProducer() : "";
        final Version maxPDFVersion = rendererConfig.getPDFVersion();
        if (maxPDFVersion == null) {
            this.pdfDoc = new PDFDocument(producer);
        } else {
            VersionController controller
                    = VersionController.getFixedVersionController(maxPDFVersion);
            this.pdfDoc = new PDFDocument(producer, controller);
        }
        updateInfo();
        updatePDFProfiles();
        pdfDoc.setFilterMap(rendererConfig.getFilterMap());
        pdfDoc.outputHeader(out);

        //Setup encryption if necessary
        PDFEncryptionManager.setupPDFEncryption(rendererConfig.getEncryptionParameters(), pdfDoc);

        addsRGBColorSpace();
        if (rendererConfig.getOutputProfileURI() != null) {
            addDefaultOutputProfile();
        }
        PDFXMode pdfXMode = rendererConfig.getPDFXMode();
        if (pdfXMode != PDFXMode.DISABLED) {
            log.debug(pdfXMode + " is active.");
            log.warn("Note: " + pdfXMode
                    + " support is work-in-progress and not fully implemented, yet!");
            addPDFXOutputIntent();
        }
        PDFAMode pdfAMode = rendererConfig.getPDFAMode();
        if (pdfAMode.isEnabled()) {
            log.debug("PDF/A is active. Conformance Level: " + pdfAMode);
            addPDFA1OutputIntent();
        }

        this.pdfDoc.enableAccessibility(userAgent.isAccessibilityEnabled());

        return this.pdfDoc;
    }

    /**
     * Generates a page label in the PDF document.
     * @param pageIndex the index of the page
     * @param pageNumber the formatted page number
     */
    public void generatePageLabel(int pageIndex, String pageNumber) {
        //Produce page labels
        PDFPageLabels pageLabels = this.pdfDoc.getRoot().getPageLabels();
        if (pageLabels == null) {
            //Set up PageLabels
            pageLabels = this.pdfDoc.getFactory().makePageLabels();
            this.pdfDoc.getRoot().setPageLabels(pageLabels);
        }
        pageLabels.addPageLabel(pageIndex, pageNumber);
    }

    /**
     * Adds an embedded file to the PDF file.
     * @param embeddedFile the object representing the embedded file to be added
     * @throws IOException if an I/O error occurs
     */
    public void addEmbeddedFile(PDFEmbeddedFileAttachment embeddedFile)
            throws IOException {
        this.pdfDoc.getProfile().verifyEmbeddedFilesAllowed();
        PDFNames names = this.pdfDoc.getRoot().getNames();
        if (names == null) {
            //Add Names if not already present
            names = this.pdfDoc.getFactory().makeNames();
            this.pdfDoc.getRoot().setNames(names);
        }

        //Create embedded file
        PDFEmbeddedFile file = new PDFEmbeddedFile();
        this.pdfDoc.registerObject(file);
        URI srcURI;
        try {
            srcURI = InternalResourceResolver.cleanURI(embeddedFile.getSrc());
        } catch (URISyntaxException use) {
            throw new RuntimeException(use);
        }
        InputStream in = userAgent.getResourceResolver().getResource(srcURI);
        if (in == null) {
            throw new FileNotFoundException(embeddedFile.getSrc());
        }
        try {
            OutputStream out = file.getBufferOutputStream();
            IOUtils.copyLarge(in, out);
        } finally {
            IOUtils.closeQuietly(in);
        }
        PDFDictionary dict = new PDFDictionary();
        dict.put("F", file);
        String filename = PDFText.toPDFString(embeddedFile.getFilename(), '_');
        PDFFileSpec fileSpec = new PDFFileSpec(filename);
        fileSpec.setEmbeddedFile(dict);
        if (embeddedFile.getDesc() != null) {
            fileSpec.setDescription(embeddedFile.getDesc());
        }
        this.pdfDoc.registerObject(fileSpec);

        //Make sure there is an EmbeddedFiles in the Names dictionary
        PDFEmbeddedFiles embeddedFiles = names.getEmbeddedFiles();
        if (embeddedFiles == null) {
            embeddedFiles = new PDFEmbeddedFiles();
            this.pdfDoc.assignObjectNumber(embeddedFiles);
            this.pdfDoc.addTrailerObject(embeddedFiles);
            names.setEmbeddedFiles(embeddedFiles);
        }

        //Add to EmbeddedFiles in the Names dictionary
        PDFArray nameArray = embeddedFiles.getNames();
        if (nameArray == null) {
            nameArray = new PDFArray();
            embeddedFiles.setNames(nameArray);
        }
        String name = PDFText.toPDFString(filename);
        nameArray.add(name);
        nameArray.add(new PDFReference(fileSpec));
    }

    private static final class EncryptionParamsBuilder {
        private PDFEncryptionParams params;

        private EncryptionParamsBuilder() {
        }

        private PDFEncryptionParams createParams(FOUserAgent userAgent) {
            params = (PDFEncryptionParams) userAgent.getRendererOptions().get(ENCRYPTION_PARAMS);
            String userPassword = (String) userAgent.getRendererOption(USER_PASSWORD);
            if (userPassword != null) {
                getEncryptionParams().setUserPassword(userPassword);
            }
            String ownerPassword = (String) userAgent.getRendererOption(OWNER_PASSWORD);
            if (ownerPassword != null) {
                getEncryptionParams().setOwnerPassword(ownerPassword);
            }
            Object noPrint = userAgent.getRendererOption(NO_PRINT);
            if (noPrint != null) {
                getEncryptionParams().setAllowPrint(!booleanValueOf(noPrint));
            }
            Object noCopyContent = userAgent.getRendererOption(NO_COPY_CONTENT);
            if (noCopyContent != null) {
                getEncryptionParams().setAllowCopyContent(!booleanValueOf(noCopyContent));
            }
            Object noEditContent = userAgent.getRendererOption(NO_EDIT_CONTENT);
            if (noEditContent != null) {
                getEncryptionParams().setAllowEditContent(!booleanValueOf(noEditContent));
            }
            Object noAnnotations = userAgent.getRendererOption(NO_ANNOTATIONS);
            if (noAnnotations != null) {
                getEncryptionParams().setAllowEditAnnotations(!booleanValueOf(noAnnotations));
            }
            Object noFillInForms = userAgent.getRendererOption(NO_FILLINFORMS);
            if (noFillInForms != null) {
                getEncryptionParams().setAllowFillInForms(!booleanValueOf(noFillInForms));
            }
            Object noAccessContent = userAgent.getRendererOption(NO_ACCESSCONTENT);
            if (noAccessContent != null) {
                getEncryptionParams().setAllowAccessContent(!booleanValueOf(noAccessContent));
            }
            Object noAssembleDoc = userAgent.getRendererOption(NO_ASSEMBLEDOC);
            if (noAssembleDoc != null) {
                getEncryptionParams().setAllowAssembleDocument(!booleanValueOf(noAssembleDoc));
            }
            Object noPrintHQ = userAgent.getRendererOption(NO_PRINTHQ);
            if (noPrintHQ != null) {
                getEncryptionParams().setAllowPrintHq(!booleanValueOf(noPrintHQ));
            }
            return params;
        }

        private PDFEncryptionParams getEncryptionParams() {
            if (params == null) {
                params = new PDFEncryptionParams();
            }
            return params;
        }

        private static boolean booleanValueOf(Object obj) {
            if (obj instanceof Boolean) {
                return ((Boolean)obj).booleanValue();
            } else if (obj instanceof String) {
                return Boolean.valueOf((String)obj).booleanValue();
            } else {
                throw new IllegalArgumentException("Boolean or \"true\" or \"false\" expected.");
            }
        }
    }
}
