package com.tdotd.ano.infrastructure.persistence;

import com.tdotd.ano.common.constant.KnowledgeArchiveConstants;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Repository
public class RedisVectorRepository {

    private final RedisTemplate<String, byte[]> redisTemplate;

    public RedisVectorRepository(
            @Qualifier("knowledgeRedisTemplate") RedisTemplate<String, byte[]> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(KnowledgeNode node) {
        String redisKey = KnowledgeArchiveConstants.REDIS_KEY_PREFIX + node.getId();
        Map<String, byte[]> fields = new HashMap<>();
        fields.put(KnowledgeArchiveConstants.REDIS_FIELD_ID, toBytes(node.getId()));
        fields.put(KnowledgeArchiveConstants.REDIS_FIELD_TARGET, toBytes(node.getTarget()));
        fields.put(KnowledgeArchiveConstants.REDIS_FIELD_CONTENT, toBytes(node.getContent()));
        fields.put(KnowledgeArchiveConstants.REDIS_FIELD_VECTOR, node.getVector() == null ? new byte[0] : node.getVector());
        redisTemplate.opsForHash().putAll(redisKey, fields);
    }

    private byte[] toBytes(String value) {
        return (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
    }
}
