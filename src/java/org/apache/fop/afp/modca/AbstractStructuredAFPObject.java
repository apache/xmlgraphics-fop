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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.fop.afp.modca.Registry.ObjectType;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.afp.modca.triplets.Triplet;
import org.apache.fop.afp.util.BinaryUtils;

/**
 * An abstract class encapsulating an MODCA structured object
 */
public abstract class AbstractStructuredAFPObject extends AbstractAFPObject {
    /**
     * list of object triplets
     */
    protected List/*<Triplet>*/ triplets = null;

    /**
     * triplet data created from triplet list
     */
    protected byte[] tripletData = null;

    /**
     * Default constructor
     */
    protected AbstractStructuredAFPObject() {
    }

    /**
     * Returns the triplet data length
     *
     * @return the triplet data length
     */
    protected int getTripletDataLength() {
        if (tripletData == null) {
            try {
                getTripletData();
            } catch (IOException e) {
                log.error("failed to get triplet data");
            }
        }
        if (tripletData != null) {
            return tripletData.length;
        }
        return 0;
    }

    /**
     * Returns the triplet data
     *
     * @return the triplet data
     * @throws IOException throws an I/O exception if one occurred
     */
    protected byte[] getTripletData() throws IOException {
        if (tripletData == null && triplets != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeObjects(triplets, baos);
            this.tripletData = baos.toByteArray();
            triplets = null; // gc
        }
        return this.tripletData;
    }

    /**
     * Writes any triplet data
     *
     * @param os The stream to write to
     * @throws IOException The stream to write to
     */
    protected void writeTriplets(OutputStream os) throws IOException {
        if (tripletData != null) {
            os.write(tripletData);
        } else if (triplets != null) {
            writeObjects(triplets, os);
            triplets = null; // gc
        }
    }

    /**
     * Helper method to write the start of the Object.
     *
     * @param os The stream to write to
     * @throws IOException throws an I/O exception if one occurred
     */
    protected void writeStart(OutputStream os) throws IOException {
        getTripletData();
    }

    /**
     * Helper method to write the end of the Object.
     *
     * @param os The stream to write to
     * @throws IOException an I/O exception if one occurred
     */
    protected void writeEnd(OutputStream os) throws IOException {
    }

    /**
     * Helper method to write the contents of the Object.
     *
     * @param os The stream to write to
     * @throws IOException throws an I/O exception if one occurred
     */
    protected void writeContent(OutputStream os) throws IOException {
        writeTriplets(os);
    }

    /** {@inheritDoc} */
    public void writeToStream(OutputStream os) throws IOException {
        writeStart(os);
        writeContent(os);
        writeEnd(os);
    }

    /**
     * Returns the first matching triplet found in the structured field triplet list
     *
     * @param tripletId the triplet identifier
     */
    private Triplet getTriplet(byte tripletId) {
        Iterator it = getTriplets().iterator();
        while (it.hasNext()) {
            Triplet triplet = (Triplet)it.next();
            if (triplet.getId() == tripletId) {
                return triplet;
            }
        }
        return null;
    }

    /**
     * Returns true of this structured field has the given triplet
     *
     * @param tripletId the triplet identifier
     * @return true if the structured field has the given triplet
     */
    public boolean hasTriplet(byte tripletId) {
        return getTriplet(tripletId) != null;
    }

    /**
     * Adds a triplet to this structured object
     *
     * @param triplet the triplet to add
     */
    protected void addTriplet(Triplet triplet) {
        getTriplets().add(triplet);
    }

    /**
     * Adds a list of triplets to the triplets contained within this structured field
     *
     * @param tripletCollection a collection of triplets
     */
    public void addTriplets(Collection/*<Triplet>*/ tripletCollection) {
        if (tripletCollection != null) {
            getTriplets().addAll(tripletCollection);
        }
    }

    /** @return the triplet list pertaining to this resource */
    protected List/*<Triplet>*/ getTriplets() {
        if (triplets == null) {
            triplets = new java.util.ArrayList();
        }
        return triplets;
    }

    /**
     * Sets the fully qualified name of this resource
     *
     * @param fqnType the fully qualified name type of this resource
     * @param fqnFormat the fully qualified name format of this resource
     * @param fqName the fully qualified name of this resource
     */
    public void setFullyQualifiedName(byte fqnType, byte fqnFormat, String fqName) {
        addTriplet(new FullyQualifiedNameTriplet(fqnType, fqnFormat, fqName));
    }

