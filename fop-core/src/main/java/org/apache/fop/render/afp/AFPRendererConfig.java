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

package org.apache.fop.render.afp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.fop.afp.AFPConstants;
import org.apache.fop.afp.AFPDataObjectInfo;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.AFPResourceLevel;
import org.apache.fop.afp.AFPResourceLevelDefaults;
import org.apache.fop.afp.modca.triplets.MappingOptionTriplet;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.RendererConfig;
import org.apache.fop.render.afp.AFPFontConfig.AFPFontInfoConfigParser;
import org.apache.fop.util.LogUtil;

import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_COLOR;
import static org.apache.fop.render.afp.AFPRendererConfig.ImagesModeOptions.MODE_GRAYSCALE;
import static org.apache.fop.render.afp.AFPRendererOption.DEFAULT_RESOURCE_LEVELS;
import static org.apache.fop.render.afp.AFPRendererOption.GOCA;
import static org.apache.fop.render.afp.AFPRendererOption.GOCA_TEXT;
import static org.apache.fop.render.afp.AFPRendererOption.GOCA_WRAP_PSEG;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_DITHERING_QUALITY;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_FS45;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_JPEG;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_MAPPING_OPTION;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_MASK_ENABLED;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_MODE;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_NATIVE;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_NATIVE_PDF;
import static org.apache.fop.render.afp.AFPRendererOption.IMAGES_WRAP_PSEG;
import static org.apache.fop.render.afp.AFPRendererOption.JPEG_ALLOW_JPEG_EMBEDDING;
import static org.apache.fop.render.afp.AFPRendererOption.JPEG_BITMAP_ENCODING_QUALITY;
import static org.apache.fop.render.afp.AFPRendererOption.JPEG_USE_IOCA_IMAGES;
import static org.apache.fop.render.afp.AFPRendererOption.LINE_WIDTH_CORRECTION;
import static org.apache.fop.render.afp.AFPRendererOption.METADATA_IN_OBJECT_CONTAINER;
import static org.apache.fop.render.afp.AFPRendererOption.RENDERER_RESOLUTION;
import static org.apache.fop.render.afp.AFPRendererOption.RESOURCE_GROUP_URI;
import static org.apache.fop.render.afp.AFPRendererOption.SHADING;

/**
 * The AFP renderer config object.
 */
public final class AFPRendererConfig implements RendererConfig {

    /**
     * An enumeration for the various images modes available to the AFP renderer.
     */
    public enum ImagesModeOptions {

        MODE_GRAYSCALE("b+w", "bits-per-pixel"),
        MODE_COLOR("color", "cmyk");

        private final String name;

        private final String modeAttribute;

        private ImagesModeOptions(String name, String modeAttribute) {
            this.name = name;
            this.modeAttribute = modeAttribute;
        }

        public String getName() {
            return name;
        }

        public String getModeAttribute() {
            return modeAttribute;
        }

        public static ImagesModeOptions forName(String name) {
            for (ImagesModeOptions option : values()) {
                if (option.name.equals(name)) {
                    return option;
                }
            }
            throw new IllegalArgumentException(name);
        }
    }

    private final EnumMap<AFPRendererOption, Object> params
            = new EnumMap<AFPRendererOption, Object>(AFPRendererOption.class);

    private final EnumMap<ImagesModeOptions, Object> imageModeParams
    = new EnumMap<ImagesModeOptions, Object>(ImagesModeOptions.class);

    private final AFPFontConfig fontConfig;

    private AFPRendererConfig(AFPFontConfig fontConfig) {
        this.fontConfig = fontConfig;
    }

    public AFPFontConfig getFontInfoConfig() {
        return fontConfig;
    }

    public Boolean isColorImages() {
        return getParam(IMAGES_MODE, Boolean.class);
    }

    public Boolean isCmykImagesSupported() {
        if (!isColorImages()) {
            throw new IllegalStateException();
        }
        return Boolean.class.cast(imageModeParams.get(MODE_COLOR));
    }

