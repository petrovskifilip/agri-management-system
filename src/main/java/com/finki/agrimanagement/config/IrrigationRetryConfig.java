package com.finki.agrimanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "irrigation.retry")
public class IrrigationRetryConfig {

    /**
     * Maximum number of retry attempts before marking as FAILED
     */
    private int maxAttempts;

    /**
     * Fixed delay in minutes between retry attempts
     */
    private int retryDelayMinutes;

    /**
     * Hours to wait before marking overdue irrigations as FAILED
     */
    private int overdueHours;

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getRetryDelayMinutes() {
        return retryDelayMinutes;
    }

    public void setRetryDelayMinutes(int retryDelayMinutes) {
        this.retryDelayMinutes = retryDelayMinutes;
    }

    public int getOverdueHours() {
        return overdueHours;
    }

    public void setOverdueHours(int overdueHours) {
        this.overdueHours = overdueHours;
    }
}

