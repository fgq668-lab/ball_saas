package com.ballsaas.report;

public record DashboardMetric(
        long todayOrders,
        int todayAmountCent,
        int todayRefundCent,
        long pendingRefunds,
        long venues
) {
}

