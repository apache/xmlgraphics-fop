/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

public class CharacterProperty extends Property {

    public static class Maker extends Property.Maker {

        public Maker(String propName) {
            super(propName);
        }

        public Property make(PropertyList propertyList, String value,
                             FObj fo) {
            char c = value.charAt(0);
            return new CharacterProperty(c);
        }

    }    // end Charakter.Maker

    private char character;

    public CharacterProperty(char character) {
        this.character = character;
    }

    public Object getObject() {
        return new Character(character);
    }

    public char getCharacter() {
        return this.character;
    }

    public String getString() {
        return new Character(character).toString();
    }

}
