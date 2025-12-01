package com.finki.agrimanagement.service;

import org.apache.poi.ss.usermodel.Workbook;

public interface ExportService {

    /**
     * Export all farms with their parcels and crops
     */
    Workbook exportFarmOverviewReport();

    /**
     * Export all irrigation records
     */
    Workbook exportAllIrrigations();

    /**
     * Export irrigation history for a specific farm
     */
    Workbook exportIrrigationByFarm(Long farmId);

    /**
     * Export all fertilization records
     */
    Workbook exportAllFertilizations();

    /**
     * Export fertilization history for a specific farm
     */
    Workbook exportFertilizationByFarm(Long farmId);

    /**
     * Export parcel activity report combining irrigation and fertilization
     */
    Workbook exportParcelActivityReport(Long parcelId);

    /**
     * Export crop management report showing crop distribution and requirements
     */
    Workbook exportCropManagementReport();

    /**
     * Export comprehensive farm report with all data
     */
    Workbook exportCompleteFarmReport(Long farmId);

    /**
     * Export current weather data for a specific parcel
     */
    Workbook exportParcelWeather(Long parcelId);
}

