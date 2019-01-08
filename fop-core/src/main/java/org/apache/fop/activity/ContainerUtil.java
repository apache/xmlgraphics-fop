package org.apache.fop.activity;

import org.apache.fop.configuration.Configurable;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;

public class ContainerUtil {

    private ContainerUtil() {
        // Never invoked.
    }

    public static void configure(Configurable configurable, Configuration configuration) {
        try {
            configurable.configure(configuration);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    public static void initialize(Initializable initializable) {
        try {
            initializable.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
