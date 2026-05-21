package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.ApprovalStatus;
import com.RentKaro.RentKaro.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByHost_Id(Long hostId);

    List<Property> findByApprovalStatus(ApprovalStatus approvalStatus);

    void deleteByHost_Id(Long hostId);

    List<Property> findByApprovalStatusAndCityContainingIgnoreCase(ApprovalStatus status, String city);

    List<Property> findByApprovalStatusAndCountryContainingIgnoreCase(ApprovalStatus status, String country);

    long countByApprovalStatus(ApprovalStatus status);
}
