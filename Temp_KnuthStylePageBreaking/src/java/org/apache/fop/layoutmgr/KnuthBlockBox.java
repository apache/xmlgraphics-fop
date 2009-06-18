/*
 * Copyright 2004-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.fop.layoutmgr;

import org.apache.fop.traits.MinOptMax;

public class KnuthBlockBox extends KnuthBox {
    
    private MinOptMax ipdRange;
    private int bpd;

    public KnuthBlockBox(int w, MinOptMax range, int bpdim, Position pos, boolean bAux) {
        super(w, pos, bAux);
        ipdRange = (MinOptMax) range.clone();
        bpd = bpdim;
    }

    public MinOptMax getIPDRange() {
        return (MinOptMax) ipdRange.clone();
    }

    public int getBPD() {
        return bpd;
    }
}