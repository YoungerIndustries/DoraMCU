package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByStatusIgnoreCase(String status);
}
