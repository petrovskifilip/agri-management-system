package com.finki.agrimanagement.entity;

import com.finki.agrimanagement.enums.FertilizationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "fertilization")
public class Fertilization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcel parcel;

    @Column(name = "scheduled_datetime", nullable = false)
    private LocalDateTime scheduledDatetime;

    @Column(name = "fertilizer_type")
    private String fertilizerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private FertilizationStatus status;

    @Column(name = "completed_datetime")
    private LocalDateTime completedDatetime;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Parcel getParcel() {
        return parcel;
    }

    public void setParcel(Parcel parcel) {
        this.parcel = parcel;
    }

    public LocalDateTime getScheduledDatetime() {
        return scheduledDatetime;
    }

    public void setScheduledDatetime(LocalDateTime scheduledDatetime) {
        this.scheduledDatetime = scheduledDatetime;
    }

    public String getFertilizerType() {
        return fertilizerType;
    }

    public void setFertilizerType(String fertilizerType) {
        this.fertilizerType = fertilizerType;
    }

    public FertilizationStatus getStatus() {
        return status;
    }

    public void setStatus(FertilizationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedDatetime() {
        return completedDatetime;
    }

    public void setCompletedDatetime(LocalDateTime completedDatetime) {
        this.completedDatetime = completedDatetime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

