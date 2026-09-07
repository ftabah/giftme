package com.ftabah.giftme.adapter.storage.csv;

import com.ftabah.giftme.application.port.ProfileRepository;
import com.ftabah.giftme.domain.CustomSize;
import com.ftabah.giftme.domain.Profile;
import com.ftabah.giftme.domain.ProfilePhoto;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositório de perfis e tamanhos personalizados persistido em CSV. */
@Repository
public class CsvProfileRepository implements ProfileRepository {

    private static final String PROFILE_FILE = "profiles.csv";
    private static final String CUSTOM_SIZE_FILE = "custom_sizes.csv";
    private final CsvFileStore store;

    public CsvProfileRepository(CsvFileStore store) {
        this.store = store;
    }

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return readAll().stream().filter(profile -> profile.userId().equals(userId)).findFirst();
    }

    @Override
    public List<Profile> findByNameQuery(String query) {
        String normalized = query.trim().toLowerCase();
        return readAll().stream().filter(profile -> profile.name().toLowerCase().contains(normalized)).toList();
    }

    @Override
    public Profile save(Profile profile) {
        List<Profile> profiles = readAll().stream()
                .filter(existing -> !existing.userId().equals(profile.userId()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        profiles.add(profile);
        writeProfiles(profiles);
        writeCustomSizes(profiles);
        return profile;
    }

    private List<Profile> readAll() {
        try {
            List<List<String>> customRows = store.read(CUSTOM_SIZE_FILE);
            return store.read(PROFILE_FILE).stream().map(row -> fromRow(row, customRows)).toList();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Não foi possível ler os perfis", exception);
        }
    }

    private Profile fromRow(List<String> row, List<List<String>> customRows) {
        if (row.size() != 9 && row.size() != 10) {
            throw new IllegalStateException("Registro de perfil inválido");
        }
        UUID userId = UUID.fromString(row.get(0));
        List<CustomSize> sizes = customRows.stream()
                .filter(custom -> custom.size() == 3 && custom.get(0).equals(userId.toString()))
                .map(custom -> new CustomSize(custom.get(1), custom.get(2))).toList();
        String interests = row.size() == 10 ? row.get(7) : "";
        String mediaType = row.size() == 10 ? row.get(8) : row.get(7);
        String photoData = row.size() == 10 ? row.get(9) : row.get(8);
        ProfilePhoto storedPhoto = photoData.isEmpty() ? null :
            new ProfilePhoto(Base64.getDecoder().decode(photoData), mediaType);
        return new Profile(userId, row.get(1), Integer.parseInt(row.get(2)), row.get(3),
            row.get(4), row.get(5), row.get(6), interests, sizes, storedPhoto);
    }

    private void writeProfiles(List<Profile> profiles) {
        try {
            store.replace(PROFILE_FILE, profiles.stream().map(profile -> List.of(
                    profile.userId().toString(), profile.name(), Integer.toString(profile.age()), profile.height(),
                    profile.shoeSize(), profile.waistSize(), profile.shirtSize(), profile.interests(),
                    profile.photo() == null ? "" : profile.photo().mediaType(),
                    profile.photo() == null ? "" : Base64.getEncoder().encodeToString(profile.photo().bytes())
            )).toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar os perfis", exception);
        }
    }

    private void writeCustomSizes(List<Profile> profiles) {
        try {
            store.replace(CUSTOM_SIZE_FILE, profiles.stream().flatMap(profile -> profile.customSizes().stream()
                    .map(size -> List.of(profile.userId().toString(), size.name(), size.value()))).toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar as medidas personalizadas", exception);
        }
    }
}