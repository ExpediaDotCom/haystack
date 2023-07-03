package com.expedia.www.haystack.span.decorators.plugin.config;

import java.util.List;

public class Plugin {
    private String directory;
    private List<PluginConfiguration> pluginConfigurationList;

    public Plugin(String directory, List<PluginConfiguration> pluginConfigurationList) {
        this.directory = directory;
        this.pluginConfigurationList = pluginConfigurationList;
    }

    public String getDirectory() {
        return directory;
    }

    public List<PluginConfiguration> getPluginConfigurationList() {
        return pluginConfigurationList;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setPluginConfigurationList(List<PluginConfiguration> pluginConfigurationList) {
        this.pluginConfigurationList = pluginConfigurationList;
    }
}
