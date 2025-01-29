package org.dvpashkevich.miniurl.service;

import org.dvpashkevich.miniurl.model.UrlEntry;
import org.dvpashkevich.miniurl.model.User;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Service implements IService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, UrlEntry> urlMap = new ConcurrentHashMap<>();
    private final Random random = new Random();
    private final int globalTtlSecs;
    private BigInteger counter = new BigInteger("18921839");


    public Service() {
        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file", e);
        }

        this.globalTtlSecs = Integer.parseInt(config.getProperty("global.expiration.time", "3600"));

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::clearExpiredUrls, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public String register() {
        String userId = UUID.randomUUID().toString();
        users.put(userId, new User(userId));
        return userId;
    }

    @Override
    public List<String> list(String userId) {
        if (!userExists(userId)) {
            throw new IllegalArgumentException("User does not exist.");
        }

        var urls = users.get(userId).getUrls();
        return urls.stream().filter(this::isActive).toList();
    }

    @Override
    public String shorten(String url, String userId, int ttlSecs, int maxVisits) {
        if (!userExists(userId)) {
            throw new IllegalArgumentException("User does not exist.");
        }

        String code = generateCode();
        long expiryTime = System.currentTimeMillis() + Math.min(ttlSecs, globalTtlSecs) * 1000L;
        UrlEntry entry = new UrlEntry(url, userId, expiryTime, maxVisits);
        urlMap.put(code, entry);
        users.get(userId).addUrl(code);

        return code;
    }

    @Override
    public void edit(String code, String userId, int ttlSecs, int maxVisits) {
        if (!isOwnedBy(code, userId)) {
            throw new IllegalArgumentException("User does not own this URL.");
        }

        UrlEntry entry = urlMap.get(code);
        entry.setExpiryTime(System.currentTimeMillis() + Math.min(ttlSecs, globalTtlSecs) * 1000L);
        entry.setMaxVisits(maxVisits);
    }

    @Override
    public void remove(String code, String userId) {
        if (!isOwnedBy(code, userId)) {
            throw new IllegalArgumentException("User does not own this URL.");
        }

        urlMap.remove(code);
        users.get(userId).removeUrl(code);
    }

    @Override
    public String decode(String code) {
        UrlEntry entry = urlMap.get(code);
        if (entry == null || !isActive(code)) {
            throw new IllegalArgumentException("Invalid or expired code.");
        }

        entry.incrementVisit();
        return entry.getUrl();
    }

    @Override
    public boolean isActive(String code) {
        UrlEntry entry = urlMap.get(code);
        return entry != null && entry.getExpiryTime() > System.currentTimeMillis() && entry.getVisitCount() < entry.getMaxVisits();
    }

    @Override
    public boolean isOwnedBy(String code, String userId) {
        UrlEntry entry = urlMap.get(code);
        return entry != null && entry.getOwnerId().equals(userId);
    }

    @Override
    public boolean userExists(String id) {
        return users.containsKey(id);
    }

    private String generateCode() {
        var intCode = counter.add(new BigInteger(Integer.toString(random.nextInt(50))));
        counter = intCode;
        return intCode.toString(16);
    }

    private void clearExpiredUrls() {
        long currentTime = System.currentTimeMillis();
        urlMap.entrySet().removeIf(entry -> entry.getValue().getExpiryTime() <= currentTime);
    }
}
