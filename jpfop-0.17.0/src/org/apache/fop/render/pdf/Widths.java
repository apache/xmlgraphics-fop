package org.apache.fop.render.pdf;

import java.util.Vector;

public class Widths {
    private Vector width = null;
    private Vector cidWidth = null;
	private int[] widths = null;

	/**
	 * Used for Type1 , TrueType , MMType1 , Type3 fonts . 
	 */
	public Widths() {
		width = new Vector();
		cidWidth = new Vector();
	}

	/**
	 * Used for Type0 fonts . 
	 */
	public Widths( int[] widths ) {
		this.widths = widths;
	}

    public void addWidths(int start, int end, int width ) {
        (this.width).addElement(new CIDFontWidthsEntry(start,end,width));
    }

    public void addWidths(int start, int[] widths) {
        width.addElement(new CIDFontWidthsEntry(start,widths));
    }

	/**
	 * Add Cfirst Clast W format entry . 
	 * Used for Type0 fonts . 
	 */
    public void addElement(int start, int end, int width ) {
        cidWidth.addElement(new CIDFontWidthsEntry(start,end,width));
    }

	/**
	 * Add C [ W1 W2 ... Wn ] format entry . 
	 * Used for Type0 fonts . 
	 */
    public void addElement(int start, int[] widths) {
        cidWidth.addElement(new CIDFontWidthsEntry(start,widths));
    }

	/**
	 * Get widths for specified code point . 
	 */
    public int getWidth(int codePoint) {
        if ( widths != null ) {
			return widths[codePoint];
        } else {
            for(int i = 0; i < width.size(); i++) {
                try {
                    return ((CIDFontWidthsEntry)width.elementAt(i)).getWidth(codePoint);
                } catch(ArrayIndexOutOfBoundsException ex) {}
            }
        }
        return -1;
    }

	public String toString() {
        StringBuffer sb = new StringBuffer();
        if ( widths != null ) {
            for ( int i = 0; i < widths.length; i++ ) {
                sb.append(widths[i]);
                sb.append(" ");
            }
        } else {
            for ( int i = 0; i < cidWidth.size(); i++ ) {
                ((CIDFontWidthsEntry)cidWidth.elementAt(i)).toString(sb);
            }
        }
        return sb.toString();
    }

}
