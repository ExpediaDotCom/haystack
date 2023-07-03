package com.expedia.www.haystack.span.decorators.plugin.config;

import com.typesafe.config.Config;

public class PluginConfiguration {
    private String name;
    private Config config;

    public PluginConfiguration(String name, Config config) {
        this.name = name;
        this.config = config;
    }

    public PluginConfiguration() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }
}
