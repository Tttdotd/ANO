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
import java.util.Map;
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
        List<String> nodeIds = new ArrayList<>();
        if (raw instanceof List<?> list) {
            // NOCONTENT（RESP2 常见）：[total_count, doc_id, doc_id, ...]
            if (list.size() <= 1) {
                return Collections.emptyList();
            }
            for (int i = 1; i < list.size(); i++) {
                String nodeId = parseNodeId(list.get(i));
                if (nodeId != null) {
                    nodeIds.add(nodeId);
                }
            }
        } else if (raw instanceof Map<?, ?> map) {
            // RESP3 下 Lettuce 可能会把 FT.SEARCH 结果解析成 MAP（你调试时看到的 LinkedHashMap）
            Object results = extractResultsFromMap(map);
            if (!(results instanceof List<?> resultsList)) {
                return Collections.emptyList();
            }
            for (Object item : resultsList) {
                // 兼容：如果返回的是 [id, score] 这种 pair，优先取第一个元素
                if (item instanceof List<?> pair && !pair.isEmpty()) {
                    String nodeId = parseNodeId(pair.get(0));
                    if (nodeId != null) {
                        nodeIds.add(nodeId);
                    }
                    continue;
                }
                if (item instanceof Map<?, ?> itemMap) {
                    // 你当前调试现象：item 是 LinkedHashMap，包含两个 key/value 对，其中一个 key 应该是 id
                    Object idValue = extractIdFromItemMap(itemMap);
                    String nodeId = parseNodeId(idValue);
                    if (nodeId != null) {
                        nodeIds.add(nodeId);
                    }
                    continue;
                }
                String nodeId = parseNodeId(item);
                if (nodeId != null) {
                    nodeIds.add(nodeId);
                }
            }
        }
        return nodeIds;
    }

    private Object extractIdFromItemMap(Map<?, ?> itemMap) {
        // 常见 key 形态：id / ID / doc_id（你说应当存在 id）
        for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
            String key = asString(entry.getKey());
            if (key == null) {
                continue;
            }
            if ("id".equalsIgnoreCase(key) || KnowledgeIndexConstants.FIELD_ID.equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        // fallback：若 map 里只有两个字段且没有找到“id”，就退化取第一个 value
        for (Map.Entry<?, ?> entry : itemMap.entrySet()) {
            return entry.getValue();
        }
        return null;
    }

    private Object extractResultsFromMap(Map<?, ?> map) {
        // 常见字段名：results / total_results（key 可能是 byte[]）
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = asString(entry.getKey());
            if (key != null && "results".equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String parseNodeId(Object element) {
        if (element == null) {
            return null;
        }
        if (element instanceof byte[] bytes) {
            element = new String(bytes, StandardCharsets.UTF_8);
        }
        String key = asString(element);
        if (key == null || key.isBlank()) {
            return null;
        }
        if (key.startsWith(KnowledgeIndexConstants.KEY_PREFIX)) {
            return key.substring(KnowledgeIndexConstants.KEY_PREFIX.length());
        }
        return key;
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
