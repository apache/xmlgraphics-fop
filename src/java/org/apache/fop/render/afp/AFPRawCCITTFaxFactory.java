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

/* $Id: $ */

package org.apache.fop.render.afp;

import java.io.IOException;

import org.apache.xmlgraphics.image.loader.impl.ImageRawCCITTFax;

/**
 * An CITT fax image configurator
 */
public class AFPRawCCITTFaxFactory extends AFPAbstractImageFactory {

    /**
     * Main constructor
     *
     * @param state the afp state
     */
    public AFPRawCCITTFaxFactory(AFPState state) {
        super(state);
    }

    /** {@inheritDoc} */
    public AFPDataObjectInfo create(AFPImageInfo afpImageInfo) throws IOException {
        AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)super.create(afpImageInfo);

        ImageRawCCITTFax ccitt = (ImageRawCCITTFax) afpImageInfo.img;
        imageObjectInfo.setCompression(ccitt.getCompression());

        AFPObjectAreaInfo objectAreaInfo = imageObjectInfo.getObjectAreaInfo();
        int xresol = (int) (ccitt.getSize().getDpiHorizontal() * 10);
        objectAreaInfo.setWidthRes(xresol);

        int yresol = (int) (ccitt.getSize().getDpiVertical() * 10);
        objectAreaInfo.setHeightRes(yresol);

        imageObjectInfo.setInputStream(ccitt.createInputStream());

        return imageObjectInfo;
    }

    /** {@inheritDoc} */
    protected AFPDataObjectInfo createDataObjectInfo() {
        return new AFPImageObjectInfo();
    }
}