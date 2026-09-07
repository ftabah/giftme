package com.ftabah.giftme.adapter.web;

import com.ftabah.giftme.application.port.AccountRepository;
import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/profiles")
public class ProfileSearchController {

    private final AccountRepository accounts;
    private final ProfileRepository profiles;

    public ProfileSearchController(AccountRepository accounts, ProfileRepository profiles) {
        this.accounts = accounts;
        this.profiles = profiles;
    }

    @GetMapping("/search")
    public List<ProfileController.ProfileResponse> search(@RequestParam String q) {
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
        return matches.stream().map(ProfileController.ProfileResponse::from).toList();
    }

    @GetMapping("/{userId}")
    public ProfileController.ProfileResponse get(@PathVariable UUID userId) {
        return profiles.findByUserId(userId).map(ProfileController.ProfileResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}