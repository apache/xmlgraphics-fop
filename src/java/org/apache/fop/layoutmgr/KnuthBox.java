package org.apache.fop.layoutmgr;

public class KnuthBox extends KnuthElement {
    private int lead;
    private int total;
    private int middle;

    public KnuthBox(int w, int l, int t, int m, Position pos, boolean bAux) {
        super(KNUTH_BOX, w, pos, bAux);
        lead = l;
        total = t;
        middle = m;
    }

    public int getLead() {
        return lead;
    }

    public int getTotal() {
        return total;
    }

    public int getMiddle() {
        return middle;
    }
}