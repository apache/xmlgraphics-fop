package org.apache.fop.render.pdf;

/**
 * This object is used for widths in CIDFonts . 
 * The entry of Widths for a CIDFont allows two defferent formats .
 * For more details , see PDF specification p.213 .
 */
public class CIDFontWidthsEntry {
    int[] widths = null;
    int width = 0;
    int start = 0;
    int end = 0;

	/**
	 * C [ W1 W2 ... Wn ] format entry . 
	 */
    CIDFontWidthsEntry(int start, int[] widths) {
        this.start = start;
        this.end = start+widths.length;
        this.widths=widths;
    }

	/**
	 * Cfirst Clast W format entry . 
	 */
    CIDFontWidthsEntry(int start, int end, int width) {
        this.start = start;
        this.end = end;
        this.width = width;
    }

	/**
	 * Get widths for specified code point . 
	 */
    public int getWidth(int codePoint) throws ArrayIndexOutOfBoundsException {
        if (codePoint<start || end<codePoint)
            throw new ArrayIndexOutOfBoundsException();
        return ( widths == null ) ? width : widths[codePoint-start];
    }

	public void toString(StringBuffer sb) {
        sb.append(start);
        if ( widths == null ) {
            sb.append(" ");
            sb.append(end);
            sb.append(" ");
            sb.append(width);
            sb.append("\n");
		} else {
            sb.append(" [ ");
            for ( int i = 0; i < widths.length; i++ ) {
                sb.append(widths[i]);
                sb.append(" ");
            }
            sb.append("]\n");
        }
    }
}
