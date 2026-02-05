package com.jpmc.midascore.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ADDED
import java.util.concurrent.CountDownLatch;
// ADDED
import java.util.concurrent.TimeUnit;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    private final List<Float> firstFourAmounts = Collections.synchronizedList(new ArrayList<>());

    private final ObjectMapper objectMapper = new ObjectMapper();


    private final List<Transaction> firstFourTransactions = Collections.synchronizedList(new ArrayList<>());


    private final CountDownLatch firstFourLatch = new CountDownLatch(4);

 
    private volatile Transaction lastTransaction;

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void onMessage(String payload) {
        if (payload == null || payload.isEmpty()) return;

        String s = payload.trim();

        if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1);
            s = s.replace("\\\"", "\"");
        }

        Transaction tx = deserializeToTransaction(s);

        if (tx == null) return;

        lastTransaction = tx;

        float amount = tx.getAmount();

        if (firstFourAmounts.size() < 4) {
            firstFourAmounts.add(amount);

            firstFourTransactions.add(tx);
            firstFourLatch.countDown();
        }

        logger.info("Received transaction: {}", tx);

    }

    private Transaction deserializeToTransaction(String s) {
        try {
            if (s.startsWith("{")) {
                return objectMapper.readValue(s, Transaction.class);
            }

            String[] parts = s.split("\\s*,\\s*");

            if (parts.length < 3) {
                logger.warn("Skipping unrecognized payload(expected 3 parts): {}", s);
                return null;
            }

            long senderId = Long.parseLong(parts[0].trim());
            long recipientId = Long.parseLong(parts[1].trim());
            float amount = Float.parseFloat(parts[2].trim());

            return new Transaction(senderId, recipientId, amount);
        } catch (Exception e) {
            logger.warn("Failed to parse payload into Transaction: {}", s, e);
            return null;
        }
    }

    public List<Float> getFirstFourAmounts() {
        return new ArrayList<>(firstFourAmounts);
    }

    public List<Transaction> getFirstFourTransactions() {
        return new ArrayList<>(firstFourTransactions);
    }

    public Transaction getLastTransaction() {
        return lastTransaction;
    }

    public boolean awaitFirstFour(long timeout, TimeUnit unit) throws InterruptedException {
        return firstFourLatch.await(timeout, unit);
    }
}
