/*
 * $Id$
 * Copyright (C) 2002-2003 The Apache Software Foundation. All rights reserved.
 * For details on use and redistribution please refer to the
 * LICENSE file included with these sources.
 */
package embedding.model;

/**
 * This bean represents a project member.
 */
public class ProjectMember {

    private String name;
    private String function;
    private String email;


    /**
     * Default no-parameter constructor.
     */
    public ProjectMember() {
    }
    
    
    /**
     * Convenience constructor.
     * @param name
     * @param function
     * @param email
     */
    public ProjectMember(String name, String function, String email) {
        setName(name);
        setFunction(function);
        setEmail(email);
    }

    /**
     * Returns the name.
     * @return String the name
     */
    public String getName() {
        return name;
    }


    /**
     * Returns the function.
     * @return String the function
     */
    public String getFunction() {
        return function;
    }


    /**
     * Returns the email address.
     * @return String the email address
     */
    public String getEmail() {
        return email;
    }


    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }


    /**
     * Sets the function.
     * @param function The function to set
     */
    public void setFunction(String function) {
        this.function = function;
    }


    /**
     * Sets the email address.
     * @param email The email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

}
