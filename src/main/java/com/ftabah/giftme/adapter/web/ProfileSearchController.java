package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.application.port.FriendshipRepository;
import com.ftabah.giftme.domain.FriendshipRequest;
import com.ftabah.giftme.domain.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileSearchController {

    private final AccountRepository accounts;
    private final ProfileRepository profiles;
    private final FriendshipRepository friendships;

    public ProfileSearchController(AccountRepository accounts, ProfileRepository profiles,
                                   FriendshipRepository friendships) {
        this.accounts = accounts;
        this.profiles = profiles;
        this.friendships = friendships;
    }

    @GetMapping("/search")
    public List<ProfileController.ProfileResponse> search(@RequestParam String q, Authentication authentication) {
        if (q == null || q.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query is required");
        }
        List<Profile> matches = new ArrayList<>(profiles.findByNameQuery(q));
        accounts.findByEmail(q.trim().toLowerCase()).flatMap(account -> profiles.findByUserId(account.id()))
                .ifPresent(profile -> {
                    if (matches.stream().noneMatch(existing -> existing.userId().equals(profile.userId()))) {
                        matches.add(profile);
                    }
                });
        UUID requesterId = UUID.fromString(authentication.getName());
        return matches.stream().map(profile -> ProfileController.ProfileResponse.from(profile,
            canSeeMeasurements(requesterId, profile.userId()))).toList();
    }

    @GetMapping("/{userId}")
    public ProfileController.ProfileResponse get(@PathVariable UUID userId, Authentication authentication) {
        UUID requesterId = UUID.fromString(authentication.getName());
        return profiles.findByUserId(userId).map(profile -> ProfileController.ProfileResponse.from(profile,
                        canSeeMeasurements(requesterId, profile.userId())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    private boolean canSeeMeasurements(UUID requesterId, UUID profileId) {
        if (requesterId.equals(profileId)) {
            return true;
        }
        return friendships.findRelevant(requesterId).stream()
                .anyMatch(request -> request.status() == FriendshipRequest.Status.ACCEPTED
                        && (request.requesterId().equals(profileId) || request.recipientId().equals(profileId)));
    }
}