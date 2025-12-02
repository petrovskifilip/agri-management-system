package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.config.IrrigationRetryConfig;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.enums.IrrigationStatus;
import com.finki.agrimanagement.exception.ResourceNotFoundException;
import com.finki.agrimanagement.repository.IrrigationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.EmailNotificationService;
import com.finki.agrimanagement.service.IrrigationExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
public class IrrigationExecutionServiceImpl implements IrrigationExecutionService {

    private final IrrigationRepository irrigationRepository;
    private final ParcelRepository parcelRepository;
    private final IrrigationRetryConfig retryConfig;
    private final EmailNotificationService emailNotificationService;

    public IrrigationExecutionServiceImpl(IrrigationRepository irrigationRepository,
                                         ParcelRepository parcelRepository,
                                         IrrigationRetryConfig retryConfig,
                                         EmailNotificationService emailNotificationService) {
        this.irrigationRepository = irrigationRepository;
        this.parcelRepository = parcelRepository;
        this.retryConfig = retryConfig;
        this.emailNotificationService = emailNotificationService;
    }

    @Override
    @Transactional
    public void executeIrrigation(Long irrigationId) {
        Irrigation irrigation = irrigationRepository.findById(irrigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Irrigation not found with id: " + irrigationId));

        try {
            // Start irrigation
            irrigation.setStatus(IrrigationStatus.IN_PROGRESS);
            irrigation.setStartDatetime(LocalDateTime.now());
            irrigation.setUpdatedAt(LocalDateTime.now());
            irrigationRepository.save(irrigation);

            log.info("Starting irrigation execution for ID: {}, Parcel: {}",
                    irrigationId, irrigation.getParcel().getName());

            // - Call IoT device/water pump API
            // - Wait for actual completion or run asynchronously
            // - Handle errors and timeouts
            executeIrrigationHardware(irrigation);

            // For test purposes complete it immediately
            LocalDateTime now = LocalDateTime.now();
            irrigation.setFinishedDatetime(now);
            irrigation.setStatus(IrrigationStatus.COMPLETED);
            irrigation.setUpdatedAt(now);
            irrigation.setStatusDescription("Irrigation completed successfully");
            irrigation.setRetryCount(0); // Reset retry count on success

            // Update parcel's last irrigated time
            Parcel irrigatedParcel = irrigation.getParcel();
            irrigatedParcel.setLastIrrigatedAt(now);
            parcelRepository.save(irrigatedParcel);

            irrigationRepository.save(irrigation);

            log.info("Successfully completed irrigation ID: {}", irrigationId);

            // Send email notification
            try {
                emailNotificationService.sendIrrigationCompletedNotification(irrigation);
            } catch (Exception emailEx) {
                log.error("Failed to send irrigation completed notification email", emailEx);
            }

        } catch (Exception e) {
            log.error("Failed to execute irrigation ID: {}. Error: {}", irrigationId, e.getMessage(), e);
            handleExecutionFailure(irrigation, e);
        }
    }

    /**
     * Activates IoT water pump to start irrigation
     */
    private void executeIrrigationHardware(Irrigation irrigation) {
        // IoTDevice device = iotService.getDeviceForParcel(irrigation.getParcel());
        // device.startIrrigation(irrigation.getDurationMinutes(), irrigation.getWaterAmountLiters());
    }

    /**
     * Handle execution failure with retry logic
     */
    private void handleExecutionFailure(Irrigation irrigation, Exception exception) {
        LocalDateTime now = LocalDateTime.now();
        int currentRetryCount = irrigation.getRetryCount() != null ? irrigation.getRetryCount() : 0;

        irrigation.setRetryCount(currentRetryCount + 1);
        irrigation.setLastRetryAt(now);
        irrigation.setStatusDescription("Execution failed: " + exception.getMessage());
        irrigation.setUpdatedAt(now);

        // Check if max retries exceeded
        if (irrigation.getRetryCount() >= retryConfig.getMaxAttempts()) {
            irrigation.setStatus(IrrigationStatus.FAILED);
            irrigation.setFinishedDatetime(now);
            log.error("Irrigation ID: {} marked as FAILED after {} attempts",
                    irrigation.getId(), irrigation.getRetryCount());

            // Send failure notification email
            try {
                emailNotificationService.sendIrrigationFailedNotification(
                    irrigation,
                    "Maximum retry attempts exceeded. Last error: " + exception.getMessage()
                );
            } catch (Exception emailEx) {
                log.error("Failed to send irrigation failed notification email", emailEx);
            }
        } else {
            // Set back to SCHEDULED for retry with fixed delay
            irrigation.setStatus(IrrigationStatus.SCHEDULED);

            // Use fixed retry delay
            int delayMinutes = retryConfig.getRetryDelayMinutes();
            LocalDateTime nextRetryTime = now.plusMinutes(delayMinutes);
            irrigation.setScheduledDatetime(nextRetryTime);

            log.warn("Irrigation ID: {} will retry in {} minutes (attempt {}/{})",
                    irrigation.getId(), delayMinutes, irrigation.getRetryCount() + 1,
                    retryConfig.getMaxAttempts());
        }

        irrigationRepository.save(irrigation);
    }

    @Override
    @Transactional
    public void stopIrrigation(Long irrigationId) {
        Irrigation irrigation = irrigationRepository.findById(irrigationId)
                .orElseThrow(() -> new ResourceNotFoundException("Irrigation not found with id: " + irrigationId));

        // Only allow stopping if irrigation is currently in progress
        if (irrigation.getStatus() != IrrigationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot stop irrigation. Current status: " + irrigation.getStatus() +
                    ". Only IN_PROGRESS irrigations can be stopped.");
        }

        try {
            log.info("Manually stopping irrigation ID: {}, Parcel: {}",
                    irrigationId, irrigation.getParcel().getName());

            // Stop the irrigation hardware
            stopIrrigationHardware(irrigation);

            // Update irrigation status to STOPPED
            LocalDateTime now = LocalDateTime.now();
            irrigation.setFinishedDatetime(now);
            irrigation.setStatus(IrrigationStatus.STOPPED);
            irrigation.setUpdatedAt(now);
            irrigation.setStatusDescription("Manually stopped by user");

            // Update parcel's last irrigated time (partial irrigation still counts)
            Parcel irrigatedParcel = irrigation.getParcel();
            irrigatedParcel.setLastIrrigatedAt(now);
            parcelRepository.save(irrigatedParcel);

            irrigationRepository.save(irrigation);

            log.info("Successfully stopped irrigation ID: {}", irrigationId);

        } catch (Exception e) {
            log.error("Failed to stop irrigation ID: {}. Error: {}", irrigationId, e.getMessage(), e);
            throw new RuntimeException("Failed to stop irrigation: " + e.getMessage(), e);
        }
    }

    /**
     * Sends stop command to irrigation hardware
     */
    private void stopIrrigationHardware(Irrigation irrigation) {
        // IoTDevice device = iotService.getDeviceForParcel(irrigation.getParcel());
        // device.stopIrrigation();

        log.debug("Stopping irrigation hardware for irrigation ID: {}", irrigation.getId());
    }
}
