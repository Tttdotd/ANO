package com.tdotd.ano.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 开发期默认用户等约定配置，后续可替换为登录态。
 */
@ConfigurationProperties(prefix = "ano")
public record AnoProperties(Security security) {

    public record Security(String devUserId) {
    }
}
