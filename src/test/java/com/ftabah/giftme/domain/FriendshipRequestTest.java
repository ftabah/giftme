package com.ftabah.giftme.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FriendshipRequestTest {

    private final UUID requester = UUID.randomUUID();
    private final UUID recipient = UUID.randomUUID();
    private final Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createsPendingRequestAndAllowsRecipientToAcceptOrReject() {
        FriendshipRequest pending = FriendshipRequest.pending(UUID.randomUUID(), requester, recipient, createdAt);

        FriendshipRequest accepted = pending.accept(recipient, createdAt.plusSeconds(1));
        FriendshipRequest rejected = pending.reject(recipient, createdAt.plusSeconds(2));

        assertThat(pending.status()).isEqualTo(FriendshipRequest.Status.PENDING);
        assertThat(accepted.status()).isEqualTo(FriendshipRequest.Status.ACCEPTED);
        assertThat(rejected.status()).isEqualTo(FriendshipRequest.Status.REJECTED);
    }

    @Test
    void rejectsSelfRequest() {
        assertThatThrownBy(() -> FriendshipRequest.pending(UUID.randomUUID(), requester, requester, createdAt))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsDecisionByNonRecipientAndPreservesPendingState() {
        FriendshipRequest pending = FriendshipRequest.pending(UUID.randomUUID(), requester, recipient, createdAt);

        assertThatThrownBy(() -> pending.accept(requester, createdAt.plusSeconds(1)))
                .isInstanceOf(SecurityException.class);
        assertThat(pending.status()).isEqualTo(FriendshipRequest.Status.PENDING);
    }

    @Test
    void rejectsRepeatedDecisionAndPreservesAcceptedState() {
        FriendshipRequest accepted = FriendshipRequest.pending(UUID.randomUUID(), requester, recipient, createdAt)
                .accept(recipient, createdAt.plusSeconds(1));

        assertThatThrownBy(() -> accepted.reject(recipient, createdAt.plusSeconds(2)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(accepted.status()).isEqualTo(FriendshipRequest.Status.ACCEPTED);
    }
}