package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.FriendshipRepository;
import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Account;
import com.ftabah.giftme.domain.FriendshipRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Expõe envio e consulta das solicitações de amizade do usuário autenticado. */
@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final AccountRepository accounts;
    private final FriendshipRepository friendships;
    private final ProfileRepository profiles;

    public FriendshipController(AccountRepository accounts, FriendshipRepository friendships,
                                ProfileRepository profiles) {
        this.accounts = accounts;
        this.friendships = friendships;
        this.profiles = profiles;
    }

    @PostMapping("/requests")
    public FriendshipResponse send(Authentication authentication, @Valid @RequestBody FriendshipRequestBody body) {
        UUID requesterId = userId(authentication);
        if (requesterId.equals(body.recipientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível enviar solicitação para si mesmo");
        }
        if (accounts.findById(body.recipientId()).isEmpty()
                || friendships.findExistingPair(requesterId, body.recipientId()).isPresent()
                || friendships.findExistingPair(body.recipientId(), requesterId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicitação de amizade inválida");
        }
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), requesterId,
                body.recipientId(), Instant.now());
        return FriendshipResponse.from(friendships.save(request), accounts, profiles, requesterId);
    }

    @GetMapping("/requests")
    public List<FriendshipResponse> list(Authentication authentication) {
        UUID currentUserId = userId(authentication);
        return friendships.findRelevant(currentUserId).stream()
            .map(request -> FriendshipResponse.from(request, accounts, profiles, currentUserId)).toList();
    }

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    public record FriendshipRequestBody(@NotNull UUID recipientId) { }

    public record FriendshipResponse(UUID id, UUID requesterId, UUID recipientId,
                         String status, String counterpartEmail,
                         ProfileController.ProfileResponse counterpartProfile) {
        static FriendshipResponse from(FriendshipRequest request, AccountRepository accounts,
                          ProfileRepository profiles, UUID currentUserId) {
            UUID counterpart = request.requesterId().equals(currentUserId)
                    ? request.recipientId() : request.requesterId();
            String email = accounts.findById(counterpart).map(Account::email).orElse("");
            ProfileController.ProfileResponse profile = request.status() == FriendshipRequest.Status.ACCEPTED
                ? profiles.findByUserId(counterpart).map(ProfileController.ProfileResponse::from).orElse(null)
                : null;
            return new FriendshipResponse(request.id(), request.requesterId(), request.recipientId(),
                request.status().name(), email, profile);
        }
    }
}