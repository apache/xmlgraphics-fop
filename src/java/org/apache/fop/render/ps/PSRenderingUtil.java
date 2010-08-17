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

package org.apache.fop.render.ps;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlgraphics.ps.PSGenerator;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.ps.extensions.PSCommentAfter;
import org.apache.fop.render.ps.extensions.PSCommentBefore;
import org.apache.fop.render.ps.extensions.PSExtensionAttachment;
import org.apache.fop.render.ps.extensions.PSSetupCode;

/**
 * Utility class which enables all sorts of features that are not directly connected to the
 * normal rendering process.
 */
public class PSRenderingUtil implements PSConfigurationConstants {

    private FOUserAgent userAgent;

    /** Whether or not the safe set page device macro will be used or not */
    private boolean safeSetPageDevice = false;

    /**
     * Whether or not PostScript Document Structuring Conventions (DSC) compliant output are
     * enforced.
     */
    private boolean dscCompliant = true;

    private boolean autoRotateLandscape = false;
    private int languageLevel = PSGenerator.DEFAULT_LANGUAGE_LEVEL;

    /** Determines whether the PS file is generated in two passes to minimize file size */
    private boolean optimizeResources = false;

    PSRenderingUtil(FOUserAgent userAgent) {
        this.userAgent = userAgent;
        initialize();
    }

    private void initialize() {
        Object obj;
        obj = userAgent.getRendererOptions().get(AUTO_ROTATE_LANDSCAPE);
        if (obj != null) {
            setAutoRotateLandscape(booleanValueOf(obj));
        }
        obj = userAgent.getRendererOptions().get(LANGUAGE_LEVEL);
        if (obj != null) {
            setLanguageLevel(intValueOf(obj));
        }
        obj = userAgent.getRendererOptions().get(OPTIMIZE_RESOURCES);
        if (obj != null) {
            setOptimizeResources(booleanValueOf(obj));
        }
    }

