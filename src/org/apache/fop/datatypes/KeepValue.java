/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;

/**
 * Keep Value
 * Stores the different types of keeps in a single convenient format.
 */
public class KeepValue {
    public static final String KEEP_WITH_ALWAYS = "KEEP_WITH_ALWAYS";
    public static final String KEEP_WITH_AUTO = "KEEP_WITH_AUTO";
    public static final String KEEP_WITH_VALUE = "KEEP_WITH_VALUE";
    private String type = KEEP_WITH_AUTO;
    private int value = 0;

    public KeepValue(String type, int val) {
        this.type = type;
        this.value = val;
    }

    public int getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }

}
