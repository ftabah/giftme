package com.ftabah.giftme.domain;

import java.util.Arrays;

public final class ProfilePhoto {

    public static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };

    private final byte[] bytes;

    public ProfilePhoto(byte[] bytes, String mediaType) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("Photo must be between 1 byte and 2 MiB");
        }
        if (!Arrays.equals(Arrays.copyOf(bytes, PNG_SIGNATURE.length), PNG_SIGNATURE)) {
            throw new IllegalArgumentException("Photo must be PNG");
        }
        if (!"image/png".equals(mediaType)) {
            throw new IllegalArgumentException("Photo media type must be image/png");
        }
        this.bytes = bytes.clone();
    }

    public byte[] bytes() {
        return bytes.clone();
    }

    public String mediaType() {
        return "image/png";
    }
}