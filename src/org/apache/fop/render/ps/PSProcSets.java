/*
 * $Id$
 * ============================================================================
 *                    The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment: "This product includes software
 *    developed by the Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and "Apache Software Foundation" must not be used to
 *    endorse or promote products derived from this software without prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache", nor may
 *    "Apache" appear in their name, without prior written permission of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * APACHE SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ============================================================================
 * 
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * James Tauber <jtauber@jtauber.com>. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */ 
package org.apache.fop.render.ps;

import java.io.IOException;

/**
 * This class defines the basic resources (procsets) used by FOP's PostScript
 * renderer and SVG transcoder.
 * 
 * @author <a href="mailto:fop-dev@xml.apache.org">Apache XML FOP Development Team</a>
 * @author <a href="mailto:jeremias@apache.org">Jeremias Maerki</a>
 * @version $Id$
 */
public final class PSProcSets {

    /**
     * Generates a resource defining standard procset for FOP.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static final void writeFOPStdProcSet(PSGenerator gen) throws IOException {
        gen.writeln("%%BeginResource: procset (Apache FOP Std ProcSet) 1.0 0");
        gen.writeln("%%Version: 1.0 0");
        gen.writeln("%%Copyright: Copyright (C) 2001-2003 "
                    + "The Apache Software Foundation. All rights reserved.");
        gen.writeln("%%Title: Basic set of procedures used by FOP");

        gen.writeln("/bd{bind def}bind def");
        gen.writeln("/ld{load def}bd");
        gen.writeln("/M/moveto ld");
        gen.writeln("/RM/rmoveto ld");
        gen.writeln("/t/show ld");

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

        gen.writeln("%%EndResource");
    }


    /**
     * Generates a resource defining a procset for including EPS graphics.
     * @param gen PSGenerator to use for output
     * @throws IOException In case of an I/O problem
     */
    public static final void writeFOPEPSProcSet(PSGenerator gen) throws IOException {
        gen.writeln("%%BeginResource: procset (Apache FOP EPS ProcSet) 1.0 0");
        gen.writeln("%%Version: 1.0 0");
        gen.writeln("%%Copyright: Copyright (C) 2002-2003 "
                    + "The Apache Software Foundation. All rights reserved.");
        gen.writeln("%%Title: EPS procedures used by FOP");

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
        
        gen.writeln("%%EndResource");
    }

}
