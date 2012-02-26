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

package org.apache.fop.complexscripts.bidi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.fop.area.Area;
import org.apache.fop.area.LinkResolver;
import org.apache.fop.area.inline.BasicLinkArea;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;

// CSOFF: EmptyForIteratorPadCheck
// CSOFF: LineLengthCheck
// CSOFF: NoWhitespaceAfterCheck
// CSOFF: SimplifyBooleanReturnCheck

/**
 * The <code>UnflattenProcessor</code> class is used to reconstruct (by unflattening) a line
 * area's internal area hierarachy after leaf inline area reordering is completed.
 *
 * @author Glenn Adams
 */
class UnflattenProcessor {
    private List<InlineArea>        il;             // list of flattened inline areas being unflattened
    private List<InlineArea>        ilNew;          // list of unflattened inline areas being constructed
    private int                     iaLevelLast;    // last (previous) level of current inline area (if applicable) or -1
    private TextArea                tcOrig;         // original text area container
    private TextArea                tcNew;          // new text area container being constructed
    private Stack<InlineParent>     icOrig;         // stack of original inline parent containers
    private Stack<InlineParent>     icNew;          // stack of new inline parent containers being constructed
    UnflattenProcessor ( List<InlineArea> inlines ) {
        this.il = inlines;
        this.ilNew = new ArrayList<InlineArea>();
        this.iaLevelLast = -1;
        this.icOrig = new Stack<InlineParent>();
        this.icNew = new Stack<InlineParent>();
    }
    List unflatten() {
        if ( il != null ) {
            for ( Iterator<InlineArea> it = il.iterator(); it.hasNext(); ) {
                process ( it.next() );
            }
        }
        finishAll();
        return ilNew;
    }
    private void process ( InlineArea ia ) {
        process ( findInlineContainers ( ia ), findTextContainer ( ia ), ia );
    }
    private void process ( List<InlineParent> ich, TextArea tc, InlineArea ia ) {
        if ( ( tcNew == null ) || ( tc != tcNew ) ) {
            maybeFinishTextContainer ( tc, ia );
            maybeFinishInlineContainers ( ich, tc, ia );
            update ( ich, tc, ia );
        } else {
            // skip inline area whose text container is the current new text container,
            // which occurs in the context of the inline runs produced by a filled area
        }
    }
    private boolean shouldFinishTextContainer ( TextArea tc, InlineArea ia ) {
        if ( ( tcOrig != null ) && ( tc != tcOrig ) ) {
            return true;
        } else if ( ( iaLevelLast != -1 ) && ( ia.getBidiLevel() != iaLevelLast ) ) {
            return true;
        } else {
            return false;
        }
    }
    private void finishTextContainer() {
        finishTextContainer ( null, null );
    }
    private void finishTextContainer ( TextArea tc, InlineArea ia ) {
        if ( tcNew != null ) {
            updateIPD ( tcNew );
            if ( ! icNew.empty() ) {
                icNew.peek().addChildArea ( tcNew );
            } else {
                ilNew.add ( tcNew );
            }
        }
        tcNew = null;
    }
    private void maybeFinishTextContainer ( TextArea tc, InlineArea ia ) {
        if ( shouldFinishTextContainer ( tc, ia ) ) {
            finishTextContainer ( tc, ia );
        }
    }
    private boolean shouldFinishInlineContainer ( List<InlineParent> ich, TextArea tc, InlineArea ia ) {
        if ( ( ich == null ) || ich.isEmpty() ) {
            return ! icOrig.empty();
        } else {
            if ( ! icOrig.empty() ) {
                InlineParent ic  = ich.get(0);
                InlineParent ic0 = icOrig.peek();
                return ( ic != ic0 ) && ! isInlineParentOf ( ic, ic0 );
            } else {
                return false;
            }
        }
    }
    private void finishInlineContainer() {
        finishInlineContainer ( null, null, null );
    }
    private void finishInlineContainer ( List<InlineParent> ich, TextArea tc, InlineArea ia ) {
        if ( ( ich != null ) && ! ich.isEmpty() ) {     // finish non-matching inner inline container(s)
            for ( Iterator<InlineParent> it = ich.iterator(); it.hasNext(); ) {
                InlineParent ic  = it.next();
                InlineParent ic0 = icOrig.empty() ? null : icOrig.peek();
                if ( ic0 == null ) {
                    assert icNew.empty();
                } else if ( ic != ic0 ) {
                    assert ! icNew.empty();
                    InlineParent icO0 = icOrig.pop();
                    InlineParent icN0 = icNew.pop();
                    assert icO0 != null;
                    assert icN0 != null;
                    if ( icNew.empty() ) {
                        ilNew.add ( icN0 );
                    } else {
                        icNew.peek().addChildArea ( icN0 );
                    }
                    if ( ! icOrig.empty() && ( icOrig.peek() == ic ) ) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else {                                        // finish all inline containers
            while ( ! icNew.empty() ) {
                InlineParent icO0 = icOrig.pop();
                InlineParent icN0 = icNew.pop();
                assert icO0 != null;
                assert icN0 != null;
                if ( icNew.empty() ) {
                    ilNew.add ( icN0 );
                } else {
                    icNew.peek().addChildArea ( icN0 );
                }
            }
        }
    }
    private void maybeFinishInlineContainers ( List<InlineParent> ich, TextArea tc, InlineArea ia ) {
        if ( shouldFinishInlineContainer ( ich, tc, ia ) ) {
            finishInlineContainer ( ich, tc, ia );
        }
    }
    private void finishAll() {
        finishTextContainer();
        finishInlineContainer();
    }
    private void update ( List<InlineParent> ich, TextArea tc, InlineArea ia ) {
        if ( ! alreadyUnflattened ( ia ) ) {
            if ( ( ich != null ) && ! ich.isEmpty() ) {
                pushInlineContainers ( ich );
            }
            if ( tc != null ) {
                pushTextContainer ( tc, ia );
            } else {
                pushNonTextInline ( ia );
            }
            iaLevelLast = ia.getBidiLevel();
            tcOrig = tc;
        } else  if ( tcNew != null ) {
            finishTextContainer();
            tcOrig = null;
        } else {
            tcOrig = null;
        }
    }
    private boolean alreadyUnflattened ( InlineArea ia ) {
        for ( Iterator<InlineArea> it = ilNew.iterator(); it.hasNext(); ) {
            if ( ia.isAncestorOrSelf ( it.next() ) ) {
                return true;
            }
        }
        return false;
    }
    private void pushInlineContainers ( List<InlineParent> ich ) {
        LinkedList<InlineParent> icl = new LinkedList<InlineParent>();
        for ( Iterator<InlineParent> it = ich.iterator(); it.hasNext(); ) {
            InlineParent ic = it.next();
            if ( icOrig.search ( ic ) >= 0 ) {
                break;
            } else {
                icl.addFirst ( ic );
            }
        }
        for ( Iterator<InlineParent> it = icl.iterator(); it.hasNext(); ) {
            InlineParent ic = it.next();
            icOrig.push ( ic );
            icNew.push ( generateInlineContainer ( ic ) );
        }
    }
    private void pushTextContainer ( TextArea tc, InlineArea ia ) {
        if ( tc instanceof UnresolvedPageNumber ) {
            tcNew = tc;
        } else {
            if ( tcNew == null ) {
                tcNew = generateTextContainer ( tc );
            }
            tcNew.addChildArea ( ia );
        }
    }
    private void pushNonTextInline ( InlineArea ia ) {
        if ( icNew.empty() ) {
            ilNew.add ( ia );
        } else {
            icNew.peek().addChildArea ( ia );
        }
    }
    private InlineParent generateInlineContainer ( InlineParent i ) {
        if ( i instanceof BasicLinkArea ) {
            return generateBasicLinkArea ( (BasicLinkArea) i );
        } else if ( i instanceof FilledArea ) {
            return generateFilledArea ( (FilledArea) i );
        } else {
            return generateInlineContainer0 ( i );
        }
    }
    private InlineParent generateBasicLinkArea ( BasicLinkArea l ) {
        BasicLinkArea lc = new BasicLinkArea();
        if ( l != null ) {
            initializeInlineContainer ( lc, l );
            initializeLinkArea ( lc, l );
        }
        return lc;
    }
    private void initializeLinkArea ( BasicLinkArea lc, BasicLinkArea l ) {
        assert lc != null;
        assert l != null;
        LinkResolver r = l.getResolver();
        if ( r != null ) {
            String[] idrefs = r.getIDRefs();
            if ( idrefs.length > 0 ) {
                String idref = idrefs[0];
                LinkResolver lr = new LinkResolver ( idref, lc );
                lc.setResolver ( lr );
                r.addDependent ( lr );
            }
        }
    }
    private InlineParent generateFilledArea ( FilledArea f ) {
        FilledArea fc = new FilledArea();
        if ( f != null ) {
            initializeInlineContainer ( fc, f );
            initializeFilledArea ( fc, f );
        }
        return fc;
    }
    private void initializeFilledArea ( FilledArea fc, FilledArea f ) {
        assert fc != null;
        assert f != null;
        fc.setIPD ( f.getIPD() );
        fc.setUnitWidth ( f.getUnitWidth() );
    }
    private InlineParent generateInlineContainer0 ( InlineParent i ) {
        InlineParent ic = new InlineParent();
        if ( i != null ) {
            initializeInlineContainer ( ic, i );
        }
        return ic;
    }
    private void initializeInlineContainer ( InlineParent ic, InlineParent i ) {
        assert ic != null;
        assert i != null;
        ic.setTraits ( i.getTraits() );
        ic.setBPD ( i.getBPD() );
        ic.setBlockProgressionOffset ( i.getBlockProgressionOffset() );
    }
    private TextArea generateTextContainer ( TextArea t ) {
        TextArea tc = new TextArea();
        if ( t != null ) {
            tc.setTraits ( t.getTraits() );
            tc.setBPD ( t.getBPD() );
            tc.setBlockProgressionOffset ( t.getBlockProgressionOffset() );
            tc.setBaselineOffset ( t.getBaselineOffset() );
            tc.setTextWordSpaceAdjust ( t.getTextWordSpaceAdjust() );
            tc.setTextLetterSpaceAdjust ( t.getTextLetterSpaceAdjust() );
        }
        return tc;
    }
    private void updateIPD ( TextArea tc ) {
        int numAdjustable = 0;
        for ( Iterator it = tc.getChildAreas().iterator(); it.hasNext(); ) {
            InlineArea ia = (InlineArea) it.next();
            if ( ia instanceof SpaceArea ) {
                SpaceArea sa = (SpaceArea) ia;
                if ( sa.isAdjustable() ) {
                    numAdjustable++;
                }
            }
        }
        if ( numAdjustable > 0 ) {
            tc.setIPD ( tc.getIPD() + ( numAdjustable * tc.getTextWordSpaceAdjust() ) );
        }
    }
    private TextArea findTextContainer ( InlineArea ia ) {
        assert ia != null;
        TextArea t = null;
        while ( t == null ) {
            if ( ia instanceof TextArea ) {
                t = (TextArea) ia;
            } else {
                Area p = ia.getParentArea();
                if ( p instanceof InlineArea ) {
                    ia = (InlineArea) p;
                } else {
                    break;
                }
            }
        }
        return t;
    }
    private List<InlineParent> findInlineContainers ( InlineArea ia ) {
        assert ia != null;
        List<InlineParent> ich = new ArrayList<InlineParent>();
        Area a = ia.getParentArea();
        while ( a != null ) {
            if ( a instanceof InlineArea ) {
                if ( ( a instanceof InlineParent ) && ! ( a instanceof TextArea ) ) {
                    ich.add ( (InlineParent) a );
                }
                a = ( (InlineArea) a ) .getParentArea();
            } else {
                a = null;
            }
        }
        return ich;
    }
    private boolean isInlineParentOf ( InlineParent ic0, InlineParent ic1 ) {
        assert ic0 != null;
        return ic0.getParentArea() == ic1;
    }
}
