/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.fo;

/**
 * Interface for adding supported element and property mappings to
 * the given builder.
 */
public interface ElementMapping {
    public static final String DEFAULT = "<default>";

    public void addToBuilder(FOTreeBuilder builder);

    public static class Maker {
        public FONode make(FONode parent) {
            return null;
        }
    }
}
