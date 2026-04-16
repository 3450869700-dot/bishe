package com.peachshop.repository;

import com.peachshop.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUserIdAndIsValid(Long userId, Integer isValid);
    
    Address findByUserIdAndIsDefaultAndIsValid(Long userId, Integer isDefault, Integer isValid);
}
