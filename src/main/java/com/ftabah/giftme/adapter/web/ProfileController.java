package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.ProfileRepository;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles/me")
public class ProfileController {

    private final ProfileRepository profiles;

    public ProfileController(ProfileRepository profiles) {
        this.profiles = profiles;
    }

    @GetMapping
    public ProfileResponse get(Authentication authentication) {
        return profiles.findByUserId(userId(authentication)).map(ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @PutMapping
    public ProfileResponse update(Authentication authentication, @Valid @RequestBody ProfileRequest request) {
        UUID userId = userId(authentication);
        Profile profile = new Profile(userId, request.name(), request.age(), request.height(), request.shoeSize(),
                request.waistSize(), request.shirtSize(), request.customSizes().stream()
                .map(size -> new CustomSize(size.name(), size.value())).toList(), null);
        return ProfileResponse.from(profiles.save(profile));
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
                                  String waistSize, String shirtSize, List<CustomSize> customSizes) {
        static ProfileResponse from(Profile profile) {
            return new ProfileResponse(profile.userId(), profile.name(), profile.age(), profile.height(),
                    profile.shoeSize(), profile.waistSize(), profile.shirtSize(), profile.customSizes());
        }
    }
}