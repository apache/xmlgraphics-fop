/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding.model;

import org.xml.sax.InputSource;

public class ProjectTeamInputSource extends InputSource {

    private ProjectTeam projectTeam;

    /**
     * Constructor for the ProjectTeamInputSource
     * @param projectTeam The ProjectTeam object to use
     */
    public ProjectTeamInputSource(ProjectTeam projectTeam) {
        this.projectTeam = projectTeam;
    }

    /**
     * Returns the projectTeam.
     * @return ProjectTeam
     */
    public ProjectTeam getProjectTeam() {
        return projectTeam;
    }

    /**
     * Sets the projectTeam.
     * @param projectTeam The projectTeam to set
     */
    public void setProjectTeam(ProjectTeam projectTeam) {
        this.projectTeam = projectTeam;
    }

}
