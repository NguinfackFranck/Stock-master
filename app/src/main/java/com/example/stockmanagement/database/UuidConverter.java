package com.example.stockmanagement.database;

import androidx.room.TypeConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

public class UuidConverter {
    private UuidConverter() {}
    /**
     * Converts a Java UUID to a compact 16-byte array.
     * Optimal for database storage (e.g., BINARY(16) in MySQL).
     */
    @TypeConverter
    public static byte[] toBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    /**
     * Converts a 16-byte array back into a Java UUID.
     */

    @TypeConverter
    public static UUID fromBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Byte array must be exactly 16 bytes long");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }
}
