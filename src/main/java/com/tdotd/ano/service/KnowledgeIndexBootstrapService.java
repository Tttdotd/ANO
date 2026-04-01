package com.tdotd.ano.service;

import com.tdotd.ano.common.constant.KnowledgeIndexConstants;
import com.tdotd.ano.config.KnowledgeVectorProperties;
import com.tdotd.ano.domain.entity.KnowledgeNode;
import com.tdotd.ano.mapper.KnowledgeNodeMapper;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ObjectOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class KnowledgeIndexBootstrapService {

    private static final String REDISEARCH_ON = "ON";
    private static final String REDISEARCH_HASH = "HASH";
    private static final String REDISEARCH_PREFIX = "PREFIX";
    private static final String REDISEARCH_SCHEMA = "SCHEMA";
    private static final String REDISEARCH_TAG = "TAG";
    private static final String REDISEARCH_TEXT = "TEXT";
    private static final String REDISEARCH_WEIGHT = "WEIGHT";
    private static final String REDISEARCH_VECTOR = "VECTOR";
    private static final String REDISEARCH_TYPE = "TYPE";
    private static final String REDISEARCH_DIM = "DIM";
    private static final String REDISEARCH_DISTANCE_METRIC = "DISTANCE_METRIC";
    private static final String HNSW_ARGUMENT_COUNT = "6";

    private final StringRedisTemplate stringRedisTemplate;
    private final KnowledgeNodeMapper knowledgeNodeMapper;
    private final KnowledgeVectorProperties knowledgeVectorProperties;

    public KnowledgeIndexBootstrapService(
            StringRedisTemplate stringRedisTemplate,
            KnowledgeNodeMapper knowledgeNodeMapper,
            KnowledgeVectorProperties knowledgeVectorProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.knowledgeNodeMapper = knowledgeNodeMapper;
        this.knowledgeVectorProperties = knowledgeVectorProperties;
    }

    /**
     * 在应用启动时初始化知识检索索引。
     * 若索引已存在则直接跳过；若不存在则创建索引并从 MySQL 全量回灌节点数据到 Redis Hash。
     */
    public void initializeKnowledgeIndex() {
        if (knowledgeIndexExists()) {
            log.info("knowledge index already exists, skip rebuild: indexName={}", KnowledgeIndexConstants.INDEX_NAME);
            return;
        }

        log.info("knowledge index not found, create and reload start: indexName={}", KnowledgeIndexConstants.INDEX_NAME);
        createKnowledgeIndex();
        int count = reloadKnowledgeNodesToRedis();
        log.info("knowledge index create and reload complete: indexName={}, reloadedCount={}",
                KnowledgeIndexConstants.INDEX_NAME, count);
    }

    private boolean knowledgeIndexExists() {
        try {
            executeRedisCommand(KnowledgeIndexConstants.COMMAND_FT_INFO, KnowledgeIndexConstants.INDEX_NAME);
            return true;
        } catch (RuntimeException ex) {
            if (isUnknownIndexException(ex)) {
                log.warn("knowledge index check miss, index will be created: indexName={}",
                        KnowledgeIndexConstants.INDEX_NAME);
                return false;
            }
            log.warn("knowledge index check failed: indexName={}, errorType={}",
                    KnowledgeIndexConstants.INDEX_NAME, ex.getClass().getSimpleName());
            throw new IllegalStateException("检查 RediSearch 索引状态失败: " + KnowledgeIndexConstants.INDEX_NAME, ex);
        }
    }

    private void createKnowledgeIndex() {
        executeRedisCommand(
                KnowledgeIndexConstants.COMMAND_FT_CREATE,
                KnowledgeIndexConstants.INDEX_NAME,
                REDISEARCH_ON, REDISEARCH_HASH,
                REDISEARCH_PREFIX, "1", KnowledgeIndexConstants.KEY_PREFIX,
                REDISEARCH_SCHEMA,
                KnowledgeIndexConstants.FIELD_ID, REDISEARCH_TAG,
                KnowledgeIndexConstants.FIELD_TITLE, REDISEARCH_TEXT, REDISEARCH_WEIGHT, KnowledgeIndexConstants.TITLE_WEIGHT,
                KnowledgeIndexConstants.FIELD_CONTENT, REDISEARCH_TEXT,
                KnowledgeIndexConstants.FIELD_VECTOR, REDISEARCH_VECTOR, KnowledgeIndexConstants.VECTOR_ALGORITHM, HNSW_ARGUMENT_COUNT,
                REDISEARCH_TYPE, KnowledgeIndexConstants.VECTOR_TYPE,
                REDISEARCH_DIM, String.valueOf(knowledgeVectorProperties.resolvedDimension()),
                REDISEARCH_DISTANCE_METRIC, KnowledgeIndexConstants.VECTOR_DISTANCE_METRIC
        );
    }

    private int reloadKnowledgeNodesToRedis() {
        List<KnowledgeNode> nodes = knowledgeNodeMapper.selectList(null);
        if (nodes.isEmpty()) {
            return 0;
        }

        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (KnowledgeNode node : nodes) {
                byte[] key = toBytes(KnowledgeIndexConstants.KEY_PREFIX + node.getId());
                connection.hashCommands().hSet(key, toBytes(KnowledgeIndexConstants.FIELD_ID), toBytes(node.getId()));
                connection.hashCommands().hSet(key, toBytes(KnowledgeIndexConstants.FIELD_TITLE), toBytes(node.getTitle()));
                connection.hashCommands().hSet(key, toBytes(KnowledgeIndexConstants.FIELD_CONTENT), toBytes(node.getContent()));
                connection.hashCommands().hSet(key, toBytes(KnowledgeIndexConstants.FIELD_VECTOR), node.getVector() == null ? new byte[0] : node.getVector());
            }
            return null;
        });
        return nodes.size();
    }

    @SuppressWarnings("resource")
    private Object executeRedisCommand(String command, String... args) {
        return stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            if (!(nativeConnection instanceof RedisAsyncCommands<?, ?>)) {
                throw new IllegalStateException("Unsupported redis connection type: " + nativeConnection.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            RedisAsyncCommands<byte[], byte[]> byteConnection =
                    (RedisAsyncCommands<byte[], byte[]>) nativeConnection;
            CommandArgs<byte[], byte[]> commandArgs = new CommandArgs<>(ByteArrayCodec.INSTANCE);
            for (String arg : args) {
                commandArgs.add(toBytes(arg));
            }
            ProtocolKeyword keyword = () -> toBytes(command);
            try {
                return byteConnection
                        .dispatch(keyword, new ObjectOutput<>(ByteArrayCodec.INSTANCE), commandArgs)
                        .get();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Redis command interrupted: " + command, ex);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof RedisCommandExecutionException commandException) {
                    throw commandException;
                }
                throw new IllegalStateException("Redis command execution failed: " + command, cause);
            }
        });
    }

    private boolean isUnknownIndexException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains(KnowledgeIndexConstants.UNKNOWN_INDEX_ERROR_TOKEN.toLowerCase())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private byte[] toBytes(String value) {
        return (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
    }
}