    public Integer getBitsPerPixel() {
        if (isColorImages()) {
            throw new IllegalStateException();
        }
        return Integer.class.cast(imageModeParams.get(MODE_GRAYSCALE));
    }

    public Float getDitheringQuality() {
        return getParam(IMAGES_DITHERING_QUALITY, Float.class);
    }

    public Boolean isNativeImagesSupported() {
        return getParam(IMAGES_NATIVE, Boolean.class);
    }

    public Boolean isNativePDFImagesSupported() {
        return getParam(IMAGES_NATIVE_PDF, Boolean.class);
    }

    public AFPShadingMode getShadingMode() {
        return getParam(SHADING, AFPShadingMode.class);
    }

    public Integer getResolution() {
        return getParam(RENDERER_RESOLUTION, Integer.class);
    }


    public URI getDefaultResourceGroupUri() {
        return getParam(RESOURCE_GROUP_URI, URI.class);
    }

    public AFPResourceLevelDefaults getResourceLevelDefaults() {
        return getParam(DEFAULT_RESOURCE_LEVELS, AFPResourceLevelDefaults.class);
    }

    public Boolean isWrapPseg() {
        return getParam(IMAGES_WRAP_PSEG, Boolean.class);
    }

    public Boolean isGocaWrapPseg() {
        return getParam(GOCA_WRAP_PSEG, Boolean.class);
    }

    public Boolean isFs45() {
        return getParam(IMAGES_FS45, Boolean.class);
    }

    public Boolean isMaskEnabled() {
        return getParam(IMAGES_MASK_ENABLED, Boolean.class);
    }

    public Boolean allowJpegEmbedding() {
        return getParam(JPEG_ALLOW_JPEG_EMBEDDING, Boolean.class);
    }

    public Float getBitmapEncodingQuality() {
        return getParam(JPEG_BITMAP_ENCODING_QUALITY, Float.class);
    }

    public Boolean isUseIocaImages() {
        return getParam(JPEG_USE_IOCA_IMAGES, Boolean.class);
    }

    public Boolean isMetadataInObjectContainer() {
        return getParam(METADATA_IN_OBJECT_CONTAINER, Boolean.class);
    }

    public Float getLineWidthCorrection() {
        return getParam(LINE_WIDTH_CORRECTION, Float.class);
    }

    public Boolean isGocaEnabled() {
        return getParam(GOCA, Boolean.class);
    }

    public Boolean isStrokeGocaText() {
        return getParam(GOCA_TEXT, Boolean.class);
    }

    private <T> T getParam(AFPRendererOption options, Class<T> type) {
        assert options.getType().equals(type);
        return type.cast(params.get(options));
    }

    private <T> void setParam(AFPRendererOption option, T value) {
        assert option.getType().isInstance(value);
        params.put(option, value);
    }

    /**
     * The parser for AFP renderer specific data in the FOP conf.
     */
    public static final class AFPRendererConfigParser implements RendererConfigParser {

        private static final Log LOG = LogFactory.getLog(AFPRendererConfigParser.class);

        /** {@inheritDoc} */
        public AFPRendererConfig build(FOUserAgent userAgent, Configuration cfg) throws FOPException {
            boolean strict = userAgent.validateUserConfigStrictly();
            AFPRendererConfig config = null;
            AFPEventProducer eventProducer = AFPEventProducer.Provider.get(userAgent.getEventBroadcaster());
            try {
                config = new ParserHelper(cfg, userAgent.getFontManager(), strict, eventProducer).config;
            } catch (ConfigurationException e) {
                LogUtil.handleException(LOG, e, strict);
            }
            return config;
        }

        /** {@inheritDoc} */
        public String getMimeType() {
            return MimeConstants.MIME_AFP;
        }
    }

    private static final class ParserHelper {

        private static final Log LOG = LogFactory.getLog(ParserHelper.class);

        private final AFPRendererConfig config;

        private final boolean strict;

        private final Configuration cfg;

