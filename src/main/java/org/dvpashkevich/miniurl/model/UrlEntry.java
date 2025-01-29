package org.dvpashkevich.miniurl.model;

public class UrlEntry {
    private final String url;
    private final String ownerId;
    private long expiryTime;
    private int maxVisits;
    private int visitCount;

    public UrlEntry(String url, String ownerId, long expiryTime, int maxVisits) {
        this.url = url;
        this.ownerId = ownerId;
        this.expiryTime = expiryTime;
        this.maxVisits = maxVisits;
        this.visitCount = 0;
    }

    public String getUrl() {
        return url;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public int getMaxVisits() {
        return maxVisits;
    }

    public void setMaxVisits(int maxVisits) {
        this.maxVisits = maxVisits;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void incrementVisit() {
        this.visitCount++;
    }
}
