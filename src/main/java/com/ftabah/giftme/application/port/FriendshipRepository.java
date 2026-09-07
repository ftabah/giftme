package com.ftabah.giftme.application.port;

import com.ftabah.giftme.domain.FriendshipRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository {

    Optional<FriendshipRequest> findById(UUID id);

    Optional<FriendshipRequest> findExistingPair(UUID requesterId, UUID recipientId);

    FriendshipRequest save(FriendshipRequest request);

    List<FriendshipRequest> findRelevant(UUID userId);
}