
package org.apache.fop.layoutmgr;

import java.util.LinkedList;

public class KnuthSequence extends LinkedList {
    // number of KnuthElements added by the LineLayoutManager
    public int ignoreAtStart = 0;
    public int ignoreAtEnd = 0;

    public KnuthSequence() {
    }

    public void startSequence() {
    }

    public KnuthSequence endSequence() {
        // remove glue and penalty item at the end of the paragraph
        while (this.size() > ignoreAtStart
               && !((KnuthElement)this.get(this.size() - 1)).isBox()) {
            this.remove(this.size() - 1);
        }
        if (this.size() > ignoreAtStart) {
            // add the elements representing the space at the end of the last line
            // and the forced break
/*LF*/      this.add(new KnuthPenalty(0, KnuthElement.INFINITE, false, null, false));
/*LF*/      this.add(new KnuthGlue(0, 10000000, 0, null, false));
            this.add(new KnuthPenalty(0, -KnuthElement.INFINITE, false, null, false));
            ignoreAtEnd = 3;
            return this;
        } else {
            this.clear();
            return null;
        }
    }
}
