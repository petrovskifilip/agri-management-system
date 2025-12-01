package com.finki.agrimanagement.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "parcel")
public class Parcel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "area")
    private Double area;

    @Column(name = "last_irrigated_at")
    private LocalDateTime lastIrrigatedAt;

    @Column(name = "last_fertilized_at")
    private LocalDateTime lastFertilizedAt;

    @ManyToOne
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @ManyToOne
    @JoinColumn(name = "crop_id")
    private Crop crop;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Irrigation> irrigations = new ArrayList<>();

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fertilization> fertilizations = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public LocalDateTime getLastIrrigatedAt() {
        return lastIrrigatedAt;
    }

    public void setLastIrrigatedAt(LocalDateTime lastIrrigatedAt) {
        this.lastIrrigatedAt = lastIrrigatedAt;
    }

    public LocalDateTime getLastFertilizedAt() {
        return lastFertilizedAt;
    }

    public void setLastFertilizedAt(LocalDateTime lastFertilizedAt) {
        this.lastFertilizedAt = lastFertilizedAt;
    }

    public Farm getFarm() {
        return farm;
    }

    public void setFarm(Farm farm) {
        this.farm = farm;
    }

    public Crop getCrop() {
        return crop;
    }

    public void setCrop(Crop crop) {
        this.crop = crop;
    }

    public List<Irrigation> getIrrigations() {
        return irrigations;
    }

    public void setIrrigations(List<Irrigation> irrigations) {
        this.irrigations = irrigations;
    }

    public List<Fertilization> getFertilizations() {
        return fertilizations;
    }

    public void setFertilizations(List<Fertilization> fertilizations) {
        this.fertilizations = fertilizations;
    }
}

