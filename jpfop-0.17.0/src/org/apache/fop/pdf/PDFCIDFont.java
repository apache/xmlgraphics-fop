package org.apache.fop.pdf;

import org.apache.fop.render.pdf.CIDFont;
import org.apache.fop.render.pdf.Widths;

public class PDFCIDFont extends PDFObject {

	private CIDFont cid = null;
	private PDFOptionalFontDescriptor descriptor = null;

	public PDFCIDFont( int number, CIDFont cid,
										PDFOptionalFontDescriptor descriptor) {
		super(number);
		this.cid = cid;
		this.descriptor = descriptor;
	}

	public byte[] toPDF() {
		StringBuffer p = new StringBuffer();
        p.append(this.number);
		p.append(" ");
		p.append(this.generation);
		p.append(" obj\n<< /Type /Font\n/BaseFont /");
		p.append(cid.getCidBaseFont());
        p.append(" \n/Subtype /CIDFontType");
		p.append(cid.getCidType());
		p.append("\n/CIDSystemInfo << /Registry (");
		p.append(cid.getRegistry());
		p.append(")/Ordering (");
		p.append(cid.getOrdering());
		p.append(")/Supplement ");
		p.append(cid.getSupplement());
		p.append(" >>\n/FontDescriptor ");
		p.append(descriptor.referencePDF());
		if ( cid.getWinCharSet() > -1 ) {
			p.append("\n/WinCharSet ");
			p.append(cid.getWinCharSet());
		}
		if ( cid.getDefaultWidth() > -1 ) {
			p.append("\n/DW ");
			p.append(Integer.toString(cid.getDefaultWidth()));
		}
		Widths w = cid.getWidths();
		if ( w != null ) {
			p.append("\n/W [ \n");
			p.append(w.toString());
			p.append("] ");
		}
		p.append("\n>>\nendobj\n");
		return p.toString().getBytes();
    }
}
