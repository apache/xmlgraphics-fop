/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding.model;

//Java
import java.util.Iterator;
import java.io.IOException;

//SAX
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import embedding.tools.AbstractObjectReader;

/**
 * XMLReader implementation for the ProjectTeam class. This class is used to
 * generate SAX events from the ProjectTeam class.
 */
public class ProjectTeamXMLReader extends AbstractObjectReader {

    /**
     * @see org.xml.sax.XMLReader#parse(InputSource)
     */
    public void parse(InputSource input) throws IOException, SAXException {
        if (input instanceof ProjectTeamInputSource) {
            parse(((ProjectTeamInputSource)input).getProjectTeam());
        } else {
            throw new SAXException("Unsupported InputSource specified. Must be a ProjectTeamInputSource");
        }
    }


    /**
     * Starts parsing the ProjectTeam object.
     * @param projectTeam The object to parse
     * @throws SAXException In case of a problem during SAX event generation
     */
    public void parse(ProjectTeam projectTeam) throws SAXException {
        if (projectTeam == null) {
            throw new NullPointerException("Parameter projectTeam must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        //Start the document
        handler.startDocument();
        
        //Generate SAX events for the ProjectTeam
        generateFor(projectTeam);
        
        //End the document
        handler.endDocument();        
    }

    
    /**
     * Generates SAX events for a ProjectTeam object.
     * @param projectTeam ProjectTeam object to use
     * @throws SAXException In case of a problem during SAX event generation
     */
    protected void generateFor(ProjectTeam projectTeam) throws SAXException {
        if (projectTeam == null) {
            throw new NullPointerException("Parameter projectTeam must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("projectteam");
        handler.element("projectname", projectTeam.getProjectName());
        Iterator i = projectTeam.getMembers().iterator();
        while (i.hasNext()) {
            ProjectMember member = (ProjectMember)i.next();
            generateFor(member);
        }
        handler.endElement("projectteam");
    }

    /**
     * Generates SAX events for a ProjectMember object.
     * @param projectMember ProjectMember object to use
     * @throws SAXException In case of a problem during SAX event generation
     */
    protected void generateFor(ProjectMember projectMember) throws SAXException {
        if (projectMember == null) {
            throw new NullPointerException("Parameter projectMember must not be null");
        }
        if (handler == null) {
            throw new IllegalStateException("ContentHandler not set");
        }
        
        handler.startElement("member");
        handler.element("name", projectMember.getName());
        handler.element("function", projectMember.getFunction());
        handler.element("email", projectMember.getEmail());
        handler.endElement("member");
    }

}
