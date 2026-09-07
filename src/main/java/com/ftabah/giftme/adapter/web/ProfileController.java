package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.application.ProfilePhotoService;
import com.ftabah.giftme.domain.CustomSize;
import com.ftabah.giftme.domain.Profile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles/me")
public class ProfileController {

    private final ProfileRepository profiles;
    private final ProfilePhotoService photos;

    public ProfileController(ProfileRepository profiles, ProfilePhotoService photos) {
        this.profiles = profiles;
        this.photos = photos;
    }

    @GetMapping
    public ProfileResponse get(Authentication authentication) {
        return profiles.findByUserId(userId(authentication)).map(ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @PutMapping
    public ProfileResponse update(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        UUID userId = userId(authentication);
        Profile existing = profiles.findByUserId(userId).orElse(null);
        Profile profile = new Profile(userId, request.name(), request.age(), request.height(), request.shoeSize(),
                request.waistSize(), request.shirtSize(), request.customSizes().stream()
                .map(size -> new CustomSize(size.name(), size.value())).toList(), existing == null ? null : existing.photo());
        return ProfileResponse.from(profiles.save(profile));
    }

    @PutMapping(value = "/photo", consumes = "multipart/form-data")
    public ProfileResponse updatePhoto(Authentication authentication, @RequestPart("file") MultipartFile file) {
        return ProfileResponse.from(photos.update(userId(authentication), file));
    }

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    public record ProfileRequest(
            @NotBlank String name,
            @Min(0) @Max(150) int age,
            @NotBlank String height,
            @NotBlank String shoeSize,
            @NotBlank String waistSize,
            @NotBlank String shirtSize,
            List<CustomSizeRequest> customSizes) {
        public ProfileRequest {
            customSizes = customSizes == null ? List.of() : customSizes;
        }
    }

    public record CustomSizeRequest(@NotBlank String name, @NotBlank String value) { }

    public record ProfileResponse(UUID userId, String name, int age, String height, String shoeSize,
                                  String waistSize, String shirtSize, List<CustomSize> customSizes,
                                  String photoMediaType, String photoBase64) {
        static ProfileResponse from(Profile profile) {
            return new ProfileResponse(profile.userId(), profile.name(), profile.age(), profile.height(),
                    profile.shoeSize(), profile.waistSize(), profile.shirtSize(), profile.customSizes(),
                    profile.photo() == null ? null : profile.photo().mediaType(),
                    profile.photo() == null ? null : Base64.getEncoder().encodeToString(profile.photo().bytes()));
        }
    }
}