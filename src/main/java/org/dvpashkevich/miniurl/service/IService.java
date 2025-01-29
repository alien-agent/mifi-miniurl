package org.dvpashkevich.miniurl.service;

import java.util.List;

public interface IService {
    String register();

    List<String> list(String userId);

    String shorten(String url, String userId, int ttlSecs, int maxVisits);

    void edit(String code, String userId, int ttlSecs, int maxVisits);

    void remove(String code, String userId);

    String decode(String code);

    boolean isActive(String code);

    boolean isOwnedBy(String code, String userId);

    boolean userExists(String id);

}
