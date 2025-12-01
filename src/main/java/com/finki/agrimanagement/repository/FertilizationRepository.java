package com.finki.agrimanagement.repository;

import com.finki.agrimanagement.entity.Fertilization;
import com.finki.agrimanagement.enums.FertilizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FertilizationRepository extends JpaRepository<Fertilization, Long> {

    List<Fertilization> findByParcelId(Long parcelId);

    List<Fertilization> findByStatus(FertilizationStatus status);

    List<Fertilization> findByStatusAndScheduledDatetimeBefore(FertilizationStatus status, LocalDateTime dateTime);

    List<Fertilization> findByParcelIdAndStatus(Long parcelId, FertilizationStatus status);
}

