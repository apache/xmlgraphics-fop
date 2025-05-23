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

package org.apache.fop.render.mif;

// Java
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * The MIF File.
 * This organises the MIF File and the corresponding elements.
 * The catalog elements are used to setup the resources that
 * are referenced.
 */
public class MIFFile extends MIFElement {

    /** colorCatalog */
    protected MIFElement colorCatalog;
    /** pgfCatalog */
    protected PGFElement pgfCatalog;
    /** fontCatalog */
    // protected MIFElement fontCatalog;
    /** rulingCatalog */
    protected RulingElement rulingCatalog;
    /** tblCatalog */
    // protected MIFElement tblCatalog;
    /** views */
    // protected MIFElement views;
    /** variableFormats */
    // protected MIFElement variableFormats;
    /** xRefFormats */
    // protected MIFElement xRefFormats;
    /** document */
    // protected MIFElement document;
    /** bookComponent */
    // protected MIFElement bookComponent;
    /** initialAutoNums */
    // protected MIFElement initialAutoNums;
    /** aFrames */
    // protected MIFElement aFrames;
    /** tbls */
    // protected MIFElement tbls;
    /** pages */
    protected List pages = new java.util.ArrayList();
    /** textFlows */
    // protected List textFlows;


    /** default constructor */
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

    /**
     * @param os output stream
     * @throws IOException if not caught
     */
    public void output(OutputStream os) throws IOException {
        if (finished) {
            return;
        }

        if (!started) {
            os.write(("<MIFFile  5.00> # Generated by FOP\n"/* + getVersion()*/).getBytes(StandardCharsets.UTF_8));
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
            os.write(("# end of MIFFile").getBytes(StandardCharsets.UTF_8));
        }
    }

    /** @param p a page element to add */
    public void addPage(MIFElement p) {
        pages.add(p);
        addElement(p);
    }
}

