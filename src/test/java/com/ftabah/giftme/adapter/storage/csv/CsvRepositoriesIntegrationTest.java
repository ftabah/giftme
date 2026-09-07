package com.ftabah.giftme.adapter.storage.csv;

import com.ftabah.giftme.domain.Account;
import com.ftabah.giftme.domain.CustomSize;
import com.ftabah.giftme.domain.FriendshipRequest;
import com.ftabah.giftme.domain.Profile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvRepositoriesIntegrationTest {

    @TempDir
    Path dataDirectory;

    @Test
    void savesAndReadsAccountsProfilesAndFriendships() {
        CsvFileStore store = new CsvFileStore(dataDirectory);
        CsvAccountRepository accounts = new CsvAccountRepository(store);
        CsvProfileRepository profiles = new CsvProfileRepository(store);
        CsvFriendshipRepository friendships = new CsvFriendshipRepository(store);
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        accounts.save(new Account(first, "ANA@EXAMPLE.COM", "hashed"));
        profiles.save(new Profile(first, "Ana", 30, "1.70 m", "38", "70", "M",
                List.of(new CustomSize("Glove", "L")), null));
        FriendshipRequest request = FriendshipRequest.pending(UUID.randomUUID(), first, second, Instant.now());
        friendships.save(request);

        assertThat(accounts.findByEmail("ana@example.com")).isPresent();
        assertThat(profiles.findByUserId(first)).get().extracting(Profile::name).isEqualTo("Ana");
        assertThat(profiles.findByUserId(first)).get().extracting(Profile::customSizes)
                .asList().isNotEmpty();
        assertThat(friendships.findRelevant(first)).hasSize(1);
        assertThat(friendships.findRelevant(first).get(0).id()).isEqualTo(request.id());
        assertThat(friendships.findRelevant(first).get(0).status()).isEqualTo(FriendshipRequest.Status.PENDING);
        assertThat(new CsvAccountRepository(new CsvFileStore(dataDirectory.resolve("empty")))
                .findById(first)).isEmpty();
    }
}