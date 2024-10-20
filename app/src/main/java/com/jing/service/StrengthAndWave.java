package com.jing.service;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @Author：静
 * @Package：com.jing.service
 * @Project：灵静
 * @name：StrengthAndWave
 * @Date：2024/10/19 下午4:51
 * @Filename：StrengthAndWave
 * @Version：1.0.0
 */
public class StrengthAndWave {


    public final byte[][] waveforms1 = {
            new byte[]{1, 9, 4},
            new byte[]{1, 9, 8},
            new byte[]{1, 9, 12},
            new byte[]{1, 9, 16},
            new byte[]{1, 9, 18},
            new byte[]{1, 9, 19},
            new byte[]{1, 9, 20},
            new byte[]{1, 9, 0},
            new byte[]{1, 9, 0},
            new byte[]{1, 9, 0}
    };
    public static byte[] wave(int x, int y, int z) {
        int xBits = x & 0x1F;
        int yBits = (y & 0x3FF) << 5;
        int zBits = (z & 0x1F) << 15;
        int data = zBits | yBits | xBits;

        return new byte[]{
                (byte) (data & 0xFF),          // 第一个字节
                (byte) ((data >> 8) & 0xFF),   // 第二个字节
                (byte) ((data >> 16) & 0xFF)   // 第三个字节
        };
    }

    public static byte[] abPowerToByte(int a, int b) {
        int aChannelBits = a & 0x7FF;
        int bChannelBits = (b & 0x7FF) << 11;
        int data = (bChannelBits | aChannelBits) & 0xFFFFFF;
/*      ByteBuffer byteBuffer= ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(data);
        byte[] bytes = new byte[3];
        byteBuffer.rewind();
        byteBuffer.get(bytes);
        return bytes;*/
        return new byte[]{
                (byte) (data & 0xFF),         // 第一个字节
                (byte) ((data >> 8) & 0xFF),  // 第二个字节
                (byte) ((data >> 16) & 0xFF)  // 第三个字节
        };
    }
}
