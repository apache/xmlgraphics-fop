/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding.model;

import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

/**
 * This bean represents a ProjectTeam.
 */
public class ProjectTeam {

    private String projectName;
    private List members = new java.util.ArrayList();


    /**
     * Returns a list of project members.
     * @return List a list of ProjectMember objects
     */
    public List getMembers() {
        return this.members;
    }


    /**
     * Adds a ProjectMember to this project team.
     * @param member the member to add
     */
    public void addMember(ProjectMember member) {
        this.members.add(member);
    }


    /**
     * Returns the name of the project
     * @return String the name of the project
     */
    public String getProjectName() {
        return projectName;
    }


    /**
     * Sets the name of the project.
     * @param projectName the project name to set
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }


    /**
     * Resturns a Source object for this object so it can be used as input for
     * a JAXP transformation.
     * @return Source The Source object
     */
    public Source getSourceForProjectTeam() {
        return new SAXSource(new ProjectTeamXMLReader(),
                new ProjectTeamInputSource(this));
    }


}
