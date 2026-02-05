package com.jpmc.midascore.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transaction_records")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserRecord sender;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserRecord recipient;

    @Column(nullable = false)
    private float amount;

    @Column(nullable = false)
    private Instant createdAt;

    public TransactionRecord() {}

    public TransactionRecord(UserRecord sender, UserRecord recipient, float amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public UserRecord getSender() { return sender; }
    public UserRecord getRecipient() { return recipient; }
    public float getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }

    public void setSender(UserRecord sender) { this.sender = sender; }
    public void setRecipient(UserRecord recipient) { this.recipient = recipient; }
    public void setAmount(float amount) { this.amount = amount; }

    //  incentive column
    @Column(nullable = false)
    private float incentive;

    // ADD getter/setter
    public float getIncentive() { return incentive; }
    public void setIncentive(float incentive) { this.incentive = incentive; }
}