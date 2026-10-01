package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Account;
import com.ftabah.giftme.domain.CustomSize;
import com.ftabah.giftme.domain.FriendshipRequest;
import com.ftabah.giftme.domain.Profile;
import com.ftabah.giftme.domain.ProfilePhoto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendshipControllerTest {

    @Mock
    private AccountRepository accounts;
        @Mock
        private ProfileRepository profiles;

    @Test
    void returnsRecipientAsCounterpartWhenRequesterViewsTheRequest() {
        UUID requesterId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), requesterId, recipientId,
                Instant.now());
        when(accounts.findById(recipientId)).thenReturn(Optional.of(
                new Account(recipientId, "recipient@example.com", "hash")));

        FriendshipController.FriendshipResponse response =
                FriendshipController.FriendshipResponse.from(request, accounts, profiles, requesterId);

        assertThat(response.counterpartEmail()).isEqualTo("recipient@example.com");
    }

    @Test
    void returnsRequesterAsCounterpartWhenRecipientViewsTheRequest() {
        UUID requesterId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), requesterId, recipientId,
                Instant.now());
        when(accounts.findById(requesterId)).thenReturn(Optional.of(
                new Account(requesterId, "requester@example.com", "hash")));

        FriendshipController.FriendshipResponse response =
                FriendshipController.FriendshipResponse.from(request, accounts, profiles, recipientId);

        assertThat(response.counterpartEmail()).isEqualTo("requester@example.com");
    }

    @Test
    void includesAcceptedCounterpartMeasurementsAndPhoto() {
        UUID requesterId = UUID.randomUUID();
        UUID recipientId = UUID.randomUUID();
        Instant createdAt = Instant.now();
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), requesterId, recipientId,
                createdAt).accept(recipientId, createdAt.plusSeconds(1));
        byte[] photoBytes = {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a};
        Profile counterpart = new Profile(recipientId, "Renata", 34, "1.68 m", "38", "72 cm", "M",
                "Caminhada", List.of(new CustomSize("Anel", "16")), new ProfilePhoto(photoBytes, "image/png"));
        when(accounts.findById(recipientId)).thenReturn(Optional.of(
                new Account(recipientId, "renata@example.com", "hash")));
        when(profiles.findByUserId(recipientId)).thenReturn(Optional.of(counterpart));

        FriendshipController.FriendshipResponse response =
                FriendshipController.FriendshipResponse.from(request, accounts, profiles, requesterId);

        assertThat(response.counterpartProfile().name()).isEqualTo("Renata");
        assertThat(response.counterpartProfile().age()).isEqualTo(34);
        assertThat(response.counterpartProfile().height()).isEqualTo("1.68 m");
        assertThat(response.counterpartProfile().shoeSize()).isEqualTo("38");
        assertThat(response.counterpartProfile().waistSize()).isEqualTo("72 cm");
        assertThat(response.counterpartProfile().shirtSize()).isEqualTo("M");
        assertThat(response.counterpartProfile().customSizes()).containsExactly(new CustomSize("Anel", "16"));
        assertThat(response.counterpartProfile().photoMediaType()).isEqualTo("image/png");
        assertThat(response.counterpartProfile().photoBase64()).isEqualTo(Base64.getEncoder().encodeToString(photoBytes));
    }
}