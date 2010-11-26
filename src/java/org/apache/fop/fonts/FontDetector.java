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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.util.LogUtil;
import org.apache.xmlgraphics.util.ClasspathResource;

/**
 * Detector of operating system and classpath fonts
 */
public class FontDetector {
    private static Log log = LogFactory.getLog(FontDetector.class);

    private static final String[] FONT_MIMETYPES = {
        "application/x-font", "application/x-font-truetype"
    };

    private FontManager fontManager;
    private FontAdder fontAdder;
    private boolean strict;

    /**
     * Main constructor
     * @param manager the font manager
     * @param adder the font adder
     * @param strict true if an Exception should be thrown if an error is found.
     */
    public FontDetector(FontManager manager, FontAdder adder, boolean strict) {
        this.fontManager = manager;
        this.fontAdder = adder;
        this.strict = strict;
    }

    /**
     * Detect installed fonts on the system
     * @param fontInfoList a list of fontinfo to populate
     * @throws FOPException thrown if a problem occurred during detection
     */
    public void detect(List<EmbedFontInfo> fontInfoList) throws FOPException {
        // search in font base if it is defined and
        // is a directory but don't recurse
        FontFileFinder fontFileFinder = new FontFileFinder();
        String fontBaseURL = fontManager.getFontBaseURL();
        if (fontBaseURL != null) {
            try {
                File fontBase = FileUtils.toFile(new URL(fontBaseURL));
                if (fontBase != null) {
                    List/*<URL>*/ fontURLList = fontFileFinder.find(
                            fontBase.getAbsolutePath());
                    fontAdder.add(fontURLList, fontInfoList);

                    //Can only use the font base URL if it's a file URL
                }
            } catch (IOException e) {
                LogUtil.handleException(log, e, strict);
            }
        }

        // native o/s font directory finding
        List/*<URL>*/ systemFontList;
        try {
            systemFontList = fontFileFinder.find();
            fontAdder.add(systemFontList, fontInfoList);
        } catch (IOException e) {
            LogUtil.handleException(log, e, strict);
        }

        // classpath font finding
        ClasspathResource resource = ClasspathResource.getInstance();
        for (int i = 0; i < FONT_MIMETYPES.length; i++) {
            fontAdder.add(resource.listResourcesOfMimeType(FONT_MIMETYPES[i]), fontInfoList);
        }
    }
}
