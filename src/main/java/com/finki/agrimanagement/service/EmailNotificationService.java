package com.finki.agrimanagement.service;

import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.entity.Irrigation;

public interface EmailNotificationService {

    /**
     * Send notification when an irrigation is successfully completed
     */
    void sendIrrigationCompletedNotification(Irrigation irrigation);

    /**
     * Send notification when an irrigation fails
     */
    void sendIrrigationFailedNotification(Irrigation irrigation, String reason);

    /**
     * Send notification when an irrigation is postponed due to weather
     */
    void sendIrrigationPostponedNotification(Irrigation irrigation, String weatherReason);

    /**
     * Send notification when a fertilization is due
     */
    void sendFertilizationDueNotification(Fertilization fertilization);

    /**
     * Send notification when a fertilization is completed
     */
    void sendFertilizationCompletedNotification(Fertilization fertilization);

    /**
     * Send notification when a fertilization is cancelled
     */
    void sendFertilizationCancelledNotification(Fertilization fertilization);

}

