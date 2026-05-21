package com.RentKaro.RentKaro.repository;

import com.RentKaro.RentKaro.model.ApprovalStatus;
import com.RentKaro.RentKaro.model.Property;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends MongoRepository<Property, String> {

    List<Property> findByHostId(String hostId);

    List<Property> findByApprovalStatus(ApprovalStatus approvalStatus);

    void deleteByHostId(String hostId);

    List<Property> findByApprovalStatusAndCityContainingIgnoreCase(ApprovalStatus status, String city);

    List<Property> findByApprovalStatusAndCountryContainingIgnoreCase(ApprovalStatus status, String country);

    long countByApprovalStatus(ApprovalStatus status);
}
