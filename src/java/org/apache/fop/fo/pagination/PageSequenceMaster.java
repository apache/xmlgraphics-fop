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

 * $Id: PageSequenceMaster.java,v 1.4.2.8 2003/06/12 18:19:33 pbwest Exp $
 */

package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fop.apps.FOPException;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.datatypes.IntegerType;
import org.apache.fop.datatypes.NCName;
import org.apache.fop.datatypes.Numeric;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObjectNames;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.fo.pagination.FoPageSequenceMaster.FoRepeatablePageMasterAlternatives.FoConditionalPageMasterReference;
import org.apache.fop.fo.pagination.PageSequenceMaster.PageMasterAlternatives.PageCondition;
import org.apache.fop.fo.properties.BlankOrNotBlank;
import org.apache.fop.fo.properties.MaximumRepeats;
import org.apache.fop.fo.properties.OddOrEven;
import org.apache.fop.fo.properties.PagePosition;

/**
 * Encodes an <b>fo:page-sequence-master</b> and associated
 * conditions.  All <i>page-sequence-master</i> specifications are converted
 * into <i>repeatable-page-master-alternative</i>s.
 * For a <i>simple-page-master</i>, a <i>PageSequenceMaster</i> object is
 * created which shares a name with its target <i>simple-page-master</i>.
 * This procedure results in a single common structure and manner of access
 * for all page masters.
 *
 * @author <a href="mailto:pbwest@powerup.com.au">Peter B. West</a>
 * @version $Revision$ $Name$
 */
public class PageSequenceMaster {
    
    private static final String tag = "$Name:  $";
    private static final String revision = "$Revision: 1.4.2.8 $";

    /** Constant representing the <i>no-limit</i> value of the
            maximum-repeats property. */
    public static final int NO_LIMIT = -1;

    private String masterName;

    /**
     * Comment for <code>masters</code>
     */
    protected ArrayList masters = new ArrayList(1);

    /**
     * Creates a <i>PageSequenceMaster</i> from an
     * <tt>FoSimplePageMaster</tt>.  The <code>PageMasterAlternatives</code>
     * is created with default minimum and maximum repeats of 1 and
     * <code>NO_LIMIT</code> respectively.  It is assigned a
     * <code>PageCondition</code> with deafault values of <code>ANY</code> for
     * <code>BlankOrNotBlank, OddOrEven</code> and <code>PagePosition</code>.
     * 
     * @param simplePM - the <tt>FoSimplePageMaster</tt> from which this
     * <i>PageSequenceMaster</i> is derived.
     */
    public PageSequenceMaster(FoSimplePageMaster simplePM)
        throws PropertyException, FOPException
    {
        PageMasterAlternatives masterAlt;
        masterName = simplePM.getMasterName();
        masterAlt = new PageMasterAlternatives(1, NO_LIMIT);
        // Create and add a single default PageCondition
        masterAlt.addCondition
                    (masterAlt.new PageCondition(simplePM,
                                   BlankOrNotBlank.ANY,
                                   OddOrEven.ANY,
                                   PagePosition.ANY));
        masters.add(masterAlt);
    }

