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

package org.apache.fop.render.ps;

import java.io.IOException;

import org.apache.xmlgraphics.ps.DSCConstants;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSProcSet;

/**
 * Proc Set with FOP-specific procs.
 */
public final class FOPProcSet extends PSProcSet {

    /** Singleton instance of the FOP procset */
    public static final FOPProcSet INSTANCE = new FOPProcSet();

    private FOPProcSet() {
        super("Apache FOP Std ProcSet", 1.0f, 0);
    }

    /**
     * Writes the procset to the PostScript file.
     * @param gen the PS generator
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(PSGenerator gen) throws IOException {
        gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE,
                new Object[] {TYPE_PROCSET, getName(),
                    Float.toString(getVersion()), Integer.toString(getRevision())});
        gen.writeDSCComment(DSCConstants.VERSION,
                new Object[] {Float.toString(getVersion()), Integer.toString(getRevision())});
        gen.writeDSCComment(DSCConstants.COPYRIGHT, "Copyright 2009 "
                    + "The Apache Software Foundation. "
                    + "License terms: http://www.apache.org/licenses/LICENSE-2.0");
        gen.writeDSCComment(DSCConstants.TITLE,
                "Basic set of procedures used by Apache FOP");


        gen.writeln("/TJ { % Similar but not equal to PDF's TJ operator");
        gen.writeln("  {");
        gen.writeln("    dup type /stringtype eq");
        gen.writeln("    { show }"); //normal text show
        gen.writeln("    { neg 1000 div 0 rmoveto }"); //negative X movement
        gen.writeln("    ifelse");
        gen.writeln("  } forall");
        gen.writeln("} bd");

        gen.writeln("/ATJ { % As TJ but adds letter-spacing");
        gen.writeln("  /ATJls exch def");
        gen.writeln("  {");
        gen.writeln("    dup type /stringtype eq");
        gen.writeln("    { ATJls 0 3 2 roll ashow }"); //normal text show
        gen.writeln("    { neg 1000 div 0 rmoveto }"); //negative X movement
        gen.writeln("    ifelse");
        gen.writeln("  } forall");
        gen.writeln("} bd");

        gen.writeDSCComment(DSCConstants.END_RESOURCE);
        gen.getResourceTracker().registerSuppliedResource(this);
    }

}
