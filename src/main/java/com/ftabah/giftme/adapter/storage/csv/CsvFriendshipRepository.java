package com.ftabah.giftme.adapter.storage.csv;

import com.ftabah.giftme.application.port.FriendshipRepository;
import com.ftabah.giftme.domain.FriendshipRequest;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repositório CSV das solicitações de amizade. */
@Repository
public class CsvFriendshipRepository implements FriendshipRepository {

    private static final String FILE = "friendships.csv";
    private final CsvFileStore store;

    public CsvFriendshipRepository(CsvFileStore store) {
        this.store = store;
    }

    @Override
    public Optional<FriendshipRequest> findById(UUID id) {
        return readAll().stream().filter(request -> request.id().equals(id)).findFirst();
    }

    @Override
    public Optional<FriendshipRequest> findExistingPair(UUID requesterId, UUID recipientId) {
        return readAll().stream().filter(request ->
                request.requesterId().equals(requesterId) && request.recipientId().equals(recipientId)
                        && request.status() != FriendshipRequest.Status.REJECTED).findFirst();
    }

    @Override
    public FriendshipRequest save(FriendshipRequest request) {
        List<FriendshipRequest> requests = readAll().stream()
                .filter(existing -> !existing.id().equals(request.id()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        requests.add(request);
        try {
            store.replace(FILE, requests.stream().map(this::toRow).toList());
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível gravar as amizades", exception);
        }
        return request;
    }

    @Override
    public List<FriendshipRequest> findRelevant(UUID userId) {
        return readAll().stream().filter(request -> request.requesterId().equals(userId)
                || request.recipientId().equals(userId)).toList();
    }

    private List<FriendshipRequest> readAll() {
        try {
            return store.read(FILE).stream().map(this::fromRow).toList();
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Não foi possível ler as amizades", exception);
        }
    }

    private FriendshipRequest fromRow(List<String> row) {
        if (row.size() != 6) {
            throw new IllegalStateException("Registro de amizade inválido");
        }
        FriendshipRequest request = FriendshipRequest.pending(UUID.fromString(row.get(0)), UUID.fromString(row.get(1)),
                UUID.fromString(row.get(2)), Instant.parse(row.get(4)));
        return switch (FriendshipRequest.Status.valueOf(row.get(3))) {
            case PENDING -> request;
            case ACCEPTED -> request.accept(request.recipientId(), Instant.parse(row.get(5)));
            case REJECTED -> request.reject(request.recipientId(), Instant.parse(row.get(5)));
        };
    }

    private List<String> toRow(FriendshipRequest request) {
        return List.of(request.id().toString(), request.requesterId().toString(), request.recipientId().toString(),
                request.status().name(), request.createdAt().toString(), request.updatedAt().toString());
    }
}