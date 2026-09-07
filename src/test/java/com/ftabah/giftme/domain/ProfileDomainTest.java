package com.ftabah.giftme.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileDomainTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final byte[] PNG = {
            (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a
    };
        private static final byte[] JPEG = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x01};

    @Test
    void acceptsValidProfileAndCustomSize() {
        Profile profile = validProfile().withCustomSize(new CustomSize("Glove", "L"));

        assertThat(profile.name()).isEqualTo("Ana");
        assertThat(profile.customSizes()).extracting(CustomSize::name).containsExactly("Glove");
    }

    @Test
    void rejectsInvalidAgeAndBlankRequiredFields() {
        assertThatThrownBy(() -> new Profile(USER_ID, "Ana", 151, "1.70 m", "38", "70", "M", List.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Profile(USER_ID, " ", 30, "1.70 m", "38", "70", "M", List.of(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDuplicateCustomNamesIgnoringCaseWithoutChangingPreviousProfile() {
        Profile profile = validProfile().withCustomSize(new CustomSize("Glove", "L"));

        assertThatThrownBy(() -> profile.withCustomSize(new CustomSize("gLoVe", "XL")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(profile.customSizes()).extracting(CustomSize::value).containsExactly("L");
    }

    @Test
    void acceptsPngAndJpegAndRejectsWrongTypeOrOversizedPhoto() {
        ProfilePhoto photo = new ProfilePhoto(PNG, "image/png");
        assertThat(photo.mediaType()).isEqualTo("image/png");
        assertThat(photo.bytes()).containsExactly(PNG);
        assertThat(new ProfilePhoto(JPEG, "image/jpeg").mediaType()).isEqualTo("image/jpeg");

        assertThatThrownBy(() -> new ProfilePhoto(new byte[]{1, 2, 3}, "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class);
        byte[] oversized = new byte[ProfilePhoto.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, oversized, 0, PNG.length);
        assertThatThrownBy(() -> new ProfilePhoto(oversized, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private Profile validProfile() {
        return new Profile(USER_ID, "Ana", 30, "1.70 m", "38", "70", "M", List.of(), null);
    }
}