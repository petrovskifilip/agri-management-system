package com.finki.agrimanagement.scheduler;

import com.finki.agrimanagement.config.IrrigationRetryConfig;
import com.finki.agrimanagement.entity.Crop;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.enums.IrrigationStatus;
import com.finki.agrimanagement.repository.IrrigationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.IrrigationExecutionService;
import com.finki.agrimanagement.service.IrrigationService;
import com.finki.agrimanagement.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class IrrigationScheduler {

    private final IrrigationService irrigationService;
    private final IrrigationExecutionService irrigationExecutionService;
    private final IrrigationRetryConfig retryConfig;
    private final ParcelRepository parcelRepository;
    private final IrrigationRepository irrigationRepository;
    private final WeatherService weatherService;

    public IrrigationScheduler(IrrigationService irrigationService,
                               IrrigationExecutionService irrigationExecutionService,
                               IrrigationRetryConfig retryConfig,
                               ParcelRepository parcelRepository,
                               IrrigationRepository irrigationRepository,
                               WeatherService weatherService) {
        this.irrigationService = irrigationService;
        this.irrigationExecutionService = irrigationExecutionService;
        this.retryConfig = retryConfig;
        this.parcelRepository = parcelRepository;
        this.irrigationRepository = irrigationRepository;
        this.weatherService = weatherService;
    }

    /**
     * Runs every minute to check for scheduled irrigations that need to be executed.
     * Checks for irrigations that are:
     * - In SCHEDULED or RETRYING status
     * - Have a scheduled datetime that is now or in the past
     */
    @Scheduled(cron = "0 * * * * *")
    public void executeScheduledIrrigations() {
        log.info("Checking for scheduled irrigations to execute");

        LocalDateTime now = LocalDateTime.now();

        // Find all scheduled and retrying irrigations that should be executed
        List<Irrigation> scheduledIrrigations = irrigationService.getIrrigationsDueBeforeByStatus(now, IrrigationStatus.SCHEDULED);
        List<Irrigation> retryingIrrigations = irrigationService.getIrrigationsDueBeforeByStatus(now, IrrigationStatus.RETRYING);

        List<Irrigation> irrigationsToExecute = new ArrayList<>(scheduledIrrigations);
        irrigationsToExecute.addAll(retryingIrrigations);

        if (irrigationsToExecute.isEmpty()) {
            log.info("No irrigations to execute at this time");
            return;
        }

        log.info("Found {} irrigation(s) to execute ({} scheduled, {} retrying)",
                irrigationsToExecute.size(), scheduledIrrigations.size(), retryingIrrigations.size());

        for (Irrigation irrigation : irrigationsToExecute) {
            try {
                Parcel parcel = irrigation.getParcel();

                String statusInfo = irrigation.getStatus() == IrrigationStatus.RETRYING
                        ? " (retry attempt " + (irrigation.getRetryCount() + 1) + ")"
                        : "";
                log.info("Executing irrigation ID: {} for parcel: {}{}",
                        irrigation.getId(),
                        parcel.getName(),
                        statusInfo);

                // Weather check - postpone if rain detected
                if (checkWeatherAndPostponeIfNeeded(irrigation, parcel)) {
                    continue;
                }

                irrigationExecutionService.executeIrrigation(irrigation.getId());

                log.info("Successfully executed irrigation ID: {}", irrigation.getId());
            } catch (Exception e) {
                log.error("Failed to execute irrigation ID: {}. Error: {}",
                        irrigation.getId(),
                        e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Check for overdue irrigations that have exceeded max retry attempts
     * and mark them as failed. Checks both SCHEDULED and RETRYING statuses.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkOverdueIrrigations() {
        log.info("Checking for overdue irrigations");

        LocalDateTime overdueDeadline = LocalDateTime.now().minusHours(retryConfig.getOverdueHours());

        List<Irrigation> overdueScheduled = irrigationService.getIrrigationsDueBeforeByStatus(overdueDeadline, IrrigationStatus.SCHEDULED);
        List<Irrigation> overdueRetrying = irrigationService.getIrrigationsDueBeforeByStatus(overdueDeadline, IrrigationStatus.RETRYING);

        List<Irrigation> overdueIrrigations = new ArrayList<>(overdueScheduled);
        overdueIrrigations.addAll(overdueRetrying);

        if (overdueIrrigations.isEmpty()) {
            log.info("No overdue irrigations found");
            return;
        }

        log.warn("Found {} overdue irrigation(s) past {}-hour deadline",
                overdueIrrigations.size(), retryConfig.getOverdueHours());

        for (Irrigation irrigation : overdueIrrigations) {
            try {
                int retryCount = irrigation.getRetryCount() != null ? irrigation.getRetryCount() : 0;

                // Only mark as failed if it hasn't been marked already
                // This is a safety check for stuck irrigations
                if (irrigation.getStatus() == IrrigationStatus.SCHEDULED ||
                        irrigation.getStatus() == IrrigationStatus.RETRYING) {
                    log.warn("Marking irrigation ID: {} as FAILED (overdue after {} hours, {} retry attempts)",
                            irrigation.getId(), retryConfig.getOverdueHours(), retryCount);

                    irrigationService.updateIrrigationStatus(irrigation.getId(), IrrigationStatus.FAILED);
                }

            } catch (Exception e) {
                log.error("Failed to mark irrigation ID: {} as failed. Error: {}",
                        irrigation.getId(),
                        e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Automatically creates irrigation schedules for parcels that need irrigation
     * based on the crop's irrigation frequency and the parcel's last irrigation time.
     */
    @Scheduled(cron = "0 * * * * *")
    public void scheduleRequiredIrrigations() {
        log.info("Checking for parcels that need irrigation scheduled");

        List<Parcel> allParcels = parcelRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        int scheduledCount = 0;

        for (Parcel parcel : allParcels) {
            try {
                // Skip parcels without a crop or without irrigation frequency configured
                if (parcel.getCrop() == null || parcel.getCrop().getIrrigationFrequencyDays() == null) {
                    continue;
                }

                int frequencyDays = parcel.getCrop().getIrrigationFrequencyDays();

                // Determine when next irrigation is needed
                LocalDateTime nextIrrigationNeeded;
                if (parcel.getLastIrrigatedAt() == null) {
                    // Never irrigated - needs irrigation now
                    nextIrrigationNeeded = now;
                } else {
                    // Calculate based on last irrigation + frequency
                    nextIrrigationNeeded = parcel.getLastIrrigatedAt().plusDays(frequencyDays);
                }

                // Check if there's already a scheduled irrigation that covers this need
                // Only skip if there's a scheduled irrigation within the irrigation frequency window
                List<Irrigation> existingScheduled = irrigationRepository.findByParcelIdAndStatus(
                        parcel.getId(), IrrigationStatus.SCHEDULED);

                boolean hasRelevantScheduledIrrigation = false;
                if (!existingScheduled.isEmpty()) {
                    // Check if any scheduled irrigation is within the next irrigation cycle
                    // If the soonest scheduled irrigation is more than frequencyDays away, we need to schedule one sooner
                    LocalDateTime soonestScheduled = existingScheduled.stream()
                            .map(Irrigation::getScheduledDatetime)
                            .min(LocalDateTime::compareTo)
                            .orElse(null);

                    // Only consider it relevant if it's scheduled before the next required irrigation
                    // Plus a grace period (e.g., half the frequency to allow some flexibility)
                    LocalDateTime gracePeriod = nextIrrigationNeeded.plusDays(frequencyDays / 2);
                    if (soonestScheduled.isBefore(gracePeriod)) {
                        hasRelevantScheduledIrrigation = true;
                        log.debug("Parcel {} has relevant scheduled irrigation at {}, skipping auto-schedule",
                                parcel.getName(), soonestScheduled);
                    } else {
                        log.info("Parcel {} has scheduled irrigation at {} but it's too far (needed by {}), will auto-schedule",
                                parcel.getName(), soonestScheduled, nextIrrigationNeeded);
                    }
                }

                if (hasRelevantScheduledIrrigation) {
                    continue;
                }

                // Determine when to schedule the irrigation
                LocalDateTime scheduledTime;

                if (parcel.getLastIrrigatedAt() == null) {
                    // Never irrigated - schedule immediately
                    scheduledTime = now.plusMinutes(5);
                    log.info("Parcel {} has never been irrigated, scheduling first irrigation",
                            parcel.getName());
                } else if (nextIrrigationNeeded.isBefore(now) || nextIrrigationNeeded.isEqual(now)) {
                    // Overdue - schedule immediately
                    scheduledTime = now.plusMinutes(5);
                    log.info("Parcel {} irrigation is overdue (last irrigated: {}, frequency: {} days), scheduling immediately",
                            parcel.getName(), parcel.getLastIrrigatedAt(), frequencyDays);
                } else {
                    // Schedule for the exact time it's needed
                    scheduledTime = nextIrrigationNeeded;
                    log.info("Parcel {} needs irrigation (last irrigated: {}, frequency: {} days), scheduling for {}",
                            parcel.getName(), parcel.getLastIrrigatedAt(), frequencyDays, scheduledTime);
                }

                createIrrigationSchedule(parcel, scheduledTime);
                scheduledCount++;

            } catch (Exception e) {
                log.error("Error processing parcel {} for auto-irrigation scheduling: {}",
                        parcel.getId(), e.getMessage(), e);
            }
        }

        if (scheduledCount > 0) {
            log.info("Successfully scheduled {} new irrigation(s)", scheduledCount);
        } else {
            log.info("No new irrigations needed at this time");
        }
    }

    /**
     * Creates an irrigation record for the given parcel.
     * Uses crop-specific irrigation parameters and calculates water amount based on parcel area.
     */
    private void createIrrigationSchedule(Parcel parcel, LocalDateTime scheduledTime) {
        Irrigation irrigation = new Irrigation();
        irrigation.setParcel(parcel);
        irrigation.setScheduledDatetime(scheduledTime);
        irrigation.setStatus(IrrigationStatus.SCHEDULED);

        Crop crop = parcel.getCrop();

        // Set duration from crop configuration, or use default if not configured
        Integer duration = crop.getIrrigationDurationMinutes();
        if (duration == null || duration <= 0) {
            duration = 30; // Default 30 minutes
            log.debug("Using default irrigation duration (30 min) for parcel {} - crop has no duration configured",
                    parcel.getName());
        }
        irrigation.setDurationMinutes(duration);

        // Calculate water amount based on parcel area and crop water requirement
        double waterAmount;
        Double waterPerSqm = crop.getWaterRequirementLitersPerSqm();
        Double parcelArea = parcel.getArea();

        if (waterPerSqm != null && waterPerSqm > 0 && parcelArea != null && parcelArea > 0) {
            // Calculate: area (sqm) * water requirement (liters/sqm) = total liters
            waterAmount = parcelArea * waterPerSqm;
            log.debug("Calculated water amount for parcel {}: {} sqm * {} L/sqm = {} L",
                    parcel.getName(), parcelArea, waterPerSqm, waterAmount);
        } else {
            // Use default if calculation not possible
            waterAmount = 100.0; // Default 100 liters
            log.debug("Using default water amount (100L) for parcel {} - area: {}, water/sqm: {}",
                    parcel.getName(), parcelArea, waterPerSqm);
        }
        irrigation.setWaterAmountLiters(waterAmount);

        irrigation.setRetryCount(0);
        irrigation.setCreatedAt(LocalDateTime.now());
        irrigation.setUpdatedAt(LocalDateTime.now());

        irrigationRepository.save(irrigation);

        log.info("Created irrigation schedule for parcel {} at {} (duration: {} min, water: {} L)",
                parcel.getName(), scheduledTime, duration, waterAmount);
    }

    /**
     * Check weather conditions and postpone irrigation if rain is detected
     *
     * @param irrigation The irrigation to check
     * @param parcel     The parcel associated with the irrigation
     * @return true if irrigation should be postponed, false otherwise
     */
    private boolean checkWeatherAndPostponeIfNeeded(Irrigation irrigation, Parcel parcel) {
        if (parcel.getLatitude() == null || parcel.getLongitude() == null) {
            log.debug("Parcel {} has no coordinates set, skipping weather check", parcel.getName());
            return false;
        }

        var rainCheck = weatherService.checkRainConditions(
                parcel.getLatitude(),
                parcel.getLongitude()
        );

        if (rainCheck.isRaining() || rainCheck.isRainExpectedInOneHour()) {
            String reason = rainCheck.isRaining() ? "currently raining" : "rain expected in next hour";
            log.info("Postponing irrigation ID: {} by 2 hours - {} for parcel: {}",
                    irrigation.getId(), reason, parcel.getName());

            irrigation.setScheduledDatetime(irrigation.getScheduledDatetime().plusHours(2));
            irrigation.setStatusDescription("Postponed by 2 hours - " + reason);
            irrigation.setUpdatedAt(LocalDateTime.now());
            irrigationRepository.save(irrigation);
            return true;
        }

        return false;
    }
}

