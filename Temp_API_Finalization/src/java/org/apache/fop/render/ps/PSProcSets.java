/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
 
package org.apache.fop.render.ps;

import java.io.IOException;

/**
 * This class defines the basic resources (procsets) used by FOP's PostScript
 * renderer and SVG transcoder.
 * 
 * @author <a href="mailto:fop-dev@xmlgraphics.apache.org">Apache FOP Development Team</a>
 * @version $Id: PSProcSets.java,v 1.3 2003/03/11 08:42:24 jeremias Exp $
 */
public final class PSProcSets {

    /** the standard FOP procset */
    public static final PSResource STD_PROCSET = new StdProcSet();
    /** the EPS FOP procset */
    public static final PSResource EPS_PROCSET = new EPSProcSet();
    
    private static class StdProcSet extends PSResource {
        
        public StdProcSet() {
            super("procset", "Apache FOP Std ProcSet");
        }
        
        public void writeTo(PSGenerator gen) throws IOException {
            gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, 
                    new Object[] {"procset", getName(), "1.0", "0"});
            gen.writeDSCComment(DSCConstants.VERSION, 
                    new Object[] {"1.0", "0"});
            gen.writeDSCComment(DSCConstants.COPYRIGHT, "Copyright 2001-2003 "
                        + "The Apache Software Foundation. All rights reserved.");
            gen.writeDSCComment(DSCConstants.TITLE, "Basic set of procedures used by FOP");

            gen.writeln("/bd{bind def}bind def");
            gen.writeln("/ld{load def}bd");
            gen.writeln("/M/moveto ld");
            gen.writeln("/RM/rmoveto ld");
            gen.writeln("/t/show ld");
            gen.writeln("/A/ashow ld");
            gen.writeln("/cp/closepath ld");

            gen.writeln("/re {4 2 roll M"); //define rectangle
            gen.writeln("1 index 0 rlineto");
            gen.writeln("0 exch rlineto");
            gen.writeln("neg 0 rlineto");
            gen.writeln("cp } bd");

            gen.writeln("/_ctm matrix def"); //Holds the current matrix
            gen.writeln("/_tm matrix def");
            //BT: save currentmatrix, set _tm to identitymatrix and move to 0/0
            gen.writeln("/BT { _ctm currentmatrix pop matrix _tm copy pop 0 0 moveto } bd"); 
            //ET: restore last currentmatrix
            gen.writeln("/ET { _ctm setmatrix } bd");
            gen.writeln("/iTm { _ctm setmatrix _tm concat } bd");
            gen.writeln("/Tm { _tm astore pop iTm 0 0 moveto } bd");
            
            gen.writeln("/ux 0.0 def");
            gen.writeln("/uy 0.0 def");

            // <font> <size> F
            gen.writeln("/F {");
            gen.writeln("  /Tp exch def");
            // gen.writeln("  currentdict exch get");
            gen.writeln("  /Tf exch def");
            gen.writeln("  Tf findfont Tp scalefont setfont");
            gen.writeln("  /cf Tf def  /cs Tp def  /cw ( ) stringwidth pop def");
            gen.writeln("} bd");

            gen.writeln("/ULS {currentpoint /uy exch def /ux exch def} bd");
            gen.writeln("/ULE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add moveto  Tcx uy To add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");

            gen.writeln("/OLE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add cs add moveto Tcx uy To add cs add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");

            gen.writeln("/SOE {");
            gen.writeln("  /Tcx currentpoint pop def");
            gen.writeln("  gsave");
            gen.writeln("  newpath");
            gen.writeln("  cf findfont cs scalefont dup");
            gen.writeln("  /FontMatrix get 0 get /Ts exch def /FontInfo get dup");
            gen.writeln("  /UnderlinePosition get Ts mul /To exch def");
            gen.writeln("  /UnderlineThickness get Ts mul /Tt exch def");
            gen.writeln("  ux uy To add cs 10 mul 26 idiv add moveto "
                        + "Tcx uy To add cs 10 mul 26 idiv add lineto");
            gen.writeln("  Tt setlinewidth stroke");
            gen.writeln("  grestore");
            gen.writeln("} bd");
            
            gen.writeln("/QUADTO {");
            gen.writeln("/Y22 exch store");
            gen.writeln("/X22 exch store");
            gen.writeln("/Y21 exch store");
            gen.writeln("/X21 exch store");
            gen.writeln("currentpoint");
            gen.writeln("/Y21 load 2 mul add 3 div exch");
            gen.writeln("/X21 load 2 mul add 3 div exch");
            gen.writeln("/X21 load 2 mul /X22 load add 3 div");
            gen.writeln("/Y21 load 2 mul /Y22 load add 3 div");
            gen.writeln("/X22 load /Y22 load curveto");
            gen.writeln("} bd");
            
            gen.writeDSCComment(DSCConstants.END_RESOURCE);
        }
        
    }

    private static class EPSProcSet extends PSResource {
        
        public EPSProcSet() {
            super("procset", "Apache FOP EPS ProcSet");
        }
        
        public void writeTo(PSGenerator gen) throws IOException {
            gen.writeDSCComment(DSCConstants.BEGIN_RESOURCE, 
                    new Object[] {"procset", getName(), "1.0", "0"});
            gen.writeDSCComment(DSCConstants.VERSION, 
                    new Object[] {"1.0", "0"});
            gen.writeDSCComment(DSCConstants.COPYRIGHT, "Copyright 2002-2003 "
                        + "The Apache Software Foundation. All rights reserved.");
            gen.writeDSCComment(DSCConstants.TITLE, "EPS procedures used by FOP");

            gen.writeln("/BeginEPSF { %def");
            gen.writeln("/b4_Inc_state save def         % Save state for cleanup");
            gen.writeln("/dict_count countdictstack def % Count objects on dict stack");
            gen.writeln("/op_count count 1 sub def      % Count objects on operand stack");
            gen.writeln("userdict begin                 % Push userdict on dict stack");
            gen.writeln("/showpage { } def              % Redefine showpage, { } = null proc");
            gen.writeln("0 setgray 0 setlinecap         % Prepare graphics state");
            gen.writeln("1 setlinewidth 0 setlinejoin");
            gen.writeln("10 setmiterlimit [ ] 0 setdash newpath");
            gen.writeln("/languagelevel where           % If level not equal to 1 then");
            gen.writeln("{pop languagelevel             % set strokeadjust and");
            gen.writeln("1 ne                           % overprint to their defaults.");
            gen.writeln("{false setstrokeadjust false setoverprint");
            gen.writeln("} if");
            gen.writeln("} if");
            gen.writeln("} bd");

            gen.writeln("/EndEPSF { %def");
            gen.writeln("count op_count sub {pop} repeat            % Clean up stacks");
            gen.writeln("countdictstack dict_count sub {end} repeat");
            gen.writeln("b4_Inc_state restore");
            gen.writeln("} bd");
            
            gen.writeDSCComment(DSCConstants.END_RESOURCE);
        }
        
    }
    
    /**
     * Generates a resource defining standard procset for FOP.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static void writeFOPStdProcSet(PSGenerator gen) throws IOException {
        ((StdProcSet)STD_PROCSET).writeTo(gen);
    }


    /**
     * Generates a resource defining a procset for including EPS graphics.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static void writeFOPEPSProcSet(PSGenerator gen) throws IOException {
        ((EPSProcSet)EPS_PROCSET).writeTo(gen);
    }

}
