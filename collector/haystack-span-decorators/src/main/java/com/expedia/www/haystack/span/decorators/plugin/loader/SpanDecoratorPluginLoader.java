package com.expedia.www.haystack.span.decorators.plugin.loader;

import com.expedia.www.haystack.span.decorators.SpanDecorator;

import com.expedia.www.haystack.span.decorators.plugin.config.Plugin;
import com.expedia.www.haystack.span.decorators.plugin.config.PluginConfiguration;
import org.slf4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

public class SpanDecoratorPluginLoader {
    private Logger logger;
    private Plugin pluginConfig;
    private static SpanDecoratorPluginLoader spanDecoratorPluginLoader;
    private ServiceLoader<SpanDecorator> loader;

    private SpanDecoratorPluginLoader(Logger logger, Plugin pluginConfig) {
        this.logger = logger;
        this.pluginConfig = pluginConfig;
    }

    public static synchronized SpanDecoratorPluginLoader getInstance(Logger logger, Plugin pluginConfig) {
        if (spanDecoratorPluginLoader == null) {
            spanDecoratorPluginLoader = new SpanDecoratorPluginLoader(logger, pluginConfig);
        }
        spanDecoratorPluginLoader.createLoader();

        return spanDecoratorPluginLoader;
    }

    private void createLoader() {
        try {
            final File[] pluginFiles = new File(pluginConfig.getDirectory()).listFiles();
            if (pluginFiles != null) {
                final List<URL> urls = new ArrayList<>();
                for (final File file : pluginFiles) {
                    urls.add(file.toURI().toURL());
                }
                URLClassLoader urlClassLoader = new URLClassLoader(urls.toArray(new URL[0]), SpanDecorator.class.getClassLoader());
                loader = ServiceLoader.load(SpanDecorator.class, urlClassLoader);
            }
        } catch (Exception ex) {
            logger.error("Could not create the class loader for finding jar ", ex);
        } catch (NoClassDefFoundError ex) {
            logger.error("Could not find the class ", ex);
        }
    }

    public List<SpanDecorator> getSpanDecorators() {
        List<SpanDecorator> spanDecorators = new ArrayList<>();
        try {
            loader.forEach((spanDecorator) -> {
                final PluginConfiguration validFirstConfig = pluginConfig.getPluginConfigurationList().stream().filter(pluginConfiguration ->
                        pluginConfiguration.getName().equals(spanDecorator.name())).findFirst().orElse(null);
                if (validFirstConfig != null) {
                    spanDecorator.init(validFirstConfig.getConfig());
                    spanDecorators.add(spanDecorator);
                    logger.info("Successfully loaded the plugin {}", spanDecorator.name());
                }
            });
        } catch (Exception ex) {
            logger.error("Unable to load the external span decorators ", ex);
        }

        return spanDecorators;
    }
}