        private ParserHelper(Configuration cfg, FontManager fontManager, boolean strict,
                AFPEventProducer eventProducer)
                        throws ConfigurationException, FOPException {
            this.cfg = cfg;
            this.strict = strict;
            if (cfg != null) {
                config = new AFPRendererConfig(new AFPFontInfoConfigParser().parse(cfg,
                        fontManager, strict, eventProducer));
                configure();
            } else {
                config = new AFPRendererConfig(new AFPFontInfoConfigParser().getEmptyConfig());
            }
        }

        private void configure() throws ConfigurationException, FOPException {
            configureImages();
            configureGOCA();
            setParam(SHADING, AFPShadingMode.getValueOf(
                    cfg.getChild(SHADING.getName()).getValue(AFPShadingMode.COLOR.getName())));
            Configuration rendererResolutionCfg = cfg.getChild(RENDERER_RESOLUTION.getName(), false);
            setParam(RENDERER_RESOLUTION, rendererResolutionCfg == null ? 240
                    : rendererResolutionCfg.getValueAsInteger(240));
            Configuration lineWidthCorrectionCfg = cfg.getChild(LINE_WIDTH_CORRECTION.getName(),
                    false);
            setParam(LINE_WIDTH_CORRECTION, lineWidthCorrectionCfg != null
                    ? lineWidthCorrectionCfg.getValueAsFloat()
                    : AFPConstants.LINE_WIDTH_CORRECTION);
            Configuration gocaCfg = cfg.getChild(GOCA.getName());
            boolean gocaEnabled = gocaCfg.getAttributeAsBoolean("enabled", true);
            setParam(GOCA, gocaEnabled);
            String strokeGocaText = gocaCfg.getAttribute(GOCA_TEXT.getName(), "default");
            setParam(GOCA_TEXT, "stroke".equalsIgnoreCase(strokeGocaText)
                            || "shapes".equalsIgnoreCase(strokeGocaText));
            //TODO remove
            createResourceGroupFile();
            createResourceLevel();
        }

        private void setParam(AFPRendererOption option, Object value) {
            config.setParam(option, value);
        }

        private void configureImages() throws ConfigurationException, FOPException {
            Configuration imagesCfg = cfg.getChild(IMAGES.getName());
            ImagesModeOptions imagesMode = ImagesModeOptions.forName(imagesCfg.getAttribute(
                    IMAGES_MODE.getName(), MODE_GRAYSCALE.getName()));
            boolean colorImages = MODE_COLOR == imagesMode;
            setParam(IMAGES_MODE, colorImages);
            if (colorImages) {
                config.imageModeParams.put(MODE_COLOR, imagesCfg
                        .getAttributeAsBoolean(imagesMode.getModeAttribute(), false));
            } else {
                config.imageModeParams.put(MODE_GRAYSCALE,
                        imagesCfg.getAttributeAsInteger(imagesMode.getModeAttribute(), 8));
            }
            String dithering = imagesCfg.getAttribute(AFPRendererOption.IMAGES_DITHERING_QUALITY.getName(), "medium");
            float dq;
            if (dithering.startsWith("min")) {
                dq = 0.0f;
            } else if (dithering.startsWith("max")) {
                dq = 1.0f;
            } else {
                try {
                    dq = Float.parseFloat(dithering);
                } catch (NumberFormatException nfe) {
                    //Default value
                    dq = 0.5f;
                }
            }
            setParam(IMAGES_DITHERING_QUALITY, dq);
            setParam(IMAGES_NATIVE, imagesCfg.getAttributeAsBoolean(IMAGES_NATIVE.getName(), false));
            setParam(IMAGES_NATIVE_PDF, imagesCfg.getAttributeAsBoolean(IMAGES_NATIVE_PDF.getName(), false));
            setParam(IMAGES_WRAP_PSEG, imagesCfg.getAttributeAsBoolean(IMAGES_WRAP_PSEG.getName(), false));
            setParam(IMAGES_FS45, imagesCfg.getAttributeAsBoolean(IMAGES_FS45.getName(), false));
            setParam(IMAGES_MASK_ENABLED, imagesCfg.getAttributeAsBoolean(IMAGES_MASK_ENABLED.getName(), false));
            if ("scale-to-fit".equals(imagesCfg.getAttribute(IMAGES_MAPPING_OPTION.getName(), null))) {
                setParam(IMAGES_MAPPING_OPTION, MappingOptionTriplet.SCALE_TO_FILL);
            } else {
                setParam(IMAGES_MAPPING_OPTION, AFPDataObjectInfo.DEFAULT_MAPPING_OPTION);
            }
            setParam(METADATA_IN_OBJECT_CONTAINER,
                    imagesCfg.getAttributeAsBoolean(METADATA_IN_OBJECT_CONTAINER.getName(), false));
            configureJpegImages(imagesCfg);
        }

