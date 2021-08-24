package com.hurek.wenbot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Component
@Validated
@ConfigurationProperties(prefix = "wenbot.discord")
public class WenBotProperties {
    @NotNull
    @NotEmpty
    String prodChannelId;

    @NotNull
    @NotEmpty
    String devChannelId;
}
