package com.finki.agrimanagement.controller;

import com.finki.agrimanagement.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/farms")
    public void exportFarmOverview(HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportFarmOverviewReport();
        writeWorkbookToResponse(response, workbook, "farm-overview-report");
    }

    @GetMapping("/irrigations")
    public void exportAllIrrigations(HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportAllIrrigations();
        writeWorkbookToResponse(response, workbook, "irrigations-all");
    }

    @GetMapping("/irrigations/farm/{farmId}")
    public void exportIrrigationsByFarm(
            @PathVariable Long farmId,
            HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportIrrigationByFarm(farmId);
        writeWorkbookToResponse(response, workbook, "irrigations-farm-" + farmId);
    }

    @GetMapping("/fertilizations")
    public void exportAllFertilizations(HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportAllFertilizations();
        writeWorkbookToResponse(response, workbook, "fertilizations-all");
    }

    @GetMapping("/fertilizations/farm/{farmId}")
    public void exportFertilizationsByFarm(
            @PathVariable Long farmId,
            HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportFertilizationByFarm(farmId);
        writeWorkbookToResponse(response, workbook, "fertilizations-farm-" + farmId);
    }

    @GetMapping("/parcel/{parcelId}")
    public void exportParcelActivity(
            @PathVariable Long parcelId,
            HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportParcelActivityReport(parcelId);
        writeWorkbookToResponse(response, workbook, "parcel-" + parcelId + "-activity");
    }

    @GetMapping("/crops")
    public void exportCropManagement(HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportCropManagementReport();
        writeWorkbookToResponse(response, workbook, "crop-management-report");
    }

    @GetMapping("/farm/{farmId}")
    public void exportCompleteFarm(
            @PathVariable Long farmId,
            HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportCompleteFarmReport(farmId);
        writeWorkbookToResponse(response, workbook, "farm-" + farmId + "-complete");
    }

    @GetMapping("/parcel/{parcelId}/weather")
    public void exportParcelWeather(
            @PathVariable Long parcelId,
            HttpServletResponse response) throws IOException {
        Workbook workbook = exportService.exportParcelWeather(parcelId);
        writeWorkbookToResponse(response, workbook, "parcel-" + parcelId + "-weather");
    }

    private void writeWorkbookToResponse(HttpServletResponse response, Workbook workbook, String baseFilename)
            throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = baseFilename + "-" + timestamp + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}

