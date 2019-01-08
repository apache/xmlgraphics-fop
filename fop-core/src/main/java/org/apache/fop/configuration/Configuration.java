package org.apache.fop.configuration;

public interface Configuration {

    Configuration getChild(String key);

    Configuration getChild(String key, boolean required);

    Configuration[] getChildren(String key);

    String[] getAttributeNames();

    String getAttribute(String key) throws ConfigurationException;

    String getAttribute(String key, String defaultValue);

    boolean getAttributeAsBoolean(String key, boolean defaultValue);

    float getAttributeAsFloat(String key) throws ConfigurationException;

    float getAttributeAsFloat(String key, float defaultValue);

    int getAttributeAsInteger(String key, int defaultValue);

    String getValue() throws ConfigurationException;

    String getValue(String defaultValue);

    boolean getValueAsBoolean() throws ConfigurationException;

    boolean getValueAsBoolean(boolean defaultValue);

    int getValueAsInteger() throws ConfigurationException;

    int getValueAsInteger(int defaultValue);

    float getValueAsFloat() throws ConfigurationException;

    float getValueAsFloat(float defaultValue);

    String getLocation();

}
