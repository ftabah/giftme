package com.ftabah.giftme.application.port;

import com.ftabah.giftme.domain.Profile;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {

    Optional<Profile> findByUserId(UUID userId);

    Profile save(Profile profile);
}