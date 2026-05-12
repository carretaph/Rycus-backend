package com.rycus.Rycus_backend.admin;

public class AdminStatsDto {

    private long totalUsers;
    private long adminUsers;
    private long usersWithReferralFee;

    public AdminStatsDto(
            long totalUsers,
            long adminUsers,
            long usersWithReferralFee
    ) {
        this.totalUsers = totalUsers;
        this.adminUsers = adminUsers;
        this.usersWithReferralFee = usersWithReferralFee;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public long getAdminUsers() {
        return adminUsers;
    }

    public long getUsersWithReferralFee() {
        return usersWithReferralFee;
    }
}