        private void configureGOCA() {
            Configuration gocaCfg = cfg.getChild(GOCA.getName());
            setParam(GOCA_WRAP_PSEG, gocaCfg.getAttributeAsBoolean(GOCA_WRAP_PSEG.getName(), false));
        }

        private void configureJpegImages(Configuration imagesCfg) {
            Configuration jpegConfig = imagesCfg.getChild(IMAGES_JPEG.getName());
            float bitmapEncodingQuality = 1.0f;
            boolean allowJpegEmbedding = false;
            boolean useIocaImages = true;
            if (jpegConfig != null) {
                allowJpegEmbedding = jpegConfig.getAttributeAsBoolean(
                        JPEG_ALLOW_JPEG_EMBEDDING.getName(),
                        false);
                useIocaImages = jpegConfig.getAttributeAsBoolean(JPEG_USE_IOCA_IMAGES.getName(), true);
                String bitmapEncodingQualityStr = jpegConfig.getAttribute(
                        JPEG_BITMAP_ENCODING_QUALITY.getName(), null);
                if (bitmapEncodingQualityStr != null) {
                    try {
                        bitmapEncodingQuality = Float.parseFloat(bitmapEncodingQualityStr);
                    } catch (NumberFormatException nfe) {
                        //ignore and leave the default above
                    }
                }
            }
            setParam(JPEG_BITMAP_ENCODING_QUALITY, bitmapEncodingQuality);
            setParam(JPEG_ALLOW_JPEG_EMBEDDING, allowJpegEmbedding);
            setParam(JPEG_USE_IOCA_IMAGES, useIocaImages);
        }

        private void createResourceGroupFile() throws FOPException {
            try {
                Configuration resourceGroupUriCfg = cfg.getChild(RESOURCE_GROUP_URI.getName(), false);
                if (resourceGroupUriCfg != null) {
                    URI resourceGroupUri = InternalResourceResolver.cleanURI(resourceGroupUriCfg.getValue());
                    setParam(RESOURCE_GROUP_URI, resourceGroupUri);
                }
            } catch (ConfigurationException e) {
                LogUtil.handleException(LOG, e, strict);
            } catch (URISyntaxException use) {
                LogUtil.handleException(LOG, use, strict);
            }
        }

        private void createResourceLevel() throws FOPException {
            Configuration defaultResourceLevelCfg = cfg.getChild(DEFAULT_RESOURCE_LEVELS.getName(), false);
            if (defaultResourceLevelCfg != null) {
                AFPResourceLevelDefaults defaults = new AFPResourceLevelDefaults();
                String[] types = defaultResourceLevelCfg.getAttributeNames();
                for (String type : types) {
                    try {
                        String level = defaultResourceLevelCfg.getAttribute(type);
                        defaults.setDefaultResourceLevel(type, AFPResourceLevel.valueOf(level));
                    } catch (IllegalArgumentException iae) {
                        LogUtil.handleException(LOG, iae, strict);
                    } catch (ConfigurationException e) {
                        LogUtil.handleException(LOG, e, strict);
                    }
                }
                setParam(DEFAULT_RESOURCE_LEVELS, defaults);
            }
        }
    }
}
