package com.ftabah.giftme.application.port;

import com.ftabah.giftme.domain.Profile;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ProfileRepository {

    Optional<Profile> findByUserId(UUID userId);

    List<Profile> findByNameQuery(String query);

    Profile save(Profile profile);
}