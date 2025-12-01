package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.entity.*;
import com.finki.agrimanagement.repository.*;
import com.finki.agrimanagement.service.ExportService;
import com.finki.agrimanagement.service.ParcelService;
import com.finki.agrimanagement.dto.weather.ParcelWeatherDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final FarmRepository farmRepository;
    private final ParcelRepository parcelRepository;
    private final IrrigationRepository irrigationRepository;
    private final FertilizationRepository fertilizationRepository;
    private final CropRepository cropRepository;
    private final ParcelService parcelService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ExportServiceImpl(FarmRepository farmRepository,
                             ParcelRepository parcelRepository,
                             IrrigationRepository irrigationRepository,
                             FertilizationRepository fertilizationRepository,
                             CropRepository cropRepository,
                             ParcelService parcelService) {
        this.farmRepository = farmRepository;
        this.parcelRepository = parcelRepository;
        this.irrigationRepository = irrigationRepository;
        this.fertilizationRepository = fertilizationRepository;
        this.cropRepository = cropRepository;
        this.parcelService = parcelService;
    }

    @Override
    public Workbook exportFarmOverviewReport() {
        Workbook workbook = new XSSFWorkbook();
        List<Farm> farms = farmRepository.findAll();

        // Create Farms sheet
        Sheet farmSheet = workbook.createSheet("Farms");
        createFarmSheet(farmSheet, farms);

        // Create Parcels sheet
        Sheet parcelSheet = workbook.createSheet("Parcels");
        createParcelSheet(parcelSheet, farms);

        return workbook;
    }

    @Override
    public Workbook exportAllIrrigations() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Irrigation Report");

        List<Irrigation> irrigations = irrigationRepository.findAll();
        createIrrigationSheet(sheet, irrigations);

        return workbook;
    }

    @Override
    public Workbook exportIrrigationByFarm(Long farmId) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Irrigation Report");

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found with id: " + farmId));

        List<Irrigation> irrigations = farm.getParcels().stream()
                .flatMap(p -> p.getIrrigations().stream())
                .collect(Collectors.toList());

        createIrrigationSheet(sheet, irrigations);
        return workbook;
    }

    @Override
    public Workbook exportAllFertilizations() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fertilization Report");

        List<Fertilization> fertilizations = fertilizationRepository.findAll();
        createFertilizationSheet(sheet, fertilizations);

        return workbook;
    }

    @Override
    public Workbook exportFertilizationByFarm(Long farmId) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Fertilization Report");

        Farm farm = farmRepository.findById(farmId)
                .orElseThrow(() -> new RuntimeException("Farm not found with id: " + farmId));

        List<Fertilization> fertilizations = farm.getParcels().stream()
                .flatMap(p -> p.getFertilizations().stream())
                .collect(Collectors.toList());

        createFertilizationSheet(sheet, fertilizations);
        return workbook;
    }

    @Override
    public Workbook exportParcelActivityReport(Long parcelId) {
        Workbook workbook = new XSSFWorkbook();
        Parcel parcel = parcelRepository.findById(parcelId).orElseThrow();

        // Parcel Info Sheet
        Sheet infoSheet = workbook.createSheet("Parcel Information");
        createParcelInfoSheet(infoSheet, parcel);

        // Irrigation History
        Sheet irrigationSheet = workbook.createSheet("Irrigation History");
        createIrrigationSheet(irrigationSheet, parcel.getIrrigations());

        // Fertilization History
        Sheet fertilizationSheet = workbook.createSheet("Fertilization History");
        createFertilizationSheet(fertilizationSheet, parcel.getFertilizations());

        return workbook;
    }

    @Override
    public Workbook exportCropManagementReport() {
        Workbook workbook = new XSSFWorkbook();
        List<Crop> crops = cropRepository.findAll();

        // Crop Information Sheet
        Sheet cropSheet = workbook.createSheet("Crops");
        createCropSheet(cropSheet, crops);

        // Crop Distribution Sheet
        Sheet distributionSheet = workbook.createSheet("Crop Distribution");
        createCropDistributionSheet(distributionSheet, crops);

        return workbook;
    }

    @Override
    public Workbook exportCompleteFarmReport(Long farmId) {
        Workbook workbook = new XSSFWorkbook();
        Farm farm = farmRepository.findById(farmId).orElseThrow();

        // Farm Info
        Sheet farmSheet = workbook.createSheet("Farm Information");
        createFarmInfoSheet(farmSheet, farm);

        // Parcels
        Sheet parcelSheet = workbook.createSheet("Parcels");
        createParcelSheet(parcelSheet, List.of(farm));

        // Irrigations
        List<Irrigation> irrigations = farm.getParcels().stream()
                .flatMap(p -> p.getIrrigations().stream())
                .collect(Collectors.toList());
        Sheet irrigationSheet = workbook.createSheet("Irrigations");
        createIrrigationSheet(irrigationSheet, irrigations);

        // Fertilizations
        List<Fertilization> fertilizations = farm.getParcels().stream()
                .flatMap(p -> p.getFertilizations().stream())
                .collect(Collectors.toList());
        Sheet fertilizationSheet = workbook.createSheet("Fertilizations");
        createFertilizationSheet(fertilizationSheet, fertilizations);

        return workbook;
    }

    // Helper methods for creating sheets
    private void createFarmSheet(Sheet sheet, List<Farm> farms) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Location", "Parcel Count", "Created At"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Farm farm : farms) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(farm.getId());
            row.createCell(1).setCellValue(farm.getName());
            row.createCell(2).setCellValue(farm.getLocation() != null ? farm.getLocation() : "");
            row.createCell(3).setCellValue(farm.getParcels().size());
            row.createCell(4).setCellValue(formatDateTime(farm.getCreatedAt()));
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createFarmInfoSheet(Sheet sheet, Farm farm) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        int rowNum = 0;
        createInfoRow(sheet, rowNum++, "Farm ID", String.valueOf(farm.getId()), headerStyle);
        createInfoRow(sheet, rowNum++, "Name", farm.getName(), headerStyle);
        createInfoRow(sheet, rowNum++, "Location", farm.getLocation() != null ? farm.getLocation() : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Total Parcels", String.valueOf(farm.getParcels().size()), headerStyle);
        createInfoRow(sheet, rowNum++, "Total Area (sqm)", String.valueOf(farm.getParcels().stream()
                .mapToDouble(p -> p.getArea() != null ? p.getArea() : 0).sum()), headerStyle);
        createInfoRow(sheet, rowNum, "Created At", formatDateTime(farm.getCreatedAt()), headerStyle);

        autoSizeColumns(sheet, 2);
    }

    private void createParcelSheet(Sheet sheet, List<Farm> farms) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Farm", "Crop", "Area (sqm)", "Latitude", "Longitude",
                "Last Irrigated", "Last Fertilized"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Farm farm : farms) {
            for (Parcel parcel : farm.getParcels()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(parcel.getId());
                row.createCell(1).setCellValue(parcel.getName());
                row.createCell(2).setCellValue(farm.getName());
                row.createCell(3).setCellValue(parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
                row.createCell(4).setCellValue(parcel.getArea() != null ? parcel.getArea() : 0);
                row.createCell(5).setCellValue(parcel.getLatitude() != null ? parcel.getLatitude() : 0);
                row.createCell(6).setCellValue(parcel.getLongitude() != null ? parcel.getLongitude() : 0);
                row.createCell(7).setCellValue(parcel.getLastIrrigatedAt() != null ?
                        formatDateTime(parcel.getLastIrrigatedAt()) : "Never");
                row.createCell(8).setCellValue(parcel.getLastFertilizedAt() != null ?
                        formatDateTime(parcel.getLastFertilizedAt()) : "Never");
            }
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createParcelInfoSheet(Sheet sheet, Parcel parcel) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        int rowNum = 0;
        createInfoRow(sheet, rowNum++, "Parcel ID", String.valueOf(parcel.getId()), headerStyle);
        createInfoRow(sheet, rowNum++, "Name", parcel.getName(), headerStyle);
        createInfoRow(sheet, rowNum++, "Farm", parcel.getFarm().getName(), headerStyle);
        createInfoRow(sheet, rowNum++, "Crop", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Area (sqm)", String.valueOf(parcel.getArea() != null ? parcel.getArea() : 0), headerStyle);
        createInfoRow(sheet, rowNum++, "Coordinates", String.format("%.6f, %.6f",
                parcel.getLatitude() != null ? parcel.getLatitude() : 0,
                parcel.getLongitude() != null ? parcel.getLongitude() : 0), headerStyle);
        createInfoRow(sheet, rowNum++, "Last Irrigated", parcel.getLastIrrigatedAt() != null ?
                formatDateTime(parcel.getLastIrrigatedAt()) : "Never", headerStyle);
        createInfoRow(sheet, rowNum, "Last Fertilized", parcel.getLastFertilizedAt() != null ?
                formatDateTime(parcel.getLastFertilizedAt()) : "Never", headerStyle);

        autoSizeColumns(sheet, 2);
    }

    private void createIrrigationSheet(Sheet sheet, List<Irrigation> irrigations) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Parcel", "Farm", "Scheduled", "Duration (min)", "Water (L)",
                "Status", "Started", "Finished", "Retry Count"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Irrigation irrigation : irrigations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(irrigation.getId());
            row.createCell(1).setCellValue(irrigation.getParcel().getName());
            row.createCell(2).setCellValue(irrigation.getParcel().getFarm().getName());
            row.createCell(3).setCellValue(formatDateTime(irrigation.getScheduledDatetime()));
            row.createCell(4).setCellValue(irrigation.getDurationMinutes() != null ? irrigation.getDurationMinutes() : 0);
            row.createCell(5).setCellValue(irrigation.getWaterAmountLiters() != null ? irrigation.getWaterAmountLiters() : 0);
            row.createCell(6).setCellValue(irrigation.getStatus().toString());
            row.createCell(7).setCellValue(irrigation.getStartDatetime() != null ?
                    formatDateTime(irrigation.getStartDatetime()) : "N/A");
            row.createCell(8).setCellValue(irrigation.getFinishedDatetime() != null ?
                    formatDateTime(irrigation.getFinishedDatetime()) : "N/A");
            row.createCell(9).setCellValue(irrigation.getRetryCount());
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createFertilizationSheet(Sheet sheet, List<Fertilization> fertilizations) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Parcel", "Farm", "Scheduled", "Fertilizer Type",
                "Status", "Completed", "Notes"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Fertilization fertilization : fertilizations) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(fertilization.getId());
            row.createCell(1).setCellValue(fertilization.getParcel().getName());
            row.createCell(2).setCellValue(fertilization.getParcel().getFarm().getName());
            row.createCell(3).setCellValue(formatDateTime(fertilization.getScheduledDatetime()));
            row.createCell(4).setCellValue(fertilization.getFertilizerType() != null ?
                    fertilization.getFertilizerType() : "N/A");
            row.createCell(5).setCellValue(fertilization.getStatus().toString());
            row.createCell(6).setCellValue(fertilization.getCompletedDatetime() != null ?
                    formatDateTime(fertilization.getCompletedDatetime()) : "N/A");
            row.createCell(7).setCellValue(fertilization.getNotes() != null ? fertilization.getNotes() : "");
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createCropSheet(Sheet sheet, List<Crop> crops) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Name", "Irrigation Freq (days)", "Fertilization Freq (days)",
                "Fertilizer Type", "Irrigation Duration (min)", "Water Requirement (L/sqm)", "Parcels Count"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Crop crop : crops) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(crop.getId());
            row.createCell(1).setCellValue(crop.getName());
            row.createCell(2).setCellValue(crop.getIrrigationFrequencyDays() != null ?
                    crop.getIrrigationFrequencyDays() : 0);
            row.createCell(3).setCellValue(crop.getFertilizationFrequencyDays() != null ?
                    crop.getFertilizationFrequencyDays() : 0);
            row.createCell(4).setCellValue(crop.getFertilizerType() != null ? crop.getFertilizerType() : "N/A");
            row.createCell(5).setCellValue(crop.getIrrigationDurationMinutes() != null ?
                    crop.getIrrigationDurationMinutes() : 0);
            row.createCell(6).setCellValue(crop.getWaterRequirementLitersPerSqm() != null ?
                    crop.getWaterRequirementLitersPerSqm() : 0);
            row.createCell(7).setCellValue(crop.getParcels().size());
        }

        autoSizeColumns(sheet, headers.length);
    }

    private void createCropDistributionSheet(Sheet sheet, List<Crop> crops) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());

        // Header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Crop", "Total Parcels", "Total Area (sqm)", "Avg Area per Parcel (sqm)"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        int rowNum = 1;
        for (Crop crop : crops) {
            Row row = sheet.createRow(rowNum++);
            double totalArea = crop.getParcels().stream()
                    .mapToDouble(p -> p.getArea() != null ? p.getArea() : 0)
                    .sum();
            int parcelCount = crop.getParcels().size();

            row.createCell(0).setCellValue(crop.getName());
            row.createCell(1).setCellValue(parcelCount);
            row.createCell(2).setCellValue(totalArea);
            row.createCell(3).setCellValue(parcelCount > 0 ? totalArea / parcelCount : 0);
        }

        autoSizeColumns(sheet, headers.length);
    }

    // Utility methods
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        byte[] rgb = new byte[]{(byte) 129, (byte) 226, (byte) 142};
        XSSFColor customColor = new XSSFColor(rgb, null);
        style.setFillForegroundColor(customColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createInfoRow(Sheet sheet, int rowNum, String label, String value, CellStyle labelStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
    }

    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_TIME_FORMATTER) : "N/A";
    }

    @Override
    public Workbook exportParcelWeather(Long parcelId) {
        Workbook workbook = new XSSFWorkbook();

        // Get parcel and weather data
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found with id: " + parcelId));
        ParcelWeatherDTO weather = parcelService.getParcelWeather(parcelId);

        // Create Weather Data sheet
        Sheet weatherSheet = workbook.createSheet("Current Weather");
        createWeatherSheet(weatherSheet, parcel, weather);

        return workbook;
    }

    private void createWeatherSheet(Sheet sheet, Parcel parcel, ParcelWeatherDTO weather) {
        CellStyle headerStyle = createHeaderStyle(sheet.getWorkbook());
        CellStyle sectionStyle = createSectionHeaderStyle(sheet.getWorkbook());

        int rowNum = 0;

        // Parcel Information Section
        Row sectionRow = sheet.createRow(rowNum++);
        Cell sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("PARCEL INFORMATION");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum++, "Parcel ID", String.valueOf(parcel.getId()), headerStyle);
        createInfoRow(sheet, rowNum++, "Parcel Name", parcel.getName(), headerStyle);
        createInfoRow(sheet, rowNum++, "Farm", parcel.getFarm().getName(), headerStyle);
        createInfoRow(sheet, rowNum++, "Location", String.format("%.6f, %.6f",
                parcel.getLatitude() != null ? parcel.getLatitude() : 0,
                parcel.getLongitude() != null ? parcel.getLongitude() : 0), headerStyle);
        rowNum++;

        // Weather Conditions Section
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("CURRENT WEATHER CONDITIONS");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum++, "Location Name",
                weather.getLocationName() != null ? weather.getLocationName() : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Country",
                weather.getCountry() != null ? weather.getCountry() : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Weather Condition",
                weather.getWeatherCondition() != null ? weather.getWeatherCondition() : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Description",
                weather.getWeatherDescription() != null ? weather.getWeatherDescription() : "N/A", headerStyle);
        rowNum++;

        // Temperature Section
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("TEMPERATURE");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum++, "Temperature",
                weather.getTemperature() != null ? String.format("%.1f °C", weather.getTemperature()) : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Feels Like",
                weather.getFeelsLike() != null ? String.format("%.1f °C", weather.getFeelsLike()) : "N/A", headerStyle);
        rowNum++;

        // Atmospheric Conditions Section
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("ATMOSPHERIC CONDITIONS");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum++, "Humidity",
                weather.getHumidity() != null ? weather.getHumidity() + " %" : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Pressure",
                weather.getPressure() != null ? weather.getPressure() + " hPa" : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Cloudiness",
                weather.getCloudiness() != null ? weather.getCloudiness() + " %" : "N/A", headerStyle);
        createInfoRow(sheet, rowNum++, "Visibility",
                weather.getVisibility() != null ? weather.getVisibility() + " m" : "N/A", headerStyle);
        rowNum++;

        // Wind Section
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("WIND");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum++, "Wind Speed",
                weather.getWindSpeed() != null ? String.format("%.1f m/s", weather.getWindSpeed()) : "N/A", headerStyle);
        rowNum++;

        // Rain Section
        sectionRow = sheet.createRow(rowNum++);
        sectionCell = sectionRow.createCell(0);
        sectionCell.setCellValue("PRECIPITATION");
        sectionCell.setCellStyle(sectionStyle);
        rowNum++;

        createInfoRow(sheet, rowNum, "Rain Expected (1 hour)",
                weather.getRainExpectedInOneHour() != null ? String.format("%.1f mm", weather.getRainExpectedInOneHour()) : "0 mm", headerStyle);

        autoSizeColumns(sheet, 2);
    }

    private CellStyle createSectionHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }
}