    /**
     * Create a <i>PageSequenceMaster</i> from an
     * <tt>FoPageSequenceMaster</tt>.
     * @param pageSeq - the <tt>FoPageSequenceMaster</tt> from which this
     * <i>PageSequenceMaster</i> is derived.
     * @param simplePageMasters - a <tt>HashMap</tt> of
     * <tt>FoSimplePageMaster</tt>s indexed by master-name.
     */
    public PageSequenceMaster
                (FoPageSequenceMaster pageSeq, HashMap simplePageMasters)
        throws PropertyException, FOPException
    {
        String masterRef;
        FoSimplePageMaster simplePM;
        PageMasterAlternatives masterAlt;
        int maxRepeats = 1;
        int enumValue;
        PropertyValue pv;
        Numeric npv;
        masterName = pageSeq.getMasterName();
        // Process the sequence of masters.
        int numChildren = pageSeq.numChildren();
        for (int child = 0; child < numChildren; child++) {
            FONode masterReference = (FONode)(pageSeq.getChild(child));
            switch (masterReference.type) {
            case FObjectNames.SINGLE_PAGE_MASTER_REFERENCE:
                // Get the master-reference property value
                simplePM = getMasterReference
                                        (masterReference, simplePageMasters);
                // Create and add a PageMasterAlternatives
                masterAlt = new PageMasterAlternatives(1, 1);
                // Create and add a single default PageCondition
                masterAlt.addCondition
                            (masterAlt.new PageCondition(simplePM,
                                           BlankOrNotBlank.ANY,
                                           OddOrEven.ANY,
                                           PagePosition.ANY));
                masters.add(masterAlt);
                break;
            case FObjectNames.REPEATABLE_PAGE_MASTER_REFERENCE:
                // Get the master-reference property value
                simplePM = getMasterReference
                                        (masterReference, simplePageMasters);
                // Get the maximum-repeats
                pv = masterReference.getPropertyValue
                                                (PropNames.MAXIMUM_REPEATS);
                switch (pv.getType()) {
                case PropertyValue.ENUM:
                    enumValue = ((EnumType)pv).getEnumValue();
                    if (enumValue == MaximumRepeats.NO_LIMIT) {
                        maxRepeats = NO_LIMIT;
                        break;
                    }
                    throw new FOPException
                            ("Unrecognized maximum-repeats enum: "
                             + enumValue);
                case PropertyValue.INTEGER:
                    maxRepeats = ((IntegerType)pv).getInt();
                    break;
                case PropertyValue.NUMERIC:
                    npv = (Numeric)pv;
                    if (npv.isInteger()) maxRepeats = npv.asInt();
                    break;
                    // else fall through to exception
                default:
                    throw new FOPException
                                        ("MAXIMUM_REPEATS not an integer.");
                }
                // Create and add a PageMasterAlternatives
                masterAlt = new PageMasterAlternatives(0, maxRepeats);
                // Create and add a single default PageCondition
                masterAlt.addCondition
                            (masterAlt.new PageCondition(simplePM,
                                           BlankOrNotBlank.ANY,
                                           OddOrEven.ANY,
                                           PagePosition.ANY));
                masters.add(masterAlt);
                break;
            case FObjectNames.REPEATABLE_PAGE_MASTER_ALTERNATIVES:
                // Get the maximum-repeats
                pv = masterReference.getPropertyValue
                                                (PropNames.MAXIMUM_REPEATS);
                switch (pv.getType()) {
                case PropertyValue.ENUM:
                    enumValue = ((EnumType)pv).getEnumValue();
                    if (enumValue == MaximumRepeats.NO_LIMIT) {
                        maxRepeats = NO_LIMIT;
                        break;
                    }
                    throw new FOPException
                            ("Unrecognized maximum-repeats enum: "
                             + enumValue);
                case PropertyValue.INTEGER:
                    maxRepeats = ((IntegerType)pv).getInt();
                    break;
                case PropertyValue.NUMERIC:
                    npv = (Numeric)pv;
                    if (npv.isInteger()) maxRepeats = npv.asInt();
                    break;
                    // else fall through to exception
                default:
                    throw new FOPException
                                        ("MAXIMUM_REPEATS not an integer.");
                }
                // Create and add a PageMasterAlternatives
                masterAlt = new PageMasterAlternatives(0, maxRepeats);
                // Process the conditional-page-master-alternatives children
                int numConds = masterReference.numChildren();
                for (int cond = 0; cond < numConds; cond++) {
                    FoConditionalPageMasterReference foCond;
                    int blankOrNot, oddOrEven, pagePosition;
                    EnumType enumval;

                    foCond = (FoConditionalPageMasterReference)
                                            (masterReference.getChild(cond));
                    // Get the master-reference property value
                    simplePM = getMasterReference(foCond, simplePageMasters);
                    enumval = (EnumType)
                                (foCond.getPropertyValue
                                             (PropNames.BLANK_OR_NOT_BLANK));
                    blankOrNot = enumval.getEnumValue();
                    enumval = (EnumType)
                                (foCond.getPropertyValue
                                             (PropNames.ODD_OR_EVEN));
                    oddOrEven = enumval.getEnumValue();
                    enumval = (EnumType)
                                (foCond.getPropertyValue
                                             (PropNames.PAGE_POSITION));
                    pagePosition = enumval.getEnumValue();
                    // Create and add a single default PageCondition
                    masterAlt.addCondition
                            (masterAlt.new PageCondition
                                 (simplePM,
                                      blankOrNot, oddOrEven, pagePosition));
                }
                masters.add(masterAlt);
                break;
            default:
                throw new FOPException
                        ("Unknown master reference type: "
                         + masterReference.type);
            }
        }
    }

