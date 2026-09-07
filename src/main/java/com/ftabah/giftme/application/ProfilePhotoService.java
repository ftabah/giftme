package com.ftabah.giftme.application;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Profile;
import com.ftabah.giftme.domain.ProfilePhoto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

/** Valida e persiste fotos de perfil recebidas por multipart. */
@Service
public class ProfilePhotoService {

    private final ProfileRepository profiles;

    public ProfilePhotoService(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    public Profile update(UUID userId, MultipartFile file) {
        Profile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
        try {
            ProfilePhoto photo = new ProfilePhoto(file.getBytes(), file.getContentType());
            return profiles.save(profile.withPhoto(photo));
        } catch (IOException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A foto deve ser PNG ou JPEG e ter no máximo 2 MiB", exception);
        }
    }
}