package com.finki.agrimanagement.repository;

import com.finki.agrimanagement.entity.Irrigation;
import com.finki.agrimanagement.enums.IrrigationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IrrigationRepository extends JpaRepository<Irrigation, Long> {

    List<Irrigation> findByParcelId(Long parcelId);

    List<Irrigation> findByStatus(IrrigationStatus status);

    List<Irrigation> findByStatusAndScheduledDatetimeAfter(IrrigationStatus status, LocalDateTime dateTime);

    List<Irrigation> findByStatusAndScheduledDatetimeBefore(IrrigationStatus status, LocalDateTime dateTime);

    List<Irrigation> findByParcelIdAndStatus(Long parcelId, IrrigationStatus status);
}