    /**
     * Get the master-name of this <i>PageSequenceMaster</i>.  This is the
     * name by which <i>fo:page-sequence</i>s will reference the master
     * through their <i>master-reference</i> property.
     * @return the name.
     */
    public String getMasterName() {
        return masterName;
    }

    /**
     * Get the <tt>FoSimplePageMaster</tt> referred to by the
     * <i>master-reference</i> property in the argument <tt>FONode</tt>.
     * @param node.
     * @return the <tt>FoSimplePageMaster</tt>.
     */
    private FoSimplePageMaster getMasterReference
                                    (FONode node, HashMap simplePageMasters)
        throws FOPException, PropertyException
    {
        FoSimplePageMaster simplePM;
        // Get the master-reference property value
        String masterRef =
            ((NCName)
             (node.getPropertyValue(PropNames.MASTER_REFERENCE))).getNCName();
        // Valid reference?
        if ((simplePM = (FoSimplePageMaster)(simplePageMasters.get(masterRef)))
                    != null)
            return simplePM;
        throw new FOPException
                ("No simple-page-master referent for "
                     + FObjectNames.getFOName(node.type) + ": " + masterRef);
    }

    /**
     * Add a new alternatives master to the sequence.
     * @param master - a <tt>PageMasterAlternatives</tt>.
     */
    public void addMaster(PageMasterAlternatives master) {
        masters.add(master);
    }

    /**
     * Get the length of the <i>masters</i> <tt>ArrayList</tt>.
     * @return - the length.
     */
    public int getMastersLength() {
        return masters.size();
    }

    /**
     * Get the indexed <tt>PageMasterAlternatives</tt> master.
     * @param i - the index of the master to retrieve.
     * @return - the indexed <tt>PageMasterAlternatives</tt> master.
     */
    public PageMasterAlternatives getMaster(int i) {
        return (PageMasterAlternatives)(masters.get(i));
    }

    /**
     * Encodes an fo:repeatable-page-master-alternatives and associated
     * conditions.
     */
    public class PageMasterAlternatives {

        private static final String tag = "$Name:  $";
        private static final String revision = "$Revision: 1.4.2.8 $";

        /** The minumum number of repeats for this set of alternatives. */
        public final int minRepeats;
        /** The maximum-repeats value for this set of alternatives. */
        public final int maxRepeats;

        /**
         * List of alternative condition sets/simple page masters
         */
        private ArrayList alternatives = new ArrayList(1);

        /**
         * @param minRepeats - the <tt>int</tt> minimum number of repeats for
         * this alternative.
         * @param maxRepeats - the <tt>int</tt> maximum number of repeats for
         * this alternative.  Set from the <i>maximum-repeats</i> property.
         */
        public PageMasterAlternatives(int minRepeats, int maxRepeats) {
            this.minRepeats = minRepeats;
            this.maxRepeats = maxRepeats;
            //System.out.println("New PMA: " + minRepeats + " " + maxRepeats);
        }

