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

package org.apache.fop.afp.ptoca;

import java.io.IOException;

import org.apache.fop.afp.AFPTextDataInfo;
import org.apache.fop.afp.fonts.CharactersetEncoder;

/**
 * {@link PtocaProducer} implementation that interprets {@link AFPTextDataInfo} objects.
 */
public class TextDataInfoProducer implements PtocaProducer, PtocaConstants {

    private AFPTextDataInfo textDataInfo;

    /**
     * Main constructor.
     * @param textDataInfo the info object
     */
    public TextDataInfoProducer(AFPTextDataInfo textDataInfo) {
        this.textDataInfo = textDataInfo;
    }

    /** {@inheritDoc} */
    public void produce(PtocaBuilder builder) throws IOException {
        builder.setTextOrientation(textDataInfo.getRotation());
        builder.absoluteMoveBaseline(textDataInfo.getY());
        builder.absoluteMoveInline(textDataInfo.getX());

        builder.setVariableSpaceCharacterIncrement(
                textDataInfo.getVariableSpaceCharacterIncrement());
        builder.setInterCharacterAdjustment(
                textDataInfo.getInterCharacterAdjustment());
        builder.setExtendedTextColor(textDataInfo.getColor());
        builder.setCodedFont((byte)textDataInfo.getFontReference());


        // Add transparent data
        String textString = textDataInfo.getString();
        String encoding = textDataInfo.getEncoding();
        builder.addTransparentData(CharactersetEncoder.encodeSBCS(textString, encoding, false));
    }

}
