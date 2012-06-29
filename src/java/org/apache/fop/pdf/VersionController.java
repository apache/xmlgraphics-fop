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

/**
 * An abstraction that controls the mutability of the PDF version for a document.
 */
public abstract class VersionController {

    private Version version;

    private VersionController(Version version) {
        this.version = version;
    }

    /**
     * Returns the PDF version of the document.
     *
     * @return the PDF version
     */
    public Version getPDFVersion() {
        return version;
    }

    /**
     * Sets the PDF version of the document.
     *
     * @param version the PDF version
     * @throws IllegalStateException if the PDF version is not allowed to change.
     */
    public abstract void setPDFVersion(Version version);

    @Override
    public String toString() {
        return version.toString();
    }

    /**
     * A class representing the version of a PDF document. This class doesn't allow the version to
     * change once it has been set, it is immutable. Any attempt to set the version will result in
     * an exception being thrown.
     */
    private static final class FixedVersion extends VersionController {

        private FixedVersion(Version version) {
            super(version);
        }

        @Override
        public void setPDFVersion(Version version) {
            throw new IllegalStateException("Cannot change the version of this PDF document.");
        }
    }

    /**
     * A class representing the version of a PDF document. This class allows the version to be
     * changed once it has been set (it is mutable) ONLY if the new version is greater. If the PDF
     * version is changed after it has been instantiated, the version will be set in the document
     * catalog.
     */
    private static final class DynamicVersion extends VersionController {

        private PDFDocument doc;

        private DynamicVersion(Version version, PDFDocument doc) {
            super(version);
            this.doc = doc;
        }

        @Override
        public void setPDFVersion(Version version) {
            if (super.version.compareTo(version) < 0) {
                super.version = version;
                doc.getRoot().setVersion(version);
            }
        }
    }

    /**
     * Returns a controller that disallows subsequent change to the document's version. The minimum
     * allowed version is v1.4.
     *
     * @param version the PDF version (must be &gt;= v1.4)
     * @return the fixed PDF version controller
     */
    public static VersionController getFixedVersionController(Version version) {
        if (version.compareTo(Version.V1_4) < 0) {
            throw new IllegalArgumentException("The PDF version cannot be set below version 1.4");
        }
        return new FixedVersion(version);
    }

    /**
     * Returns a controller that allows subsequent changes to the document's version.
     *
     * @param initialVersion the initial PDF version
     * @param doc the document whose version is being set
     * @return the dynamic PDF version controller
     */
    public static VersionController getDynamicVersionController(Version initialVersion,
            PDFDocument doc) {
        return new DynamicVersion(initialVersion, doc);
    }
}
