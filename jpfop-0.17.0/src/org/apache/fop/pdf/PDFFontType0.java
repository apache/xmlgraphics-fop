package org.apache.fop.pdf;

public class PDFFontType0 extends PDFFont {

	/** descendant font */
	protected PDFCIDFont descendant = null;

	public PDFFontType0(int number, String fontname,
		String basefont, Object encoding, PDFCIDFont descendant) {
		super(number, fontname, TYPE0, basefont, encoding);
		this.descendant = descendant;
	}

	protected void fillInPDF(StringBuffer p) {
		p.append("\n/DescendantFonts [ ");
		p.append(descendant.referencePDF());
		p.append(" ]");
	}
}