        /**
         * Add a new alternative condition set.
         * @param condition - a <tt>PageCondition</tt>.
         */
        public void addCondition(PageCondition condition) {
            alternatives.add(condition);
        }

        /**
         * Get the length of the <i>alternatives</i> <tt>ArrayList</tt>.
         * @return - the length.
         */
        public int getAlternativesLength() {
            return alternatives.size();
        }

        /**
         * Get the indexed <tt>PageMasterAlternatives</tt> master.
         * @param i - the index of the master to retrieve.
         * @return - the indexed <tt>PageMasterAlternatives</tt> master.
         */
        public PageCondition getAlternative(int i) {
            return (PageCondition)(alternatives.get(i));
        }
        
        /**
         * Gets the <code>PageCondition</code> object matching the
         * arguments
         * @param blankOrNot blank or not blank page test condition
         * @param oddOrEven odd or even page numbered page test condition
         * @param pagePosition position on sequence test condition
         * @return the matching <code>PageCondition</code> or null if
         * conditions match no object
         */
        public PageCondition conditionMatch(
                int blankOrNot, int oddOrEven, int pagePosition) {
            for (int i = 0; i < alternatives.size(); i++) {
                PageCondition pageCond = (PageCondition)(alternatives.get(i));
                if (pageCond.isMatch(blankOrNot, oddOrEven, pagePosition)) {
                    return pageCond;
                }
            }
            return null;
        }
        
        /**
         * Encodes a condition set from an FoConditionalPageReferenceMaster.
         */
        public class PageCondition {

            private static final String tag = "$Name:  $";
            private static final String revision = "$Revision: 1.4.2.8 $";

            /** The simple page master used when these conditions are true. */
            public final FoSimplePageMaster master;
            /** The blank-or-not-blank condition.
                Encoded from BlankOrNotBlank. */
            public final int blankOrNot;
            /** The odd-or-even condition.
                                        Encoded from OddOrEven. */
            public final int oddOrEven;
            /** The page-position condition.
                                       Encoded from pagePosition. */
            public final int pagePosition;

            /**
             * @param master - a reference to an <i>fo:simple-page-master</i>.
             * @param blankOrNot - an <tt>int</tt> encoding a
             * <i>blank-or-not-blank</i> condition.
             * @param oddOrEven - an <tt>int</tt> encoding an
             * <i>odd-or-even</i> condition.
             * @param pagePosition - an <tt>int</tt> encoding a
             * <i>page-position</i>
             * condition.
             */
            public PageCondition(FoSimplePageMaster master,
                            int blankOrNot, int oddOrEven, int pagePosition)
            {
                this.master       = master;
                this.blankOrNot   = blankOrNot;
                this.oddOrEven    = oddOrEven;
                this.pagePosition = pagePosition;
            }
            
            /**
             * Does this <code>PageCondition</code> match the arguments?
             * @param blankOrNot blank or not blank page test condition
             * @param oddOrEven odd or even page numbered page test condition
             * @param pagePosition position on sequence test condition
             * @return true if all tests match
             */
            public boolean isMatch(
                    int blankOrNot, int oddOrEven, int pagePosition) {
                return (this.blankOrNot == blankOrNot &&
                         this.oddOrEven == oddOrEven &&
                         this.pagePosition == pagePosition);
            }
            
            /**
             * Gets the simple page master associated with this set of
             * alternative conditions
             * @return the simple page master
             */
            public FoSimplePageMaster getSimplePM() {
                return master;
            }
            
            /**
             * Gets the simple page master associated with this set of page
             * test condition if the conditions match the arguments
             * @param blankOrNot blank or not blank page test condition
             * @param oddOrEven odd or even page numbered page test condition
             * @param pagePosition position on sequence test condition
             * @return the simple page master or null if the conditions do
             * not match
             */
            public FoSimplePageMaster getSimplePM(
                    int blankOrNot, int oddOrEven, int pagePosition) {
                if (isMatch(blankOrNot, oddOrEven, pagePosition)) {
                    return master;
                }
                return null;
            }

        } // End of PageCondition

    } // End of PageMasterAlternatives

