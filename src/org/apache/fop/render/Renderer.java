/*-- $Id$ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:
 
 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.
 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
 
 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.
 
 4. The names "Fop" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.
 
 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.
 
 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 James Tauber <jtauber@jtauber.com>. For more  information on the Apache 
 Software Foundation, please see <http://www.apache.org/>.
 
 */
package org.apache.fop.render;

// FOP
import org.apache.fop.svg.SVGArea;
import org.apache.fop.image.ImageArea;
import org.apache.fop.apps.FOPException;
import org.apache.fop.layout.*;
import org.apache.fop.layout.inline.*;

// Java
import java.io.OutputStream;
import java.io.IOException;

/**
 * interface implement by all renderers.
 *
 * a Renderer implementation takes areas/spaces and produces output in
 * some format.
 */
public interface Renderer {
    
    /** set up the given FontInfo */
    public void setupFontInfo(FontInfo fontInfo);

    /** set the producer of the rendering */
    public void setProducer(String producer);

    /** render the given area tree to the given stream */
    public void render(AreaTree areaTree, OutputStream stream) throws IOException, FOPException;
 
    /** render the given area container */
    public void renderAreaContainer(AreaContainer area);

    /** render the given area container */
    public void renderBodyAreaContainer(BodyAreaContainer area);

    /** render the given span area */
	public void renderSpanArea(SpanArea area);

    /** render the given block area */
    public void renderBlockArea(BlockArea area);

    /** render the given display space */
    public void renderDisplaySpace(DisplaySpace space);

    /** render the given SVG area */
    public void renderSVGArea(SVGArea area);

    /** render a foreign object area */
    public void renderForeignObjectArea(ForeignObjectArea area);

    /** render the given image area */
    public void renderImageArea(ImageArea area);

    /** render the given inline area */
    public void renderWordArea(WordArea area);

    /** render the given inline space */
    public void renderInlineSpace(InlineSpace space);

    /** render the given line area */
    public void renderLineArea(LineArea area);

    /** render the given page */
    public void renderPage(Page page);

    /** render the given leader area */
    public void renderLeaderArea(LeaderArea area);

}
