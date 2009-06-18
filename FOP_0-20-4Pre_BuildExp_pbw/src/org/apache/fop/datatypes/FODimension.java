/*
 * $Id$
 * Copyright (C) 2001 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */

package org.apache.fop.datatypes;


/**
 * This datatype hold a pair of resolved lengths,
 * specifiying the dimensions in
 * both inline and block-progression-directions.
 */
public class FODimension {

    public int ipd;
    public int bpd;


    public FODimension(int ipd, int bpd) {
	this.ipd = ipd;
	this.bpd = bpd;
    }
}
