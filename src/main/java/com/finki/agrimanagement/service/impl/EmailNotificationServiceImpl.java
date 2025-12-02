package com.finki.agrimanagement.service.impl;

import com.finki.agrimanagement.config.NotificationConfig;
import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.entity.Parcel;
import com.finki.agrimanagement.service.EmailNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final JavaMailSender mailSender;
    private final NotificationConfig notificationConfig;
    private final TemplateEngine templateEngine;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EmailNotificationServiceImpl(JavaMailSender mailSender,
                                        NotificationConfig notificationConfig,
                                        TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.notificationConfig = notificationConfig;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendIrrigationCompletedNotification(Irrigation irrigation) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = irrigation.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("waterAmount", irrigation.getWaterAmountLiters());
            context.setVariable("scheduledTime", irrigation.getScheduledDatetime().format(DATE_TIME_FORMATTER));
            context.setVariable("startTime", irrigation.getStartDatetime() != null ?
                irrigation.getStartDatetime().format(DATE_TIME_FORMATTER) : "N/A");
            context.setVariable("finishTime", irrigation.getFinishedDatetime() != null ?
                irrigation.getFinishedDatetime().format(DATE_TIME_FORMATTER) : "N/A");

            String htmlContent = templateEngine.process("email/irrigation-completed", context);

            sendEmail(
                notificationConfig.getTo(),
                "✅ Irrigation Completed - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent irrigation completed notification for irrigation ID: {}", irrigation.getId());
        } catch (Exception e) {
            log.error("Failed to send irrigation completed notification", e);
        }
    }

    @Override
    public void sendIrrigationFailedNotification(Irrigation irrigation, String reason) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = irrigation.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("waterAmount", irrigation.getWaterAmountLiters());
            context.setVariable("scheduledTime", irrigation.getScheduledDatetime().format(DATE_TIME_FORMATTER));
            context.setVariable("reason", reason);
            context.setVariable("retryCount", irrigation.getRetryCount());

            String htmlContent = templateEngine.process("email/irrigation-failed", context);

            sendEmail(
                notificationConfig.getTo(),
                "❌ Irrigation Failed - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent irrigation failed notification for irrigation ID: {}", irrigation.getId());
        } catch (Exception e) {
            log.error("Failed to send irrigation failed notification", e);
        }
    }

    @Override
    public void sendIrrigationPostponedNotification(Irrigation irrigation, String weatherReason) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = irrigation.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("waterAmount", irrigation.getWaterAmountLiters());
            context.setVariable("originalTime", irrigation.getScheduledDatetime().format(DATE_TIME_FORMATTER));
            context.setVariable("newTime", irrigation.getScheduledDatetime() != null ?
                irrigation.getScheduledDatetime().format(DATE_TIME_FORMATTER) : "To be determined");
            context.setVariable("weatherReason", weatherReason);

            String htmlContent = templateEngine.process("email/irrigation-postponed", context);

            sendEmail(
                notificationConfig.getTo(),
                "⏰ Irrigation Postponed - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent irrigation postponed notification for irrigation ID: {}", irrigation.getId());
        } catch (Exception e) {
            log.error("Failed to send irrigation postponed notification", e);
        }
    }

    @Override
    public void sendFertilizationDueNotification(Fertilization fertilization) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = fertilization.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("fertilizerType", fertilization.getFertilizerType());
            context.setVariable("scheduledDate", fertilization.getScheduledDatetime().format(DATE_TIME_FORMATTER));

            String htmlContent = templateEngine.process("email/fertilization-due", context);

            sendEmail(
                notificationConfig.getTo(),
                "🌱 Fertilization Due - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent fertilization due notification for fertilization ID: {}", fertilization.getId());
        } catch (Exception e) {
            log.error("Failed to send fertilization due notification", e);
        }
    }

    @Override
    public void sendFertilizationCompletedNotification(Fertilization fertilization) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = fertilization.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("fertilizerType", fertilization.getFertilizerType());
            context.setVariable("scheduledDate", fertilization.getScheduledDatetime().format(DATE_TIME_FORMATTER));
            context.setVariable("completedDate", fertilization.getCompletedDatetime() != null ?
                fertilization.getCompletedDatetime().format(DATE_TIME_FORMATTER) : "N/A");

            String htmlContent = templateEngine.process("email/fertilization-completed", context);

            sendEmail(
                notificationConfig.getTo(),
                "✅ Fertilization Completed - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent fertilization completed notification for fertilization ID: {}", fertilization.getId());
        } catch (Exception e) {
            log.error("Failed to send fertilization completed notification", e);
        }
    }

    @Override
    public void sendFertilizationCancelledNotification(Fertilization fertilization) {
        if (!notificationConfig.isEnabled()) {
            log.debug("Email notifications are disabled");
            return;
        }

        try {
            Parcel parcel = fertilization.getParcel();
            Context context = new Context();
            context.setVariable("parcelName", parcel.getName());
            context.setVariable("farmName", parcel.getFarm().getName());
            context.setVariable("cropName", parcel.getCrop() != null ? parcel.getCrop().getName() : "N/A");
            context.setVariable("fertilizerType", fertilization.getFertilizerType());
            context.setVariable("scheduledDate", fertilization.getScheduledDatetime().format(DATE_TIME_FORMATTER));

            String htmlContent = templateEngine.process("email/fertilization-cancelled", context);

            sendEmail(
                notificationConfig.getTo(),
                "🚫 Fertilization Cancelled - " + parcel.getName(),
                htmlContent
            );

            log.info("Sent fertilization cancelled notification for fertilization ID: {}", fertilization.getId());
        } catch (Exception e) {
            log.error("Failed to send fertilization cancelled notification", e);
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(notificationConfig.getFrom());
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

