package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfilePhotoServiceTest {

    @Test
    void normalizesPngTo200SquareAndRejectsInvalidPhotoWithoutReplacingPreviousPhoto() throws Exception {
        UUID userId = UUID.randomUUID();
        Profile initial = new Profile(userId, "Ana", 30, "1.70 m", "38", "70", "M", List.of(), null);
        TestRepository repository = new TestRepository(initial);
        ProfilePhotoService service = new ProfilePhotoService(repository);

        Profile saved = service.update(userId, file(imageBytes("png", 400, 100), "image/png"));
        assertThat(saved.photo().mediaType()).isEqualTo("image/png");
        BufferedImage storedImage = ImageIO.read(new ByteArrayInputStream(saved.photo().bytes()));
        assertThat(storedImage.getWidth()).isEqualTo(200);
        assertThat(storedImage.getHeight()).isEqualTo(200);

        assertThatThrownBy(() -> service.update(userId, file(new byte[]{1, 2, 3}, "image/jpeg")))
                .isInstanceOf(RuntimeException.class);
        assertThat(repository.profile.photo().bytes()).containsExactly(saved.photo().bytes());
    }

    @Test
    void normalizesJpegTo200SquareAndPreservesItsMediaType() throws Exception {
        UUID userId = UUID.randomUUID();
        Profile initial = new Profile(userId, "Ana", 30, "1.70 m", "38", "70", "M", List.of(), null);
        ProfilePhotoService service = new ProfilePhotoService(new TestRepository(initial));

        Profile saved = service.update(userId, file(imageBytes("jpeg", 80, 300), "image/jpeg"));

        BufferedImage storedImage = ImageIO.read(new ByteArrayInputStream(saved.photo().bytes()));
        assertThat(saved.photo().mediaType()).isEqualTo("image/jpeg");
        assertThat(storedImage.getWidth()).isEqualTo(200);
        assertThat(storedImage.getHeight()).isEqualTo(200);
    }

    private byte[] imageBytes(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height,
                "png".equals(format) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
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
        public List<Profile> findByNameQuery(String query) { return List.of(); }
        public Profile save(Profile profile) { this.profile = profile; return profile; }
    }
}