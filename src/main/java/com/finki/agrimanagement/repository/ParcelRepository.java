package com.finki.agrimanagement.repository;

import com.finki.agrimanagement.entity.Parcel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    List<Parcel> findByFarmId(Long farmId);

    List<Parcel> findByCropId(Long cropId);
}

