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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.fop.afp.modca.Registry.ObjectType;
import org.apache.fop.afp.modca.triplets.AbstractTriplet;
import org.apache.fop.afp.modca.triplets.CommentTriplet;
import org.apache.fop.afp.modca.triplets.FullyQualifiedNameTriplet;
import org.apache.fop.afp.modca.triplets.ObjectClassificationTriplet;
import org.apache.fop.afp.modca.triplets.Triplet;

/**
 * A MODCA structured object base class providing support for Triplets
 */
public class AbstractTripletStructuredObject extends AbstractStructuredObject {

    /** list of object triplets */
    protected List/*<Triplet>*/ triplets = new java.util.ArrayList/*<Triplet>*/();

    /**
     * Returns the triplet data length
     *
     * @return the triplet data length
     */
    protected int getTripletDataLength() {
        int dataLength = 0;
        if (hasTriplets()) {
            Iterator it = triplets.iterator();
            while (it.hasNext()) {
                AbstractTriplet triplet = (AbstractTriplet)it.next();
                dataLength += triplet.getDataLength();
            }
        }
        return dataLength;
    }

    /**
     * Returns true when this structured field contains triplets
     *
     * @return true when this structured field contains triplets
     */
    public boolean hasTriplets() {
        return triplets.size() > 0;
    }

    /**
     * Writes any triplet data
     *
     * @param os The stream to write to
     * @throws IOException The stream to write to
     */
    protected void writeTriplets(OutputStream os) throws IOException {
        if (hasTriplets()) {
            writeObjects(triplets, os);
            triplets = null; // gc
        }
    }

    /**
     * Returns the first matching triplet found in the structured field triplet list
     *
     * @param tripletId the triplet identifier
     */
    private AbstractTriplet getTriplet(byte tripletId) {
        Iterator it = getTriplets().iterator();
        while (it.hasNext()) {
            AbstractTriplet triplet = (AbstractTriplet)it.next();
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
        triplets.add(triplet);
    }

    /**
     * Adds a list of triplets to the triplets contained within this structured field
     *
     * @param tripletCollection a collection of triplets
     */
    public void addTriplets(Collection/*<Triplet>*/ tripletCollection) {
        if (tripletCollection != null) {
            triplets.addAll(tripletCollection);
        }
    }

    /** @return the triplet list pertaining to this resource */
    protected List/*<Triplet>*/ getTriplets() {
        return triplets;
    }

    /**
     * Sets the fully qualified name of this structured field
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
            = (FullyQualifiedNameTriplet)getTriplet(AbstractTriplet.FULLY_QUALIFIED_NAME);
        if (fqNameTriplet != null) {
            return fqNameTriplet.getFullyQualifiedName();
        }
        LOG.warn(this + " has no fully qualified name");
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
     * @param commentString a comment string
     */
    public void setComment(String commentString) {
        addTriplet(new CommentTriplet(AbstractTriplet.COMMENT, commentString));
    }

}
