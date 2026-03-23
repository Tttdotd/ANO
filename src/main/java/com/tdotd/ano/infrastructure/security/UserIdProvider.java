package com.tdotd.ano.infrastructure.security;

/**
 * 当前请求所属用户 ID（字符串，与 API/前端精度约定一致）。
 */
public interface UserIdProvider {

    String currentUserId();
}
