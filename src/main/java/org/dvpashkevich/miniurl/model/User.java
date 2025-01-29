package org.dvpashkevich.miniurl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private final String id;
    private final List<String> urls = new ArrayList<>();

    public User(String id) {
        this.id = id;
    }

    public void addUrl(String code) {
        urls.add(code);
    }

    public void removeUrl(String code) {
        urls.remove(code);
    }

    public List<String> getUrls() {
        return Collections.unmodifiableList(urls);
    }
}