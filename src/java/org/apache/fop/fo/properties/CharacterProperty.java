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

package org.apache.fop.fo.properties;

import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;

/**
 * Superclass for properties that wrap a character value
 * TODO convert character value to int in order to denote unicode scalar value
 * instead of a single UTF-16 code element
 */
public final class CharacterProperty extends Property {

    /**
     * Inner class for creating instances of CharacterProperty
     */
    public static class Maker extends PropertyMaker {

        /**
         * @param propId the id of the property for which a Maker should be created
         */
        public Maker(int propId) {
            super(propId);
        }

        /** {@inheritDoc} */
        public Property make(PropertyList propertyList, String value,
                             FObj fo) {
            char c = value.charAt(0);
            return CharacterProperty.getInstance(c);
        }

    }

    /** cache containing all canonical CharacterProperty instances */
    private static final PropertyCache CACHE
        = new PropertyCache(CharacterProperty.class);

    private final char character;

    /**
     * @param character character value to be wrapped in this property
     */
    private CharacterProperty(char character) {
        this.character = character;
    }

    /**
     * Get character property instance for character.
     * @param character the character
     * @return the character property instance
     */
    public static CharacterProperty getInstance(char character) {
        return (CharacterProperty) CACHE.fetch(
                        new CharacterProperty(character));
    }

    /**
     * @return this.character cast as an Object
     */
    public Object getObject() {
        return new Character(character);
    }

    /**
     * @return this.character
     */
    public char getCharacter() {
        return this.character;
    }

    /**
     * @return this.character cast as a String
     */
    public String getString() {
        return new Character(character).toString();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (obj instanceof CharacterProperty) {
            return (((CharacterProperty)obj).character == this.character);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (int) character;
    }

}
