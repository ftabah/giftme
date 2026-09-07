package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilePhotoServiceTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};

    @Test
    void savesPngAndRejectsInvalidPhotoWithoutReplacingPreviousPhoto() throws Exception {
        UUID userId = UUID.randomUUID();
        Profile initial = new Profile(userId, "Ana", 30, "1.70 m", "38", "70", "M", List.of(), null);
        TestRepository repository = new TestRepository(initial);
        ProfilePhotoService service = new ProfilePhotoService(repository);

        Profile saved = service.update(userId, file(PNG, "image/png"));
        assertThat(saved.photo().mediaType()).isEqualTo("image/png");
        assertThat(saved.photo().bytes()).containsExactly(PNG);

        assertThatThrownBy(() -> service.update(userId, file(new byte[]{1, 2, 3}, "image/jpeg")))
                .isInstanceOf(RuntimeException.class);
        assertThat(repository.profile.photo().bytes()).containsExactly(PNG);
    }

    private MultipartFile file(byte[] bytes, String contentType) {
        return new MultipartFile() {
            public String getName() { return "file"; }
            public String getOriginalFilename() { return "photo.png"; }
            public String getContentType() { return contentType; }
            public boolean isEmpty() { return bytes.length == 0; }
            public long getSize() { return bytes.length; }
            public byte[] getBytes() { return bytes; }
            public java.io.InputStream getInputStream() throws IOException { return new java.io.ByteArrayInputStream(bytes); }
            public void transferTo(java.io.File destination) throws IOException { throw new UnsupportedOperationException(); }
        };
    }

    private static class TestRepository implements ProfileRepository {
        private Profile profile;

        TestRepository(Profile profile) { this.profile = profile; }
        public Optional<Profile> findByUserId(UUID userId) { return Optional.of(profile); }
        public Profile save(Profile profile) { this.profile = profile; return profile; }
    }
}