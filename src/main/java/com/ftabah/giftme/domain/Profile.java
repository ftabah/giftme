package com.ftabah.giftme.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Perfil pessoal com medidas, interesses e foto opcional. */
public final class Profile {

    private final UUID userId;
    private final String name;
    private final int age;
    private final String height;
    private final String shoeSize;
    private final String waistSize;
    private final String shirtSize;
    private final String interests;
    private final List<CustomSize> customSizes;
    private final ProfilePhoto photo;

    public Profile(UUID userId, String name, int age, String height, String shoeSize,
                   String waistSize, String shirtSize, List<CustomSize> customSizes,
                   ProfilePhoto photo) {
        this(userId, name, age, height, shoeSize, waistSize, shirtSize, "", customSizes, photo);
    }

    public Profile(UUID userId, String name, int age, String height, String shoeSize,
                   String waistSize, String shirtSize, String interests, List<CustomSize> customSizes,
                   ProfilePhoto photo) {
        if (userId == null) {
            throw new IllegalArgumentException("O identificador do usuário é obrigatório");
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("A idade deve estar entre 0 e 150");
        }
        this.userId = userId;
        this.name = required(name, "Nome");
        this.age = age;
        this.height = required(height, "Altura");
        this.shoeSize = required(shoeSize, "Calçado");
        this.waistSize = required(waistSize, "Cintura");
        this.shirtSize = required(shirtSize, "Camisa");
        this.interests = interests == null ? "" : interests.trim();
        this.customSizes = immutableCustomSizes(customSizes);
        this.photo = photo;
    }

    public Profile withCustomSize(CustomSize customSize) {
        if (customSize == null) {
            throw new IllegalArgumentException("A medida personalizada é obrigatória");
        }
        if (customSizes.stream().anyMatch(existing ->
                existing.name().equalsIgnoreCase(customSize.name()))) {
            throw new IllegalArgumentException("Já existe uma medida com esse nome");
        }
        List<CustomSize> updated = new ArrayList<>(customSizes);
        updated.add(customSize);
        return copyWith(updated, photo);
    }

    public Profile withPhoto(ProfilePhoto updatedPhoto) {
        return copyWith(customSizes, updatedPhoto);
    }

    private Profile copyWith(List<CustomSize> sizes, ProfilePhoto updatedPhoto) {
        return new Profile(userId, name, age, height, shoeSize, waistSize, shirtSize, interests, sizes, updatedPhoto);
    }

    private static List<CustomSize> immutableCustomSizes(List<CustomSize> sizes) {
        if (sizes == null) {
            throw new IllegalArgumentException("A lista de medidas personalizadas é obrigatória");
        }
        List<CustomSize> copy = new ArrayList<>();
        for (CustomSize size : sizes) {
            if (size == null || copy.stream().anyMatch(existing ->
                    existing.name().equalsIgnoreCase(size.name()))) {
                throw new IllegalArgumentException("Os nomes das medidas personalizadas devem ser únicos");
            }
            copy.add(size);
        }
        return Collections.unmodifiableList(copy);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " é obrigatório");
        }
        return value.trim();
    }

    public UUID userId() { return userId; }
    public String name() { return name; }
    public int age() { return age; }
    public String height() { return height; }
    public String shoeSize() { return shoeSize; }
    public String waistSize() { return waistSize; }
    public String shirtSize() { return shirtSize; }
    public String interests() { return interests; }
    public List<CustomSize> customSizes() { return customSizes; }
    public ProfilePhoto photo() { return photo; }
}