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
 * This class represents the /Launch action.
 */
public class PDFLaunch extends PDFAction {

    private PDFReference externalFileSpec;

    /**
     * Creates a new /Launch action.
     * @param fileSpec the file specification to launch
     */
    public PDFLaunch(PDFFileSpec fileSpec) {
        this(fileSpec.makeReference());
    }

    /**
     * Creates a new /Launch action.
     * @param fileSpec a reference to the file specification
     */
    public PDFLaunch(PDFReference fileSpec) {
        PDFObject fs = fileSpec.getObject();
        if (fs != null) {
            assert fs instanceof PDFFileSpec;
        }
        this.externalFileSpec = fileSpec;
    }

    /** {@inheritDoc} */
    public String getAction() {
        return this.referencePDF();
    }

    /** {@inheritDoc} */
    public String toPDFString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getObjectID());
        sb.append("<<\n/S /Launch\n/F ");
        sb.append(externalFileSpec.toString());
        sb.append(" \n>>\nendobj\n");

        return sb.toString();
    }

    /** {@inheritDoc} */
    protected boolean contentEquals(PDFObject obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PDFLaunch)) {
            return false;
        }

        PDFLaunch launch = (PDFLaunch) obj;

        if (!launch.externalFileSpec.toString().equals(externalFileSpec.toString())) {
            return false;
        }

        return true;
    }
}
