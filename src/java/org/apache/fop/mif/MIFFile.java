/*
 * $Id: MIFFile.java,v 1.2 2003/03/07 08:09:26 jeremias Exp $
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
package org.apache.fop.mif;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

/**
 * The MIF File.
 * This organises the MIF File and the corresponding elements.
 * The catalog elements are used to setup the resources that
 * are referenced.
 */
public class MIFFile extends MIFElement {

    protected MIFElement colorCatalog = null;
    protected PGFElement pgfCatalog = null;
    protected MIFElement fontCatalog = null;
    protected RulingElement rulingCatalog = null;
    protected MIFElement tblCatalog = null;
    protected MIFElement views = null;
    protected MIFElement variableFormats = null;
    protected MIFElement xRefFormats = null;
    protected MIFElement document = null;
    protected MIFElement bookComponent = null;
    protected MIFElement initialAutoNums = null;
    protected MIFElement aFrames = null;
    protected MIFElement tbls = null;
    protected List pages = new java.util.ArrayList();
    protected List textFlows = null;


    public MIFFile() {
        super("");
        valueElements = new java.util.ArrayList();
        setup();
    }

    /**
     * Do some setup.
     * Currently adds some dummy values to the resources.
     */
    protected void setup() {
        MIFElement unit = new MIFElement("Units");
        unit.setValue("Ucm");
        addElement(unit);

        colorCatalog = new MIFElement("ColorCatalog");
        MIFElement color = new MIFElement("Color");
        MIFElement prop = new MIFElement("ColorTag");
        prop.setValue("`Black'");
        color.addElement(prop);
        prop = new MIFElement("ColorCyan");
        prop.setValue("0.000000");
        color.addElement(prop);

        prop = new MIFElement("ColorMagenta");
        prop.setValue("0.000000");
        color.addElement(prop);
        prop = new MIFElement("ColorYellow");
        prop.setValue("0.000000");
        color.addElement(prop);
        prop = new MIFElement("ColorBlack");
        prop.setValue("100.000000");
        color.addElement(prop);
        prop = new MIFElement("ColorAttribute");
        prop.setValue("ColorIsBlack");
        color.addElement(prop);
        prop = new MIFElement("ColorAttribute");
        prop.setValue("ColorIsReserved");
        color.addElement(prop);
        color.finish(true);

        colorCatalog.addElement(color);
        addElement(colorCatalog);

        pgfCatalog = new PGFElement();
        pgfCatalog.lookupElement(null);
        addElement(pgfCatalog);

        rulingCatalog = new RulingElement();
        rulingCatalog.lookupElement(null);
        addElement(rulingCatalog);

    }

    public void output(OutputStream os) throws IOException {
        if (finished) {
            return;
        }

        if (!started) {
            os.write(("<MIFFile  5.00> # Generated by FOP\n"/* + getVersion()*/).getBytes());
            started = true;
        }
        boolean done = true;

        for (Iterator iter = valueElements.iterator(); iter.hasNext();) {
            MIFElement el = (MIFElement)iter.next();
            boolean d = el.output(os, 0);
            if (d) {
                iter.remove();
            } else {
                done = false;
                break;
            }
        }
        if (done && finish) {
            os.write(("# end of MIFFile").getBytes());
        }
    }

    public void addPage(MIFElement p) {
        pages.add(p);
        addElement(p);
    }
}

