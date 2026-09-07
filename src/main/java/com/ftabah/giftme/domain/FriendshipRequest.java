package com.ftabah.giftme.domain;

import java.time.Instant;
import java.util.UUID;

public final class FriendshipRequest {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    private final UUID id;
    private final UUID requesterId;
    private final UUID recipientId;
    private final Instant createdAt;
    private final Status status;
    private final Instant updatedAt;

    private FriendshipRequest(UUID id, UUID requesterId, UUID recipientId, Instant createdAt,
                              Status status, Instant updatedAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.recipientId = recipientId;
        this.createdAt = createdAt;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public static FriendshipRequest pending(UUID id, UUID requesterId, UUID recipientId, Instant createdAt) {
        if (id == null || requesterId == null || recipientId == null || createdAt == null) {
            throw new IllegalArgumentException("Friendship request fields are required");
        }
        if (requesterId.equals(recipientId)) {
            throw new IllegalArgumentException("A user cannot request themself");
        }
        return new FriendshipRequest(id, requesterId, recipientId, createdAt, Status.PENDING, createdAt);
    }

    public FriendshipRequest accept(UUID actorId, Instant changedAt) {
        return decide(actorId, changedAt, Status.ACCEPTED);
    }

    public FriendshipRequest reject(UUID actorId, Instant changedAt) {
        return decide(actorId, changedAt, Status.REJECTED);
    }

    private FriendshipRequest decide(UUID actorId, Instant changedAt, Status nextStatus) {
        if (!recipientId.equals(actorId)) {
            throw new SecurityException("Only the recipient can decide");
        }
        if (status != Status.PENDING) {
            throw new IllegalStateException("Only pending requests can be decided");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("Decision time is required");
        }
        return new FriendshipRequest(id, requesterId, recipientId, createdAt, nextStatus, changedAt);
    }

    public UUID id() { return id; }
    public UUID requesterId() { return requesterId; }
    public UUID recipientId() { return recipientId; }
    public Instant createdAt() { return createdAt; }
    public Status status() { return status; }
    public Instant updatedAt() { return updatedAt; }
}