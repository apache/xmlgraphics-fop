/*
   Copyright 2002-2004 The Apache Software Foundation.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 * $Id$
 * Created: Thu Aug  2 20:29:57 2001
 */
package org.apache.fop.fo;

import java.util.logging.Level;

import org.apache.fop.apps.Fop;
import org.apache.fop.datastructs.Tree;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.expr.PropertyParser;
import org.apache.fop.xml.XmlEvent;
import org.apache.fop.xml.XmlEventReader;

/**
 * <tt>FOTree</tt> is the class that generates and maintains the FO Tree.
 * It runs as a thread, so it implements the <tt>run()</tt> method.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Id$
 */

public class FOTree extends Tree implements Runnable {

    private static final String tag = "$Name$";
    private static final String revision = "$Revision$";

    /**
     * The buffer from which the <tt>XmlEvent</tt>s from the parser will
     * be read.  <tt>protected</tt> so that FONode can access it.
     */
    protected XmlEventReader xmlevents;
    private Thread parserThread;
    private boolean errorDump;

    /**
     * The <tt>PropertyParser</tt> which will be used by the FO tree
     * builder.
     */
    protected PropertyParser exprParser;

    /**
     * @param xmlevents the buffer from which <tt>XmlEvent</tt>s from the
     * parser are read.
     */
    public FOTree(XmlEventReader xmlevents)
        throws PropertyException
    {
        super();
        Level level = Fop.logger.getLevel();
        if (level.intValue() <= Level.FINE.intValue()) {
            errorDump = true;
        }
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
    public XmlEventReader getXmlEventReader() {
        return xmlevents;
    }

    /**
     * The <tt>run</tt> method() invoked by the call of <tt>start</tt>
     * on the thread in which runs off FOTree.
     */
    public void run() {
        FoRoot foRoot;
        XmlEvent event;
        try {
            // Let the parser look after STARTDOCUMENT and the correct
            // positioning of the root element
            event = xmlevents.getStartElement(FObjectNames.ROOT);
            foRoot = new FoRoot(this, event);
            foRoot.buildFoTree();
            System.out.println("Back from buildFoTree");
            // Clean up the fo:root event
            event = xmlevents.getEndElement(XmlEventReader.DISCARD_EV, event);
            // Get the end of document
            xmlevents.getEndDocument();
        } catch (Exception e) {
            if (errorDump) {
                e.printStackTrace();
            }
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

        FONode.PreOrder preorder = foRoot.new PreOrder();
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
