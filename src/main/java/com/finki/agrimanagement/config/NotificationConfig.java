package com.finki.agrimanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification.email")
@Getter
@Setter
public class NotificationConfig {

    private boolean enabled;
    private String from;
    private String to;
}
