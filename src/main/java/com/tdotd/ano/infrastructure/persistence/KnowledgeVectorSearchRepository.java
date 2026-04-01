package com.tdotd.ano.infrastructure.persistence;

import com.tdotd.ano.common.constant.KnowledgeIndexConstants;
import com.tdotd.ano.common.utils.VectorUtils;
import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.output.ObjectOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.ProtocolKeyword;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Repository
public class KnowledgeVectorSearchRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public KnowledgeVectorSearchRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public List<String> searchTopK(float[] vector, int topK) {
        if (vector == null || vector.length == 0 || topK <= 0) {
            return Collections.emptyList();
        }
        byte[] vectorBlob = VectorUtils.toBuffer(vector);
        Object raw = executeRedisCommand(
                "FT.SEARCH",
                KnowledgeIndexConstants.INDEX_NAME,
                "*=>[KNN %d @%s $BLOB AS score]".formatted(topK, KnowledgeIndexConstants.FIELD_VECTOR),
                "PARAMS", "2", "BLOB", vectorBlob,
                "SORTBY", "score",
                "DIALECT", "2",
                "NOCONTENT"
        );
        if (!(raw instanceof List<?> list) || list.size() <= 1) {
            return Collections.emptyList();
        }
        List<String> nodeIds = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            String key = asString(list.get(i));
            if (key == null || key.isBlank()) {
                continue;
            }
            if (key.startsWith(KnowledgeIndexConstants.KEY_PREFIX)) {
                nodeIds.add(key.substring(KnowledgeIndexConstants.KEY_PREFIX.length()));
            } else {
                nodeIds.add(key);
            }
        }
        return nodeIds;
    }

    @SuppressWarnings("resource")
    private Object executeRedisCommand(String command, Object... args) {
        return stringRedisTemplate.execute((RedisCallback<Object>) connection -> {
            Object nativeConnection = connection.getNativeConnection();
            if (!(nativeConnection instanceof RedisAsyncCommands<?, ?>)) {
                throw new IllegalStateException("Unsupported redis connection type: " + nativeConnection.getClass().getName());
            }
            @SuppressWarnings("unchecked")
            RedisAsyncCommands<byte[], byte[]> byteConnection =
                    (RedisAsyncCommands<byte[], byte[]>) nativeConnection;
            CommandArgs<byte[], byte[]> commandArgs = new CommandArgs<>(ByteArrayCodec.INSTANCE);
            for (Object arg : args) {
                if (arg instanceof byte[] bytes) {
                    commandArgs.add(bytes);
                } else {
                    commandArgs.add(toBytes(String.valueOf(arg)));
                }
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

    private String asString(Object value) {
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return value == null ? null : String.valueOf(value);
    }

    private byte[] toBytes(String value) {
        return (value == null ? "" : value).getBytes(StandardCharsets.UTF_8);
    }
}
