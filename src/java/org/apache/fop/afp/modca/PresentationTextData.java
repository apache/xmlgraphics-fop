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

package org.apache.fop.afp.modca;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

import org.apache.fop.afp.ptoca.PtocaBuilder;
import org.apache.fop.afp.ptoca.PtocaConstants;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * Presentation text data contains the graphic characters and the control
 * sequences necessary to position the characters within the object space. The
 * data consists of: - graphic characters to be presented - control sequences
 * that position them - modal control sequences that adjust the positions by
 * small amounts - other functions causing text to be presented with differences
 * in appearance.
 * <p>
 * The graphic characters are expected to conform to a coded font representation
 * so that they can be translated from the code point in the object data to the
 * character in the coded font. The units of measure for linear displacements
 * are derived from the PresentationTextDescriptor or from the hierarchical
 * defaults.
 * <p>
 * In addition to graphic character code points, Presentation Text data can
 * contain embedded control sequences. These are strings of two or more bytes
 * which signal an alternate mode of processing for the content of the current
 * Presentation Text data.
 * <p>
 * The content for this object can be created using {@link PtocaBuilder}.
 */
public class PresentationTextData extends AbstractAFPObject implements PtocaConstants {

    /** the maximum size of the presentation text data.*/
    private static final int MAX_SIZE = 8192;

    /** the AFP data relating to this presentation text data. */
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /**
     * Default constructor for the PresentationTextData.
     */
    public PresentationTextData() {
        this(false);
    }

    private static final int HEADER_LENGTH = 9;

    /**
     * Constructor for the PresentationTextData, the boolean flag indicate
     * whether the control sequence prefix should be set to indicate the start
     * of a new control sequence.
     *
     * @param controlInd
     *            The control sequence indicator.
     */
    public PresentationTextData(boolean controlInd) {
        final byte[] data = {
                0x5A, // Structured field identifier
                0x00, // Record length byte 1
                0x00, // Record length byte 2
                SF_CLASS, // PresentationTextData identifier byte 1
                Type.DATA, // PresentationTextData identifier byte 2
                Category.PRESENTATION_TEXT, // PresentationTextData identifier byte 3
                0x00, // Flag
                0x00, // Reserved
                0x00, // Reserved
        };
        baos.write(data, 0, HEADER_LENGTH);

        if (controlInd) {
            baos.write(new byte[] {0x2B, (byte) 0xD3}, 0, 2);
        }
    }

    /**
     * Returns the number of data bytes still available in this object until it is full and a new
     * one has to be started.
     * @return the number of data bytes available
     */
    public int getBytesAvailable() {
        return MAX_SIZE - baos.size() + HEADER_LENGTH;
    }

    /**
     * Returns the output stream the content data is written to.
     * @return the output stream
     */
    protected OutputStream getOutputStream() {
        return this.baos;
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        assert getBytesAvailable() >= 0;
        byte[] data = baos.toByteArray();
        byte[] size = BinaryUtils.convert(data.length - 1, 2);
        data[1] = size[0];
        data[2] = size[1];
        os.write(data);
    }

}