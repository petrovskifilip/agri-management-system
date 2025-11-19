package com.finki.agrimanagement.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crop")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private Integer irrigationFrequencyDays;

    @Column
    private Integer fertilizationFrequencyDays;

    @OneToMany(mappedBy = "crop", cascade = CascadeType.ALL)
    private List<Parcel> parcels = new ArrayList<>();

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

    public Integer getIrrigationFrequencyDays() {
        return irrigationFrequencyDays;
    }

    public void setIrrigationFrequencyDays(Integer irrigationFrequencyDays) {
        this.irrigationFrequencyDays = irrigationFrequencyDays;
    }

    public Integer getFertilizationFrequencyDays() {
        return fertilizationFrequencyDays;
    }

    public void setFertilizationFrequencyDays(Integer fertilizationFrequencyDays) {
        this.fertilizationFrequencyDays = fertilizationFrequencyDays;
    }

    public List<Parcel> getParcels() {
        return parcels;
    }

    public void setParcels(List<Parcel> parcels) {
        this.parcels = parcels;
    }

    // Helper method for derived property
    public int getParcelCount() {
        return parcels != null ? parcels.size() : 0;
    }
}

