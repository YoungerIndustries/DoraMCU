package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.model.Device;
import com.yisy.modules.doramcu.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private static final Logger log = LoggerFactory.getLogger(DeviceController.class);

    private final DeviceRepository deviceRepository;
    private final MetricsConfig metricsConfig;

    public DeviceController(DeviceRepository deviceRepository, MetricsConfig metricsConfig) {
        this.deviceRepository = deviceRepository;
        this.metricsConfig = metricsConfig;
    }

    @GetMapping
    @Cacheable(value = "devices", key = "'all'")
    public List<Device> getAllDevices() {
        log.debug("Fetching all devices from database");
        long start = System.nanoTime();
        List<Device> devices = deviceRepository.findAll();
        metricsConfig.getDeviceOperationTimer().record(Duration.ofNanos(System.nanoTime() - start));
        metricsConfig.getDeviceFetchCounter().increment();
        log.debug("Loaded {} devices from database", devices.size());
        return devices;
    }

    @GetMapping("/{id}")
    @Cacheable(value = "devices", key = "#id")
    public ResponseEntity<Device> getDeviceById(@PathVariable Long id) {
        log.debug("Fetching device with ID: {} from database", id);
        metricsConfig.getDeviceFetchCounter().increment();
        return deviceRepository.findById(id)
                .map(device -> {
                    log.debug("Loaded device from database: {} ({})", device.getName(), id);
                    return ResponseEntity.ok(device);
                })
                .orElseGet(() -> {
                    log.warn("Device not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    @CacheEvict(value = "devices", allEntries = true)
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        log.debug("Creating new device: {}", device.getName());
        if (device.getStatus() == null || device.getStatus().isEmpty()) {
            device.setStatus("inactive");
        }
        long start = System.nanoTime();
        Device saved = deviceRepository.save(device);
        metricsConfig.getDeviceOperationTimer().record(Duration.ofNanos(System.nanoTime() - start));
        metricsConfig.getDeviceCreateCounter().increment();
        log.debug("Device created successfully with ID: {}, cache evicted", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = "devices", allEntries = true)
    public ResponseEntity<Device> updateDevice(@PathVariable Long id, @RequestBody Device device) {
        log.debug("Updating device with ID: {}", id);
        return deviceRepository.findById(id)
                .map(existing -> {
                    existing.setName(device.getName());
                    existing.setStatus(device.getStatus());
                    existing.setLocation(device.getLocation());
                    Device updated = deviceRepository.save(existing);
                    metricsConfig.getDeviceUpdateCounter().increment();
                    log.debug("Device updated successfully: {} ({}), cache evicted", updated.getName(), id);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> {
                    log.warn("Device not found for update with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "devices", allEntries = true)
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        log.debug("Deleting device with ID: {}", id);
        if (!deviceRepository.existsById(id)) {
            log.warn("Device not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        deviceRepository.deleteById(id);
        metricsConfig.getDeviceDeleteCounter().increment();
        log.debug("Device deleted successfully with ID: {}, cache evicted", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Cacheable(value = "devices", key = "'search:' + #status")
    public List<Device> searchByStatus(@RequestParam String status) {
        log.debug("Searching devices by status: {} from database", status);
        List<Device> devices = deviceRepository.findByStatusIgnoreCase(status);
        metricsConfig.getDeviceFetchCounter().increment();
        log.debug("Loaded {} devices with status: {} from database", devices.size(), status);
        return devices;
    }
}
