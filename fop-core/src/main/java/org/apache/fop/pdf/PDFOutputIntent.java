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

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Represents the OutputIntent dictionary.
 * @since PDF 1.4
 */
public class PDFOutputIntent extends PDFObject {

    /** Subtype for PDF/X output intents */
    public static final String GTS_PDFX = "GTS_PDFX";
    /** Subtype for PDF/A-1 output intents */
    public static final String GTS_PDFA1 = "GTS_PDFA1";

    private String subtype; //S in the PDF spec
    private String outputCondition;
    private String outputConditionIdentifier;
    private String registryName;
    private String info;
    private PDFICCStream destOutputProfile;


    /** @return the output intent subtype. */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Sets the output intent subtype.
     * @param subtype the subtype (usually "GTS_PDFX")
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /** @return the OutputCondition field */
    public String getOutputCondition() {
        return outputCondition;
    }

    /**
     * Sets the human-readable form of the output condition.
     * @param outputCondition A text string concisely identifying the intended output
     *                        device or production condition in human-readable form.
     */
    public void setOutputCondition(String outputCondition) {
        this.outputCondition = outputCondition;
    }

    /** @return the OutputConditionIdentifier field */
    public String getOutputConditionIdentifier() {
        return outputConditionIdentifier;
    }

    /**
     * Sets the identifier for the output condition.
     * @param outputConditionIdentifier A string identifying the intended output device or
     *                                  production condition in human- or machine-readable form.
     */
    public void setOutputConditionIdentifier(String outputConditionIdentifier) {
        this.outputConditionIdentifier = outputConditionIdentifier;
    }

    /** @return the RegistryName field */
    public String getRegistryName() {
        return registryName;
    }

    /**
     * Sets the registry name.
     * @param registryName A string (conventionally a uniform resource identifier,
     *                     or URI) identifying the registry in which the condition designated
     *                     by OutputConditionIdentifier is defined.
     */
    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }

    /** @return the Info field */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the Info field.
     * @param info A human-readable text string containing additional information or comments about
     *             the intended target device or production condition.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /** @return the DestOutputProfile */
    public PDFICCStream getDestOutputProfile() {
        return destOutputProfile;
    }

    /**
     * Sets the destination ICC profile.
     * @param destOutputProfile An ICC profile stream defining the transformation from the PDF
     *                          document's source colors to output device colorants.
     */
    public void setDestOutputProfile(PDFICCStream destOutputProfile) {
        this.destOutputProfile = destOutputProfile;
    }

    /** {@inheritDoc} */
    public byte[] toPDF() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(128);
        try {
            bout.write(encode("<<\n"));
            bout.write(encode("/Type /OutputIntent\n"));

            bout.write(encode("/S /"));
            bout.write(encode(this.subtype));
            bout.write(encode("\n"));

            if (outputCondition != null) {
                bout.write(encode("/OutputCondition "));
                bout.write(encodeText(this.outputCondition));
                bout.write(encode("\n"));
            }

            bout.write(encode("/OutputConditionIdentifier "));
            bout.write(encodeText(this.outputConditionIdentifier));
            bout.write(encode("\n"));

            if (registryName != null) {
                bout.write(encode("/RegistryName "));
                bout.write(encodeText(this.registryName));
                bout.write(encode("\n"));
            }

            if (info != null) {
                bout.write(encode("/Info "));
                bout.write(encodeText(this.info));
                bout.write(encode("\n"));
            }

            if (destOutputProfile != null) {
                bout.write(encode("/DestOutputProfile " + destOutputProfile.referencePDF() + "\n"));
            }

            bout.write(encode(">>"));
        } catch (IOException ioe) {
            log.error("Ignored I/O exception", ioe);
        }
        byte[] bytes = bout.toByteArray();
        IOUtils.closeQuietly(bout);
        return bytes;
    }


}
