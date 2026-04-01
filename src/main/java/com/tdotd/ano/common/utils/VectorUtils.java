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
}
