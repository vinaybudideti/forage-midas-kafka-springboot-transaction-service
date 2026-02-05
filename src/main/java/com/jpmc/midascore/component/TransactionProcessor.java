package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    // Incentive API client
    @Autowired
    private IncentiveClient incentiveClient;

    public TransactionProcessor(UserRepository userRepository,
                                TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    public void process(Transaction tx) {
        if (tx == null || tx.getAmount() <= 0) {
            return;
        }

        UserRecord sender = userRepository.findById(tx.getSenderId());
        UserRecord recipient = userRepository.findById(tx.getRecipientId());

        // invalid if user IDs don't exist
        if (sender == null || recipient == null) {
            return;
        }

        // invalid if insufficient funds
        if (sender.getBalance() < tx.getAmount()) {
            return;
        }

        //  fetch incentive AFTER validation (valid tx only)
        float incentiveAmount = 0.0f;
        if (incentiveClient != null) {
            incentiveAmount = incentiveClient.fetchIncentiveAmount(tx);
        }

        // apply balance changes
        sender.setBalance(sender.getBalance() - tx.getAmount());
        recipient.setBalance(recipient.getBalance() + tx.getAmount());

        //  ADD (Task 4): credit incentive ONLY to recipient (do not subtract from sender)
        recipient.setBalance(recipient.getBalance() + incentiveAmount);

        // persist updated users + transaction record
        userRepository.save(sender);
        userRepository.save(recipient);


        TransactionRecord record = new TransactionRecord(sender, recipient, tx.getAmount());

        //store incentive alongside amount
        record.setIncentive(incentiveAmount);

        transactionRecordRepository.save(record);

        log.info("Processed tx: senderId={}, recipientId={}, amount={}, senderBal={}, recipientBal={}",
                sender.getId(), recipient.getId(), tx.getAmount(),
                sender.getBalance(), recipient.getBalance());
    }
}