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

package org.apache.fop.pdf;

import java.text.MessageFormat;

/**
 * This class allows tracks the enabled PDF profiles (PDF/A and PDF/X) and provides methods to
 * the libarary and its users to enable the generation of PDFs conforming to the enabled PDF
 * profiles.
 * <p>
 * Some profile from PDF/X and PDF/A can be active simultaneously (example: PDF/A-1 and
 * PDF/X-3:2003).
 */
public class PDFProfile {

    /**
     * Indicates the PDF/A mode currently active. Defaults to "no restrictions", i.e.
     * PDF/A not active.
     */
    protected PDFAMode pdfAMode = PDFAMode.DISABLED;

    /**
     * Indicates the PDF/X mode currently active. Defaults to "no restrictions", i.e.
     * PDF/X not active.
     */
    protected PDFXMode pdfXMode = PDFXMode.DISABLED;

    private PDFDocument doc;

    /**
     * Main constructor
     * @param doc the PDF document
     */
    public PDFProfile(PDFDocument doc) {
        this.doc = doc;
    }

    /**
     * Validates if the requested profile combination is compatible.
     */
    protected void validateProfileCombination() {
        if (pdfAMode != PDFAMode.DISABLED) {
            if (pdfAMode == PDFAMode.PDFA_1B) {
                if (pdfXMode != PDFXMode.DISABLED && pdfXMode != PDFXMode.PDFX_3_2003) {
                    throw new PDFConformanceException(
                            pdfAMode + " and " + pdfXMode + " are not compatible!");
                }
            }
        }
    }

    /** @return the PDFDocument this profile is attached to */
    public PDFDocument getDocument() {
        return this.doc;
    }

    /** @return the PDF/A mode */
    public PDFAMode getPDFAMode() {
        return this.pdfAMode;
    }

    /** @return true if any PDF/A mode is active */
    public boolean isPDFAActive() {
        return getPDFAMode() != PDFAMode.DISABLED;
    }

    /**
     * Sets the PDF/A mode
     * @param mode the PDF/A mode
     */
    public void setPDFAMode(PDFAMode mode) {
        if (mode == null) {
            mode = PDFAMode.DISABLED;
        }
        this.pdfAMode = mode;
        validateProfileCombination();
    }

    /** @return the PDF/X mode */
    public PDFXMode getPDFXMode() {
        return this.pdfXMode;
    }

    /** @return true if any PDF/X mode is active */
    public boolean isPDFXActive() {
        return getPDFXMode() != PDFXMode.DISABLED;
    }

    /**
     * Sets the PDF/X mode
     * @param mode the PDF/X mode
     */
    public void setPDFXMode(PDFXMode mode) {
        if (mode == null) {
            mode = PDFXMode.DISABLED;
        }
        this.pdfXMode = mode;
        validateProfileCombination();
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (isPDFAActive() && isPDFXActive()) {
            sb.append("[").append(getPDFAMode()).append(",").append(getPDFXMode()).append("]");
        } else if (isPDFAActive()) {
            sb.append(getPDFAMode());
        } else if (isPDFXActive()) {
            sb.append(getPDFXMode());
        } else {
            sb.append(super.toString());
        }
        return sb.toString();
    }

    //---------=== Info and validation methods ===---------

    private String format(String pattern, Object[] args) {
        return MessageFormat.format(pattern, args);
    }

    private String format(String pattern, Object arg) {
        return format(pattern, new Object[] {arg});
    }

