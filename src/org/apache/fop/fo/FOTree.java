/*
 * $Id$
 * 
 * ============================================================================
 *                   The Apache Software License, Version 1.1
 * ============================================================================
 * 
 * Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modifica-
 * tion, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of  source code must  retain the above copyright  notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include  the following  acknowledgment:  "This product includes  software
 *    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
 *    Alternately, this  acknowledgment may  appear in the software itself,  if
 *    and wherever such third-party acknowledgments normally appear.
 * 
 * 4. The names "FOP" and  "Apache Software Foundation"  must not be used to
 *    endorse  or promote  products derived  from this  software without  prior
 *    written permission. For written permission, please contact
 *    apache@apache.org.
 * 
 * 5. Products  derived from this software may not  be called "Apache", nor may
 *    "Apache" appear  in their name,  without prior written permission  of the
 *    Apache Software Foundation.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 * APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 * DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 * ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 * (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * This software  consists of voluntary contributions made  by many individuals
 * on  behalf of the Apache Software  Foundation and was  originally created by
 * James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 * Software Foundation, please see <http://www.apache.org/>.
 *  
 *
 * Created: Thu Aug  2 20:29:57 2001
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */
package org.apache.fop.fo;

import org.apache.fop.apps.Driver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.xml.FoXMLEvent;
import org.apache.fop.xml.SyncedFoXmlEventsBuffer;

/**
 * <tt>FOTree</tt> is the class that generates and maintains the FO Tree.
 * It runs as a thread, so it implements the <tt>run()</tt> method.
 */

public class FOTree extends Tree implements Runnable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The buffer from which the <tt>XMLEvent</tt>s from the parser will
     * be read.  <tt>protected</tt> so that FONode can access it.
     */
    protected SyncedFoXmlEventsBuffer xmlevents;
    private Thread parserThread;
    private boolean errorDump;

    /**
     * The <tt>PropertyParser</tt> which will be used by the FO tree
     * builder.
     */
    protected PropertyParser exprParser;

    /**
     * @param xmlevents the buffer from which <tt>XMLEvent</tt>s from the
     * parser are read.
     */
    public FOTree(SyncedFoXmlEventsBuffer xmlevents)
        throws PropertyException
    {
        super();
        errorDump = Configuration.getBooleanValue("debugMode").booleanValue();
        this.xmlevents = xmlevents;
        exprParser = new PropertyParser(this);

        // Initialize the FontSize first.  Any lengths defined in ems must
        // be resolved relative to the current font size.  This may happen
        // during setup of initial values.
        // Set the initial value
        PropertyValue prop =
                PropertyConsts.pconsts.getInitialValue(PropNames.FONT_SIZE);
        if ( ! (prop instanceof Numeric) || ! ((Numeric)prop).isLength())
            throw new PropertyException("Initial font-size is not a Length");


        /*
        for (int i = 1; i <= PropNames.LAST_PROPERTY_INDEX; i++) {
            if (i == PropNames.FONT_SIZE) continue;
            // Set up the initial values for each property
            PropertyConsts.pconsts.getInitialValue(i);
            //System.out.println("....Setting initial value for " + i);
        }
        */

    }

    /**
     * Parser thread notifies itself to FO tree builder by this call.  The
     * purpose of this notification is to allow the FO tree builder thread
     * to attempt to interrupt the parser thread when the builder
     * terminates.
     * @param parserThread - the <tt>Thread</tt> object of the parser thread.
     */
    public void setParserThread(Thread parserThread) {
        this.parserThread = parserThread;
    }

    /**
     * Get the <i>xmlevents</i> buffer through which descendents can access
     * parser events.
     * @return <i>xmlevents</i>.
     */
    public SyncedFoXmlEventsBuffer getXmlevents() {
        return xmlevents;
    }

    /**
     * The <tt>run</tt> method() invoked by the call of <tt>start</tt>
     * on the thread in which runs off FOTree.
     */
    public void run() {
        FoRoot foRoot;
        FoXMLEvent event;
        try {
            // Let the parser look after STARTDOCUMENT and the correct
            // positioning of the root element
            event = xmlevents.getStartElement(FObjectNames.ROOT);
            foRoot = new FoRoot(this, event);
            foRoot.buildFoTree();
            System.out.println("Back from buildFoTree");
            // Clean up the fo:root event
            event = xmlevents.getEndElement(SyncedFoXmlEventsBuffer.DISCARD_EV, event);
            // Get the end of document
            xmlevents.getEndDocument();
        } catch (Exception e) {
            if (errorDump) Driver.dumpError(e);
            if (parserThread != null) {
                try {
                    parserThread.interrupt();
                } catch (Exception ex) {} // Ignore
            }
            // Now propagate a Runtime exception
            throw new RuntimeException(e);
        }

        // DEBUG
        System.out.println("Elapsed time: " +
                    (System.currentTimeMillis() - 
                            org.apache.fop.apps.Fop.startTime)); // DEBUG

        FONode.PreOrder preorder = foRoot.new PreOrder(getModCount());
        int nodecount = 0;
        while (preorder.hasNext()) {
            nodecount++;
            FONode next = (FONode) preorder.next();
            /*
            PropertyValue[] pvset = next.getSparsePropsSet();
            System.out.println("......Number of properties: " + pvset.length);
            for (int i = 0; i < pvset.length; i++ ) {
                PropertyValue pv = pvset[i];
                System.out.println(pv);
            }
            */
        }
        System.out.println("# of FONodes: " + nodecount);
    }

}// FOTree