    private boolean booleanValueOf(Object obj) {
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        } else if (obj instanceof String) {
            return Boolean.valueOf((String)obj).booleanValue();
        } else {
            throw new IllegalArgumentException("Boolean or \"true\" or \"false\" expected.");
        }
    }

    private int intValueOf(Object obj) {
        if (obj instanceof Integer) {
            return ((Integer)obj).intValue();
        } else if (obj instanceof String) {
            return Integer.parseInt((String)obj);
        } else {
            throw new IllegalArgumentException("Integer or String with a number expected.");
        }
    }

    /**
     * Formats and writes a List of PSSetupCode instances to the output stream.
     * @param gen the PS generator
     * @param setupCodeList a List of PSSetupCode instances
     * @param type the type of code section
     * @throws IOException if an I/O error occurs.
     */
    public static void writeSetupCodeList(PSGenerator gen, List setupCodeList, String type)
            throws IOException {
        if (setupCodeList != null) {
            Iterator i = setupCodeList.iterator();
            while (i.hasNext()) {
                PSSetupCode setupCode = (PSSetupCode)i.next();
                gen.commentln("%FOPBegin" + type + ": ("
                        + (setupCode.getName() != null ? setupCode.getName() : "")
                        + ")");
                LineNumberReader reader = new LineNumberReader(
                        new java.io.StringReader(setupCode.getContent()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.length() > 0) {
                        gen.writeln(line.trim());
                    }
                }
                gen.commentln("%FOPEnd" + type);
                i.remove();
            }
        }
    }

    /**
     * Formats and writes a Collection of PSExtensionAttachment instances to
     * the output stream. The instances are removed from the collection when they
     * have been written.
     *
     * @param gen the PS generator
     * @param attachmentCollection
     *            a Collection of PSExtensionAttachment instances
     * @throws IOException if an I/O error occurs.
     */
    public static void writeEnclosedExtensionAttachments(PSGenerator gen,
            Collection attachmentCollection) throws IOException {
        Iterator iter = attachmentCollection.iterator();
        while (iter.hasNext()) {
            PSExtensionAttachment attachment = (PSExtensionAttachment)iter.next();
            if (attachment != null) {
                writeEnclosedExtensionAttachment(gen, attachment);
            }
            iter.remove();
        }
    }

    /**
     * Formats and writes a PSExtensionAttachment to the output stream.
     *
     * @param gen the PS generator
     * @param attachment an PSExtensionAttachment instance
     * @throws IOException if an I/O error occurs.
     */
    public static void writeEnclosedExtensionAttachment(PSGenerator gen,
                PSExtensionAttachment attachment) throws IOException {
        if (attachment instanceof PSCommentBefore) {
            gen.commentln("%" + attachment.getContent());
        } else if (attachment instanceof PSCommentAfter) {
            gen.commentln("%" + attachment.getContent());
        } else {
            String info = "";
            if (attachment instanceof PSSetupCode) {
                PSSetupCode setupCodeAttach = (PSSetupCode)attachment;
                String name = setupCodeAttach.getName();
                if (name != null) {
                    info += ": (" + name + ")";
                }
            }
            String type = attachment.getType();
            gen.commentln("%FOPBegin" + type + info);
            LineNumberReader reader = new LineNumberReader(
                    new java.io.StringReader(attachment.getContent()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    gen.writeln(line);
                }
            }
            gen.commentln("%FOPEnd" + type);
        }
    }

    /**
     * Sets whether or not PostScript Document Structuring Conventions (DSC) compliance are
     * enforced.
     * <p>
     * It can cause problems (unwanted PostScript subsystem initgraphics/erasepage calls)
     * on some printers when the pagedevice is set.  If this causes problems on a
     * particular implementation then use this setting with a 'false' value to try and
     * minimize the number of setpagedevice calls in the PostScript document output.
     * <p>
     * Set this value to false if you experience unwanted blank pages in your
     * PostScript output.
     * @param value boolean value (default is true)
     */
    public void setSafeSetPageDevice(boolean value) {
        this.safeSetPageDevice = value;
    }

    /**
     * Indicates whether the "safe setpagedevice" mode is active.
     * See {@link #setSafeSetPageDevice(boolean)} for more information.
     * @return true if active
     */
    public boolean isSafeSetPageDevice() {
        return this.safeSetPageDevice;
    }

    /**
     * Sets whether or not the safe set page device macro should be used
     * (as opposed to directly invoking setpagedevice) when setting the
     * PostScript page device.
     * <p>
     * This option is a useful option when you want to guard against the possibility
     * of invalid/unsupported PostScript key/values being placed in the page device.
     * <p>
     * @param value setting to false and the renderer will make a
     *          standard "setpagedevice" call, setting to true will make a safe set page
     *          device macro call (default is false).
     */
    public void setDSCComplianceEnabled(boolean value) {
        this.dscCompliant = value;
    }

    /** @return true if DSC complicance is enabled */
    public boolean isDSCComplianceEnabled() {
        return this.dscCompliant;
    }

    /**
     * Controls whether landscape pages should be rotated.
     * @param value true to enable the rotation
     */
    public void setAutoRotateLandscape(boolean value) {
        this.autoRotateLandscape = value;
    }

    /**
     * Indicates whether landscape pages are rotated.
     * @return true if landscape pages are to be rotated
     */
    public boolean isAutoRotateLandscape() {
        return autoRotateLandscape;
    }

    /**
     * Sets the PostScript language level.
     * @param level the PostScript language level (Only 2 and 3 are currently supported)
     */
    public void setLanguageLevel(int level) {
        if (level == 2 || level == 3) {
            this.languageLevel = level;
        } else {
            throw new IllegalArgumentException("Only language levels 2 or 3 are allowed/supported");
        }
    }

    /**
     * Indicates the selected PostScript language level.
     * @return the PostScript language level
     */
    public int getLanguageLevel() {
        return languageLevel;
    }

    /**
     * Controls whether PostScript resources are optimized in a second pass over the document.
     * Enable this to obtain smaller PostScript files.
     * @param value true to enable resource optimization
     */
    public void setOptimizeResources(boolean value) {
        this.optimizeResources = value;
    }

    /**
     * Indicates whether PostScript resources are optimized in a second pass over the document.
     * @return true if resource optimization is enabled
     */
    public boolean isOptimizeResources() {
        return optimizeResources;
    }


}
