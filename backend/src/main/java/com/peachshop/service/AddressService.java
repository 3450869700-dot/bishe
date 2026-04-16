package com.peachshop.service;

import com.peachshop.model.Address;
import com.peachshop.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AddressService {
    
    @Autowired
    private AddressRepository addressRepository;
    
    public List<Address> getAddressesByUserId(Long userId) {
        return addressRepository.findByUserIdAndIsValid(userId, 1);
    }
    
    public Address getDefaultAddress(Long userId) {
        return addressRepository.findByUserIdAndIsDefaultAndIsValid(userId, 1, 1);
    }
    
    public Address getAddressById(Long id) {
        return addressRepository.findById(id).orElse(null);
    }
    
    public Address saveAddress(Address address) {
        // 如果设置为默认地址，需要将其他默认地址取消
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            List<Address> userAddresses = addressRepository.findByUserIdAndIsValid(address.getUserId(), 1);
            for (Address addr : userAddresses) {
                if (addr.getIsDefault() != null && addr.getIsDefault() == 1) {
                    addr.setIsDefault(0);
                    addr.setUpdateTime(LocalDateTime.now());
                    addressRepository.save(addr);
                }
            }
        }
        
        if (address.getId() == null) {
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());
            address.setIsValid(1);
        } else {
            address.setUpdateTime(LocalDateTime.now());
        }
        
        return addressRepository.save(address);
    }
    
    public void deleteAddress(Long id) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null) {
            address.setIsValid(0);
            address.setUpdateTime(LocalDateTime.now());
            addressRepository.save(address);
        }
    }
}
