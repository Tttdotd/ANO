package com.tdotd.ano.common.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class VectorUtils {

    private VectorUtils() {
    }

    public static byte[] toBuffer(float[] vector) {
        if (vector == null) {
            return new byte[0];
        }
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (float value : vector) {
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    /**
     * 与 {@link #toBuffer(float[])} 对称：将 little-endian float32 blob 还原为向量。
     *
     * @return 合法时返回 float 数组；blob 为空或长度非 4 的倍数时返回 {@code null}
     */
    public static float[] fromBuffer(byte[] blob) {
        if (blob == null || blob.length == 0 || blob.length % Float.BYTES != 0) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.wrap(blob).order(ByteOrder.LITTLE_ENDIAN);
        float[] out = new float[blob.length / Float.BYTES];
        for (int i = 0; i < out.length; i++) {
            out[i] = buffer.getFloat();
        }
        return out;
    }
}
