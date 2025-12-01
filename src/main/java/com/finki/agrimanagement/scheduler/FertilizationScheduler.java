package com.finki.agrimanagement.scheduler;

import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.enums.FertilizationStatus;
import com.finki.agrimanagement.repository.FertilizationRepository;
import com.finki.agrimanagement.repository.ParcelRepository;
import com.finki.agrimanagement.service.FertilizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class FertilizationScheduler {

    private final FertilizationService fertilizationService;
    private final ParcelRepository parcelRepository;
    private final FertilizationRepository fertilizationRepository;

    public FertilizationScheduler(FertilizationService fertilizationService,
                                  ParcelRepository parcelRepository,
                                  FertilizationRepository fertilizationRepository) {
        this.fertilizationService = fertilizationService;
        this.parcelRepository = parcelRepository;
        this.fertilizationRepository = fertilizationRepository;
    }

    /**
     * Runs every hour to check for scheduled fertilizations that are due
     * and marks them as PENDING (ready for notification to be sent)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkDueFertilizations() {
        log.info("Checking for fertilizations that are due");

        LocalDateTime now = LocalDateTime.now();

        // Find all scheduled fertilizations that should be marked as pending
        List<Fertilization> dueFertilizations = fertilizationService.getFertilizationsDueBeforeByStatus(
                now, FertilizationStatus.SCHEDULED);

        if (dueFertilizations.isEmpty()) {
            log.info("No fertilizations are due at this time");
            return;
        }

        log.info("Found {} fertilization(s) that are due", dueFertilizations.size());

        for (Fertilization fertilization : dueFertilizations) {
            try {
                log.info("Marking fertilization ID: {} as PENDING for parcel: {}",
                        fertilization.getId(),
                        fertilization.getParcel().getName());

                fertilizationService.markAsPending(fertilization.getId());

                // TODO: Send notification to user (will be implemented later)
                log.info("Fertilization ID: {} is now PENDING - notification should be sent to user",
                        fertilization.getId());

            } catch (Exception e) {
                log.error("Failed to process fertilization ID: {}. Error: {}",
                        fertilization.getId(),
                        e.getMessage(),
                        e);
            }
        }
    }

    /**
     * Automatically creates fertilization schedules for parcels that need fertilization
     * based on the crop's fertilization frequency and the parcel's last fertilization time.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scheduleRequiredFertilizations() {
        log.info("Checking for parcels that need fertilization scheduled");

        List<Parcel> allParcels = parcelRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        int scheduledCount = 0;

        for (Parcel parcel : allParcels) {
            try {
                // Skip parcels without a crop or without fertilization frequency configured
                if (parcel.getCrop() == null ||
                    parcel.getCrop().getFertilizationFrequencyDays() == null ||
                    parcel.getCrop().getFertilizerType() == null) {
                    continue;
                }

                int frequencyDays = parcel.getCrop().getFertilizationFrequencyDays();
                String fertilizerType = parcel.getCrop().getFertilizerType();

                // Determine when next fertilization is needed
                LocalDateTime nextFertilizationNeeded;
                if (parcel.getLastFertilizedAt() == null) {
                    // Never fertilized - needs fertilization now
                    nextFertilizationNeeded = now;
                } else {
                    // Calculate based on last fertilization + frequency
                    nextFertilizationNeeded = parcel.getLastFertilizedAt().plusDays(frequencyDays);
                }

                // Only schedule if the next fertilization is due now or overdue
                if (nextFertilizationNeeded.isAfter(now)) {
                    log.debug("Parcel {} does not need fertilization yet. Next due: {}",
                            parcel.getName(), nextFertilizationNeeded);
                    continue;
                }

                // Check if there's already a scheduled or pending fertilization
                List<Fertilization> existingScheduled = fertilizationRepository.findByParcelIdAndStatus(
                        parcel.getId(), FertilizationStatus.SCHEDULED);
                List<Fertilization> existingPending = fertilizationRepository.findByParcelIdAndStatus(
                        parcel.getId(), FertilizationStatus.PENDING);

                if (!existingScheduled.isEmpty() || !existingPending.isEmpty()) {
                    log.debug("Parcel {} already has a scheduled/pending fertilization, skipping",
                            parcel.getName());
                    continue;
                }

                // Schedule new fertilization
                log.info("Auto-scheduling fertilization for parcel: {} (last fertilized: {}, frequency: {} days)",
                        parcel.getName(),
                        parcel.getLastFertilizedAt() != null ? parcel.getLastFertilizedAt() : "never",
                        frequencyDays);

                fertilizationService.scheduleFertilization(
                        parcel.getId(),
                        nextFertilizationNeeded,
                        fertilizerType
                );

                scheduledCount++;

            } catch (Exception e) {
                log.error("Failed to auto-schedule fertilization for parcel ID: {}. Error: {}",
                        parcel.getId(),
                        e.getMessage(),
                        e);
            }
        }

        if (scheduledCount > 0) {
            log.info("Auto-scheduled {} fertilization(s)", scheduledCount);
        } else {
            log.info("No fertilizations needed to be auto-scheduled");
        }
    }
}

