package com.tdotd.ano.infrastructure.security;

import com.tdotd.ano.config.AnoProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 优先读取请求头 {@code X-User-Id}，否则使用配置的 {@code ano.security.dev-user-id}。
 */
@Component
public class DevUserIdProvider implements UserIdProvider {

    private static final String HEADER = "X-User-Id";

    private final AnoProperties anoProperties;

    public DevUserIdProvider(AnoProperties anoProperties) {
        this.anoProperties = anoProperties;
    }

    @Override
    public String currentUserId() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            String header = servletAttrs.getRequest().getHeader(HEADER);
            if (header != null && !header.isBlank()) {
                return header.trim();
            }
        }
        return anoProperties.security().devUserId();
    }
}