    /**
     * Provides an iterator across the sequence of page masters in the
     * containing <code>PageSequenceMaster</code>.
     * 
     * @author pbw
     * @version $Revision$ $Name$
     */
    public class PageMasterIterator implements Cloneable {

        /**
         * Effectively, the iterator across <code>masters</code>
         */
        private int currentMasterIndex = -1;
        
        /**
         * Number of times this set of alternatives has been used
         */
        private int usageCount = 0;
        
        private PageMasterAlternatives altMaster;
        
        /**
         * Returns a new iterator across <code>masters</code>
         */
        public PageMasterIterator() {}
        
        /**
         * Clone the iterator.  The purpose of this operation is to allow
         * interested methods to attempt layouts of the same flow data using
         * different masters, e.g., laying out a 'last' page and 'rest' page
         * from the same flow data.
         * 
         * @see java.lang.Object#clone()
         */
        protected Object clone() throws CloneNotSupportedException {
            synchronized (this) {
                return super.clone();
            }
        }

        /**
         * @return true if any repetitions on any masters remain in the
         * sequence
         */
        public boolean hasNext() {
            if (currentMasterIndex >= masters.size()) return false;
            if (currentMasterIndex == masters.size() - 1
                    && currentExhausted()) return false;
            return true;
        }
        
        /**
         * Use the simple page master from the first <code>PageCondition</code>
         * matching the argument.
         * The usage count for this <code>PageMasterAlternatives</code>
         * object is incremented.
         * @param blankOrNot blank or not blank page test condition
         * @param oddOrEven odd or even page numbered page test condition
         * @param pagePosition position on sequence test condition
         * @return the simple page master or null if the usage count has been
         * exceeded or there is no matching set of conditions. 
         */
        public FoSimplePageMaster useConditionalMaster(
                int blankOrNot, int oddOrEven, int pagePosition) {
            if (altMaster.maxRepeats == NO_LIMIT
                    || usageCount < altMaster.maxRepeats) {
                PageCondition pageCond = altMaster.conditionMatch(
                        blankOrNot, oddOrEven, pagePosition);
                if (pageCond != null) {
                    synchronized (this) {
                        usageCount++;
                    }
                    return pageCond.getSimplePM();
                }
            }
            return null;
        }
        
        public boolean currentExhausted() {
            return (currentMasterIndex < 0 || 
                    (altMaster.maxRepeats != NO_LIMIT
                            && usageCount >= altMaster.maxRepeats));
        }
        
        /**
         * Gets the next simple page master matching the given conditions.
         * @param blankOrNot blank or not blank page test condition
         * @param oddOrEven odd or even page numbered page test condition
         * @param pagePosition position on sequence test condition
         * @return the matching page master or null if none can be found
         */
        public FoSimplePageMaster next(
                int blankOrNot, int oddOrEven, int pagePosition) {
            PageMasterAlternatives masterAlt;
            FoSimplePageMaster simplePM;
            while (hasNext()) {
                if (currentExhausted()) {
                    synchronized (this) {
                    // Iterate to next altMaster
                        altMaster = (PageMasterAlternatives)(
                                masters.get(++currentMasterIndex));
                        usageCount = 0;
                    }
                }
                simplePM = useConditionalMaster(
                        blankOrNot, oddOrEven, pagePosition);
                if (simplePM != null) {
                    return simplePM;
                }
            }
            return null;
        }
        
        /**
         * Gets the next simple page master matching the default conditions
         * @return the matching page master or null if none can be found
         */
        public FoSimplePageMaster next() {
            return next(
                    BlankOrNotBlank.ANY, OddOrEven.ANY, PagePosition.ANY);
        }

    } // End of PageMasterIterator
    
}
