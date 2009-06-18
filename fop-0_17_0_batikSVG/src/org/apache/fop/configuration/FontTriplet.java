/*
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources."
 */


package org.apache.fop.configuration;

/**
 * FontTriplet contains information on name, weight, style of one font
 */


public class FontTriplet {
    private String name, weight, style;
    public FontTriplet(String name, String weight, String style) {
        this.name = name;
        this.weight = weight;
        this.style = style;
    }

    public String getName() {
        return name;
    }

    public String getWeight() {
        return weight;
    }

    public String getStyle() {
        return style;
    }

}