    /** Checks if encryption is allowed. */
    public void verifyEncryptionAllowed() {
        final String err = "{0} doesn't allow encrypted PDFs";
        if (isPDFAActive()) {
            throw new PDFConformanceException(format(err, getPDFAMode()));
        }
        if (isPDFXActive()) {
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /** Checks if PostScript XObjects are allowed. */
    public void verifyPSXObjectsAllowed() {
        final String err = "PostScript XObjects are prohibited when {0}"
                + " is active. Convert EPS graphics to another format.";
        if (isPDFAActive()) {
            throw new PDFConformanceException(format(err, getPDFAMode()));
        }
        if (isPDFXActive()) {
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /**
     * Checks if the use of transparency is allowed.
     * @param context Context information for the user to identify the problem spot
     */
    public void verifyTransparencyAllowed(String context) {
        final String err = "{0} does not allow the use of transparency. ({1})";
        if (isPDFAActive()) {
            throw new PDFConformanceException(MessageFormat.format(err,
                    new Object[] {getPDFAMode(), context}));
        }
        if (isPDFXActive()) {
            throw new PDFConformanceException(MessageFormat.format(err,
                    new Object[] {getPDFXMode(), context}));
        }
    }

    /** Checks if the right PDF version is set. */
    public void verifyPDFVersion() {
        final String err = "PDF version must be 1.4 for {0}";
        if (getPDFAMode().isPDFA1LevelB()
                && getDocument().getPDFVersion() != PDFDocument.PDF_VERSION_1_4) {
            throw new PDFConformanceException(format(err, getPDFAMode()));
        }
        if (getPDFXMode() == PDFXMode.PDFX_3_2003
                && getDocument().getPDFVersion() != PDFDocument.PDF_VERSION_1_4) {
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /**
     * Checks a few things required for tagged PDF.
     */
    public void verifyTaggedPDF() {
        if (getPDFAMode().isPDFA1LevelA()) {
            final String err = "{0} requires the {1} dictionary entry to be set";
            PDFDictionary markInfo = getDocument().getRoot().getMarkInfo();
            if (markInfo == null) {
                throw new PDFConformanceException(format(
                        "{0} requires the MarkInfo dictionary to be present", getPDFAMode()));
            }
            if (!Boolean.TRUE.equals(markInfo.get("Marked"))) {
                throw new PDFConformanceException(format(err,
                        new Object[] {getPDFAMode(), "Marked"}));
            }
            if (getDocument().getRoot().getStructTreeRoot() == null) {
                throw new PDFConformanceException(format(err,
                        new Object[] {getPDFAMode(), "StructTreeRoot"}));
            }
            if (getDocument().getRoot().getLanguage() == null) {
                throw new PDFConformanceException(format(err,
                        new Object[] {getPDFAMode(), "Lang"}));
            }
        }
    }

    /** @return true if the ID entry must be present in the trailer. */
    public boolean isIDEntryRequired() {
        return isPDFAActive() || isPDFXActive();
    }

    /** @return true if all fonts need to be embedded. */
    public boolean isFontEmbeddingRequired() {
        return isPDFAActive() || isPDFXActive();
    }

    /** Checks if a title may be absent. */
    public void verifyTitleAbsent() {
        if (isPDFXActive()) {
            final String err = "{0} requires the title to be set.";
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /** @return true if the ModDate Info entry must be present. */
    public boolean isModDateRequired() {
        return getPDFXMode() == PDFXMode.PDFX_3_2003;
    }

    /** @return true if the Trapped Info entry must be present. */
    public boolean isTrappedEntryRequired() {
        return getPDFXMode() == PDFXMode.PDFX_3_2003;
    }

    /** @return true if annotations are allowed */
    public boolean isAnnotationAllowed() {
        return !isPDFXActive();
    }

    /** Checks if annotations are allowed. */
    public void verifyAnnotAllowed() {
        if (!isAnnotationAllowed()) {
            final String err = "{0} does not allow annotations inside the printable area.";
            //Note: this rule is simplified. Refer to the standard for details.
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /** Checks if Actions are allowed. */
    public void verifyActionAllowed() {
        if (isPDFXActive()) {
            final String err = "{0} does not allow Actions.";
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

    /** Checks if embedded files are allowed. */
    public void verifyEmbeddedFilesAllowed() {
        final String err = "{0} does not allow embedded files.";
        if (isPDFAActive()) {
            throw new PDFConformanceException(format(err, getPDFAMode()));
        }
        if (isPDFXActive()) {
            //Implicit since file specs are forbidden
            throw new PDFConformanceException(format(err, getPDFXMode()));
        }
    }

}
