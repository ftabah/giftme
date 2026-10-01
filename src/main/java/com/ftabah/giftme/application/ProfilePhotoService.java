package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Profile;
import com.ftabah.giftme.domain.ProfilePhoto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.UUID;

/** Valida e persiste fotos de perfil recebidas por multipart. */
@Service
public class ProfilePhotoService {

    private static final int PHOTO_SIZE = 200;
    private final ProfileRepository profiles;

    public ProfilePhotoService(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    public Profile update(UUID userId, MultipartFile file) {
        Profile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
        try {
            byte[] bytes = file.getBytes();
            String mediaType = file.getContentType();
            if (file.isEmpty() || bytes.length > ProfilePhoto.MAX_BYTES
                    || !("image/png".equals(mediaType) || "image/jpeg".equals(mediaType))) {
                throw new IllegalArgumentException("Formato ou tamanho de foto inválido");
            }
            BufferedImage source = ImageIO.read(new ByteArrayInputStream(bytes));
            if (source == null) {
                throw new IllegalArgumentException("A foto não pôde ser decodificada");
            }
            ProfilePhoto photo = new ProfilePhoto(normalize(source, mediaType), mediaType);
            return profiles.save(profile.withPhoto(photo));
        } catch (IOException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A foto deve ser PNG ou JPEG e ter no máximo 2 MiB", exception);
        }
    }

    private static byte[] normalize(BufferedImage source, String mediaType) throws IOException {
        int sourceSide = Math.min(source.getWidth(), source.getHeight());
        int sourceX = (source.getWidth() - sourceSide) / 2;
        int sourceY = (source.getHeight() - sourceSide) / 2;
        int imageType = "image/png".equals(mediaType) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage normalized = new BufferedImage(PHOTO_SIZE, PHOTO_SIZE, imageType);
        Graphics2D graphics = normalized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.drawImage(source, 0, 0, PHOTO_SIZE, PHOTO_SIZE,
                sourceX, sourceY, sourceX + sourceSide, sourceY + sourceSide, null);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String format = "image/png".equals(mediaType) ? "png" : "jpeg";
        if (!ImageIO.write(normalized, format, output)) {
            throw new IllegalArgumentException("Formato de imagem não suportado");
        }
        return output.toByteArray();
    }
}