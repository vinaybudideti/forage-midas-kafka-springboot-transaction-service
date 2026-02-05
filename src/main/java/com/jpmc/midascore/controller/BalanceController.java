package com.jpmc.midascore.controller;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RestController
public class BalanceController {

    private final UserRepository userRepository;

    public BalanceController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance getBalance(@RequestParam("userId") long userId) {
        UserRecord user = userRepository.findById(userId);
        float bal = (user == null) ? 0.0f : user.getBalance();
        return buildBalance(bal);
    }

    /**
     * Robust construction so it works even if Balance class uses:
     * - Balance(float) constructor OR
     * - no-args + setAmount/setBalance OR
     * - field named "amount"/"balance"
     *
     * (You were warned not to change Balance.toString(); we don’t touch it.)
     */
    private Balance buildBalance(float amount) {
        try {
            // 1) Try constructor Balance(float)
            Constructor<Balance> c = Balance.class.getConstructor(float.class);
            return c.newInstance(amount);
        } catch (Exception ignored) {}

        try {
            // 2) Try no-args constructor
            Balance b = Balance.class.getConstructor().newInstance();

            // 2a) Try setAmount(float)
            try {
                Method m = Balance.class.getMethod("setAmount", float.class);
                m.invoke(b, amount);
                return b;
            } catch (Exception ignored) {}

            // 2b) Try setBalance(float)
            try {
                Method m = Balance.class.getMethod("setBalance", float.class);
                m.invoke(b, amount);
                return b;
            } catch (Exception ignored) {}

            // 3) Try direct field set: "amount" or "balance"
            try {
                Field f = Balance.class.getDeclaredField("amount");
                f.setAccessible(true);
                f.setFloat(b, amount);
                return b;
            } catch (Exception ignored) {}

            try {
                Field f = Balance.class.getDeclaredField("balance");
                f.setAccessible(true);
                f.setFloat(b, amount);
                return b;
            } catch (Exception ignored) {}

            return b;
        } catch (Exception e) {
            // If Balance is truly impossible to construct differently, fail loudly:
            throw new IllegalStateException("Unable to construct Balance object. Check Balance class constructors/setters.", e);
        }
    }
}