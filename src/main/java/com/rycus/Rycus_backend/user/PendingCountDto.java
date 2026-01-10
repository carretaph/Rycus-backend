package com.rycus.Rycus_backend.user;

public class PendingCountDto {

    private long count;

    public PendingCountDto() {
    }

    public PendingCountDto(long count) {
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
