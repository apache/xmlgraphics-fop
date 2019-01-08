package org.apache.fop.configuration;

class NullConfiguration implements Configuration {

    static final NullConfiguration INSTANCE = new NullConfiguration();

    private NullConfiguration() {

    }

    @Override
    public Configuration getChild(String key) {
        return INSTANCE;
    }

    @Override
    public Configuration getChild(String key, boolean required) {
        return INSTANCE;
    }

    @Override
    public Configuration[] getChildren(String key) {
        return new Configuration[0];
    }

    @Override
    public String[] getAttributeNames() {
        return new String[0];
    }

    @Override
    public String getAttribute(String key) throws ConfigurationException {
        return "";
    }

    @Override
    public String getAttribute(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public boolean getAttributeAsBoolean(String key, boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public float getAttributeAsFloat(String key) throws ConfigurationException {
        return 0;
    }

    @Override
    public float getAttributeAsFloat(String key, float defaultValue) {
        return defaultValue;
    }

    @Override
    public int getAttributeAsInteger(String key, int defaultValue) {
        return defaultValue;
    }

    @Override
    public String getValue() throws ConfigurationException {
        // return null;
        throw new ConfigurationException("missing value");
    }

    @Override
    public String getValue(String defaultValue) {
        /*
        if (defaultValue == null) {
            defaultValue = "";
        }
         */
        return defaultValue;
    }

    @Override
    public boolean getValueAsBoolean() throws ConfigurationException {
        return false;
    }

    @Override
    public boolean getValueAsBoolean(boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public int getValueAsInteger() throws ConfigurationException {
        return 0;
    }

    @Override
    public int getValueAsInteger(int defaultValue) {
        return defaultValue;
    }

    @Override
    public float getValueAsFloat() throws ConfigurationException {
        return 0;
    }

    @Override
    public float getValueAsFloat(float defaultValue) {
        return defaultValue;
    }

    @Override
    public String getLocation() {
        return "<no-location>";
    }
}
