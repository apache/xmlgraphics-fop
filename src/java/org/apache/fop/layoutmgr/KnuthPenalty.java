package org.apache.fop.layoutmgr;

public class KnuthPenalty extends KnuthElement {
    private int penalty;
    private boolean bFlagged; 

    public KnuthPenalty(int w, int p, boolean f, Position pos, boolean bAux) {
        super(KNUTH_PENALTY, w, pos, bAux);
        penalty = p;
        bFlagged = f;
    }

    public int getP() {
        return penalty;
    }

    public boolean isFlagged() {
        return bFlagged;
    }
}