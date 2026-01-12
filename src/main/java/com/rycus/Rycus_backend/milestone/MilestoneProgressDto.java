package com.rycus.Rycus_backend.milestone;

public class MilestoneProgressDto {

    private String milestoneType;
    private int qualifiedCustomers;
    private int timesAwarded;
    private int nextRewardAt;
    private int remaining;

    public MilestoneProgressDto() {}

    public MilestoneProgressDto(String milestoneType, int qualifiedCustomers, int timesAwarded, int nextRewardAt, int remaining) {
        this.milestoneType = milestoneType;
        this.qualifiedCustomers = qualifiedCustomers;
        this.timesAwarded = timesAwarded;
        this.nextRewardAt = nextRewardAt;
        this.remaining = remaining;
    }

    public String getMilestoneType() { return milestoneType; }
    public void setMilestoneType(String milestoneType) { this.milestoneType = milestoneType; }

    public int getQualifiedCustomers() { return qualifiedCustomers; }
    public void setQualifiedCustomers(int qualifiedCustomers) { this.qualifiedCustomers = qualifiedCustomers; }

    public int getTimesAwarded() { return timesAwarded; }
    public void setTimesAwarded(int timesAwarded) { this.timesAwarded = timesAwarded; }

    public int getNextRewardAt() { return nextRewardAt; }
    public void setNextRewardAt(int nextRewardAt) { this.nextRewardAt = nextRewardAt; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }
}
