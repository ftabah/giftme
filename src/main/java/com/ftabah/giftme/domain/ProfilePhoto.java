package com.ftabah.giftme.domain;

import java.util.Arrays;

/** Foto de perfil validada por formato e tamanho máximo. */
public final class ProfilePhoto {

    public static final int MAX_BYTES = 2 * 1024 * 1024;
        private static final byte[] PNG_SIGNATURE = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };
        private static final byte[] JPEG_SIGNATURE = {(byte) 0xff, (byte) 0xd8, (byte) 0xff};
        private final String mediaType;

    private final byte[] bytes;

    public ProfilePhoto(byte[] bytes, String mediaType) {
        if (bytes == null || bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new IllegalArgumentException("A foto deve ter entre 1 byte e 2 MiB");
        }
        boolean png = "image/png".equals(mediaType) && hasSignature(bytes, PNG_SIGNATURE);
        boolean jpeg = "image/jpeg".equals(mediaType) && hasSignature(bytes, JPEG_SIGNATURE);
        if (!png && !jpeg) {
            throw new IllegalArgumentException("A foto deve ser um PNG ou JPEG válido");
        }
        this.bytes = bytes.clone();
        this.mediaType = mediaType;
    }

    public byte[] bytes() {
        return bytes.clone();
    }

    public String mediaType() {
        return mediaType;
    }

    private static boolean hasSignature(byte[] bytes, byte[] signature) {
        return bytes.length >= signature.length && Arrays.equals(Arrays.copyOf(bytes, signature.length), signature);
    }
}