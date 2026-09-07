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

@Service
public class ProfilePhotoService {

    private final ProfileRepository profiles;

    public ProfilePhotoService(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    public Profile update(UUID userId, MultipartFile file) {
        Profile profile = profiles.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        try {
            ProfilePhoto photo = new ProfilePhoto(file.getBytes(), file.getContentType());
            return profiles.save(profile.withPhoto(photo));
        } catch (IOException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo must be a PNG up to 2 MiB", exception);
        }
    }
}