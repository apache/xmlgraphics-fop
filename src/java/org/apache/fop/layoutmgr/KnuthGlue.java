package org.apache.fop.layoutmgr;

public class KnuthGlue extends KnuthElement {
    private int stretchability;
    private int shrinkability;

    public KnuthGlue(int w, int y, int z, Position pos, boolean bAux) {
        super(KNUTH_GLUE, w, pos, bAux);
        stretchability = y;
        shrinkability = z;
    }

    public int getY() {
        return stretchability;
    }

    public int getZ() {
        return shrinkability;
    }
}