package com.rallibau.apps.commons.config.commandweb;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-${env:development}.properties")
public class ConfigurationProvider {
}
