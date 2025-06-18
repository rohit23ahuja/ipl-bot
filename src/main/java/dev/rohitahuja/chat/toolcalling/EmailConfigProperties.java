package dev.rohitahuja.chat.toolcalling;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "email")
public record EmailConfigProperties(String username, String password) {
}