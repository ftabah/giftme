package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.FriendshipRepository;
import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.FriendshipRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

/** Controla aceite e rejeição de solicitações pelo destinatário. */
@RestController
@RequestMapping("/api/friendships/requests")
public class FriendshipDecisionController {

    private final FriendshipRepository friendships;
    private final AccountRepository accounts;
    private final ProfileRepository profiles;

    public FriendshipDecisionController(FriendshipRepository friendships, AccountRepository accounts,
                                        ProfileRepository profiles) {
        this.friendships = friendships;
        this.accounts = accounts;
        this.profiles = profiles;
    }

    @PostMapping("/{requestId}/decision")
    public FriendshipController.FriendshipResponse decide(Authentication authentication,
                                                            @PathVariable UUID requestId,
                                                            @Valid @RequestBody DecisionBody body) {
        FriendshipRequest request = friendships.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação de amizade não encontrada"));
        UUID recipientId = UUID.fromString(authentication.getName());
        try {
            FriendshipRequest decided = switch (body.decision().trim().toUpperCase()) {
                case "ACCEPT", "ACCEPTED" -> request.accept(recipientId, Instant.now());
                case "REJECT", "REJECTED" -> request.reject(recipientId, Instant.now());
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A decisão deve ser ACCEPT ou REJECT");
            };
                return FriendshipController.FriendshipResponse.from(friendships.save(decided), accounts, profiles,
                    recipientId);
        } catch (SecurityException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Somente o destinatário pode decidir", exception);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A solicitação de amizade não está mais pendente", exception);
        }
    }

    public record DecisionBody(@NotBlank String decision) { }
}