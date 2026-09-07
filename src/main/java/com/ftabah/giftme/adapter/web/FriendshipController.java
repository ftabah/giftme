package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.FriendshipRepository;
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

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final AccountRepository accounts;
    private final FriendshipRepository friendships;

    public FriendshipController(AccountRepository accounts, FriendshipRepository friendships) {
        this.accounts = accounts;
        this.friendships = friendships;
    }

    @PostMapping("/requests")
    public FriendshipResponse send(Authentication authentication, @Valid @RequestBody FriendshipRequestBody body) {
        UUID requesterId = userId(authentication);
        if (requesterId.equals(body.recipientId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request yourself");
        }
        if (accounts.findById(body.recipientId()).isEmpty()
                || friendships.findExistingPair(requesterId, body.recipientId()).isPresent()
                || friendships.findExistingPair(body.recipientId(), requesterId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid friendship request");
        }
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), requesterId,
                body.recipientId(), Instant.now());
        return FriendshipResponse.from(friendships.save(request), accounts);
    }

    @GetMapping("/requests")
    public List<FriendshipResponse> list(Authentication authentication) {
        return friendships.findRelevant(userId(authentication)).stream()
                .map(request -> FriendshipResponse.from(request, accounts)).toList();
    }

    private static UUID userId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    public record FriendshipRequestBody(@NotNull UUID recipientId) { }

    public record FriendshipResponse(UUID id, UUID requesterId, UUID recipientId,
                                     String status, String counterpartEmail) {
        static FriendshipResponse from(FriendshipRequest request, AccountRepository accounts) {
            UUID counterpart = request.requesterId();
            String email = accounts.findById(counterpart).map(Account::email).orElse("");
            return new FriendshipResponse(request.id(), request.requesterId(), request.recipientId(),
                    request.status().name(), email);
        }
    }
}