    /** @return the fully qualified name of this triplet or null if it does not exist */
    public String getFullyQualifiedName() {
        FullyQualifiedNameTriplet fqNameTriplet
            = (FullyQualifiedNameTriplet)getTriplet(Triplet.FULLY_QUALIFIED_NAME);
        if (fqNameTriplet != null) {
            return fqNameTriplet.getFullyQualifiedName();
        }
        log.warn(this + " has no fully qualified name");
        return null;
    }

    /**
     * Sets the objects classification
     *
     * @param objectClass the classification of the object
     * @param objectType the MOD:CA registry object type entry for the given
     *        object/component type of the object
     * @param dataInContainer whether the data resides in the container
     * @param containerHasOEG whether the container has an object environment group
     * @param dataInOCD whether the data resides in a object container data structured field
     */
    public void setObjectClassification(
            byte objectClass, ObjectType objectType,
            boolean dataInContainer, boolean containerHasOEG, boolean dataInOCD) {
        addTriplet(
                new ObjectClassificationTriplet(
                        objectClass, objectType, dataInContainer, containerHasOEG, dataInOCD));
    }

    /**
     * Sets a comment on this resource
     *
     * @param comment a comment string
     */
    public void setComment(String comment) {
        try {
            addTriplet(new Triplet(Triplet.COMMENT, comment));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Reads data chunks from an inputstream
     * and then formats them with a structured header to a given outputstream
     *
     * @param dataHeader the header data
     * @param lengthOffset offset of length field in data chunk
     * @param maxChunkLength the maximum chunk length
     * @param inputStream the inputstream to read from
     * @param outputStream the outputstream to write to
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    protected static void copyChunks(byte[] dataHeader, int lengthOffset,
            int maxChunkLength, InputStream inputStream, OutputStream outputStream)
    throws IOException {
        int headerLen = dataHeader.length - lengthOffset;
        // length field is just before data so do not include in data length
        if (headerLen == 2) {
            headerLen = 0;
        }
        byte[] data = new byte[maxChunkLength];
        int numBytesRead = 0;
        while ((numBytesRead = inputStream.read(data, 0, maxChunkLength)) > 0) {
            byte[] len = BinaryUtils.convert(headerLen + numBytesRead, 2);
            dataHeader[lengthOffset] = len[0]; // Length byte 1
            dataHeader[lengthOffset + 1] = len[1]; // Length byte 2
            outputStream.write(dataHeader);
            outputStream.write(data, 0, numBytesRead);
        }
    }

    /**
     * Writes data chunks to a given outputstream
     *
     * @param data the data byte array
     * @param dataHeader the header data
     * @param lengthOffset offset of length field in data chunk
     * @param maxChunkLength the maximum chunk length
     * @param os the outputstream to write to
     * @throws IOException thrown if an I/O exception of some sort has occurred.
     */
    protected static void writeChunksToStream(byte[] data, byte[] dataHeader,
            int lengthOffset, int maxChunkLength, OutputStream os) throws IOException {
        int dataLength = data.length;
        int numFullChunks = dataLength / maxChunkLength;
        int lastChunkLength = dataLength % maxChunkLength;

        int headerLen = dataHeader.length - lengthOffset;
        // length field is just before data so do not include in data length
        if (headerLen == 2) {
            headerLen = 0;
        }

        byte[] len;
        int off = 0;
        if (numFullChunks > 0) {
            // write out full data chunks
            len = BinaryUtils.convert(headerLen + maxChunkLength, 2);
            dataHeader[lengthOffset] = len[0]; // Length byte 1
            dataHeader[lengthOffset + 1] = len[1]; // Length byte 2
            for (int i = 0; i < numFullChunks; i++, off += maxChunkLength) {
                os.write(dataHeader);
                os.write(data, off, maxChunkLength);
            }
        }

        if (lastChunkLength > 0) {
            // write last data chunk
            len = BinaryUtils.convert(headerLen + lastChunkLength, 2);
            dataHeader[lengthOffset] = len[0]; // Length byte 1
            dataHeader[lengthOffset + 1] = len[1]; // Length byte 2
            os.write(dataHeader);
            os.write(data, off, lastChunkLength);
        }
    }
}
