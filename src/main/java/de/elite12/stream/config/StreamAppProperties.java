package de.elite12.stream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "stream")
@Data
public class StreamAppProperties {
    /**
     * Resource ID from where additional Roles are extracted from provided JWT Tokens
     */
    private String oauthResourceId;
}
