package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class IncentiveClient {

    private static final Logger logger = LoggerFactory.getLogger(IncentiveClient.class);

    private final RestTemplate restTemplate;
    private final String incentiveUrl;

    public IncentiveClient(RestTemplate restTemplate,
                           @Value("${general.incentive-url}") String incentiveUrl) {
        this.restTemplate = restTemplate;
        this.incentiveUrl = incentiveUrl;
    }

    public float fetchIncentiveAmount(Transaction tx) {
        try {
            Incentive resp = restTemplate.postForObject(incentiveUrl, tx, Incentive.class);
            float amt = (resp == null) ? 0.0f : resp.getAmount();
            if (amt < 0.0f) amt = 0.0f; // per spec: amount >= 0
            return amt;
        } catch (Exception e) {
            logger.warn("Incentive API call failed; defaulting incentive=0. tx={}", tx, e);
            return 0.0f;
        }
    }
}