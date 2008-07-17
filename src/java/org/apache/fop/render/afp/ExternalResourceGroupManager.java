package org.apache.fop.render.afp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.render.afp.modca.ResourceGroup;

/**
 * Manages the use of resource groups (external and internal)
 */
public class ExternalResourceGroupManager {
    
    /** Static logging instance */
    protected static Log log = LogFactory.getLog(ExternalResourceGroupManager.class);

    /** A mapping of external resource destinations to resource groups */
    private Map/*<String,ResourceGroup>*/externalResourceGroups
        = new java.util.HashMap/*<String,ResourceGroup>*/();

    /** Sets the default resource group file */
    private String defaultResourceGroupFilePath;

    /**
     * Default constructor
     */
    public ExternalResourceGroupManager() {
    }

    /**
     * Sets the default resource group file
     *
     * @param resourceGroupFilePath the default resource group file path
     */
    public void setDefaultResourceGroupFilePath(String resourceGroupFilePath) {
        this.defaultResourceGroupFilePath = resourceGroupFilePath;
    }

    /**
     * Returns the corresponding resource group for the given resource level
     *
     * @param level the resource level
     * @return the corresponding resource group for the given resource level
     * or null if not found.
     */
    public ResourceGroup getResourceGroup(ResourceLevel level) {
        ResourceGroup resourceGroup = null;
        // this resource info does not have an external resource group
        // file definition
        String filePath = level.getExternalFilePath();
        if (filePath != null) {
            filePath = level.getExternalFilePath();
            resourceGroup = (ResourceGroup)externalResourceGroups.get(filePath);
            if (resourceGroup == null) {
                resourceGroup = new ResourceGroup();
                externalResourceGroups.put(filePath, resourceGroup);
            }
        } else if (defaultResourceGroupFilePath != null) {
            // fallback to default resource group file
            level.setExternalFilePath(defaultResourceGroupFilePath);
            resourceGroup = getResourceGroup(level);
        }
        return resourceGroup;
    }
 
    /**
     * Writes out all external resource groups
     */
    public void write() {
        // write any external resources
        Iterator it = externalResourceGroups.keySet().iterator();
        while (it.hasNext()) {
            String filePath = (String)it.next();
            ResourceGroup resourceGroup
                = (ResourceGroup)externalResourceGroups.get(filePath);
            OutputStream os = null;
            try {
                log.debug("Writing external AFP resource file " + filePath);
                os = new java.io.FileOutputStream(filePath);
                resourceGroup.write(os);
            } catch (IOException e) {
                log.error(
                        "An error occurred when attempting to write external AFP resource file "
                                + filePath);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        log.error("Failed to close outputstream for external AFP resource file "
                                        + filePath);
                    }
                }
            }
        }
    }
}