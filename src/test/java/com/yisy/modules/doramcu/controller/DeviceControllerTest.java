package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.config.MetricsConfig;
import com.yisy.modules.doramcu.model.Device;
import com.yisy.modules.doramcu.repository.DeviceRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceController Tests")
@MockitoSettings(strictness = Strictness.LENIENT)
class DeviceControllerTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private Counter deviceCreateCounter;

    @Mock
    private Counter deviceUpdateCounter;

    @Mock
    private Counter deviceDeleteCounter;

    @Mock
    private Counter deviceFetchCounter;

    @Mock
    private Timer deviceOperationTimer;

    private DeviceController controller;

    @BeforeEach
    void setUp() {
        when(metricsConfig.getDeviceCreateCounter()).thenReturn(deviceCreateCounter);
        when(metricsConfig.getDeviceUpdateCounter()).thenReturn(deviceUpdateCounter);
        when(metricsConfig.getDeviceDeleteCounter()).thenReturn(deviceDeleteCounter);
        when(metricsConfig.getDeviceFetchCounter()).thenReturn(deviceFetchCounter);
        when(metricsConfig.getDeviceOperationTimer()).thenReturn(deviceOperationTimer);
        controller = new DeviceController(deviceRepository, metricsConfig);
    }

    // ---- GET All Devices ----

    @Test
    @DisplayName("getAllDevices - should return empty list when no devices exist")
    void getAllDevices_shouldReturnEmptyList() {
        when(deviceRepository.findAll()).thenReturn(Collections.emptyList());

        List<Device> devices = controller.getAllDevices();

        assertNotNull(devices);
        assertTrue(devices.isEmpty());
        verify(deviceRepository).findAll();
        verify(deviceFetchCounter).increment();
    }

    @Test
    @DisplayName("getAllDevices - should return all devices")
    void getAllDevices_shouldReturnAllDevices() {
        Device device1 = new Device(1L, "Laptop", "active", "Office");
        Device device2 = new Device(2L, "Phone", "inactive", "Home");
        when(deviceRepository.findAll()).thenReturn(List.of(device1, device2));

        List<Device> devices = controller.getAllDevices();

        assertEquals(2, devices.size());
        assertEquals("Laptop", devices.get(0).getName());
        assertEquals("Phone", devices.get(1).getName());
        verify(deviceFetchCounter).increment();
    }

    // ---- GET Device By ID ----

    @Test
    @DisplayName("getDeviceById - should return 200 OK when device exists")
    void getDeviceById_shouldReturnOk() {
        Device device = new Device(1L, "Laptop", "active", "Office");
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

        ResponseEntity<Device> response = controller.getDeviceById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Laptop", response.getBody().getName());
        assertEquals(1L, response.getBody().getId());
        verify(deviceFetchCounter).increment();
    }

    @Test
    @DisplayName("getDeviceById - should return 404 when device not found")
    void getDeviceById_shouldReturn404() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Device> response = controller.getDeviceById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    // ---- POST Create Device ----

    @Test
    @DisplayName("createDevice - should return 201 Created with saved device")
    void createDevice_shouldReturnCreated() {
        Device requestDevice = new Device(null, "Server", "active", "Datacenter");
        Device savedDevice = new Device(1L, "Server", "active", "Datacenter");
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        ResponseEntity<Device> response = controller.createDevice(requestDevice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Server", response.getBody().getName());
        verify(deviceRepository).save(any(Device.class));
        verify(deviceCreateCounter).increment();
    }

    @Test
    @DisplayName("createDevice - should set default status to inactive when empty")
    void createDevice_shouldSetDefaultStatus() {
        Device requestDevice = new Device(null, "Server", "", "Datacenter");
        Device savedDevice = new Device(1L, "Server", "inactive", "Datacenter");
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        ResponseEntity<Device> response = controller.createDevice(requestDevice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        // The controller sets status to "inactive" when empty before saving
        verify(deviceRepository).save(argThat(device -> "inactive".equals(device.getStatus())));
    }

    @Test
    @DisplayName("createDevice - should set default status to inactive when null")
    void createDevice_shouldSetDefaultStatusWhenNull() {
        Device requestDevice = new Device(null, "Server", null, "Datacenter");
        Device savedDevice = new Device(1L, "Server", "inactive", "Datacenter");
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        ResponseEntity<Device> response = controller.createDevice(requestDevice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(deviceRepository).save(argThat(device -> "inactive".equals(device.getStatus())));
    }

    @Test
    @DisplayName("createDevice - should preserve non-empty status")
    void createDevice_shouldPreserveStatus() {
        Device requestDevice = new Device(null, "Server", "active", "Datacenter");
        Device savedDevice = new Device(1L, "Server", "active", "Datacenter");
        when(deviceRepository.save(any(Device.class))).thenReturn(savedDevice);

        ResponseEntity<Device> response = controller.createDevice(requestDevice);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(deviceRepository).save(argThat(device -> "active".equals(device.getStatus())));
    }

    // ---- PUT Update Device ----

    @Test
    @DisplayName("updateDevice - should return 200 OK when device exists")
    void updateDevice_shouldReturnOk() {
        Device existing = new Device(1L, "Old Name", "inactive", "Old Location");
        Device requestDevice = new Device(null, "New Name", "active", "New Location");
        Device updatedDevice = new Device(1L, "New Name", "active", "New Location");

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);

        ResponseEntity<Device> response = controller.updateDevice(1L, requestDevice);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Name", response.getBody().getName());
        assertEquals("active", response.getBody().getStatus());
        assertEquals("New Location", response.getBody().getLocation());
        verify(deviceUpdateCounter).increment();
    }

    @Test
    @DisplayName("updateDevice - should return 404 when device not found")
    void updateDevice_shouldReturn404() {
        Device requestDevice = new Device(null, "New Name", "active", "New Location");
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Device> response = controller.updateDevice(99L, requestDevice);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(deviceRepository, never()).save(any());
    }

    // ---- DELETE Device ----

    @Test
    @DisplayName("deleteDevice - should return 204 No Content when device exists")
    void deleteDevice_shouldReturnNoContent() {
        when(deviceRepository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteDevice(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(deviceRepository).deleteById(1L);
        verify(deviceDeleteCounter).increment();
    }

    @Test
    @DisplayName("deleteDevice - should return 404 when device not found")
    void deleteDevice_shouldReturn404() {
        when(deviceRepository.existsById(99L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteDevice(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deviceRepository, never()).deleteById(anyLong());
    }

    // ---- Search By Status ----

    @Test
    @DisplayName("searchByStatus - should return devices matching status")
    void searchByStatus_shouldReturnMatchingDevices() {
        Device device1 = new Device(1L, "Laptop", "active", "Office");
        Device device2 = new Device(2L, "Desktop", "active", "Office");
        when(deviceRepository.findByStatusIgnoreCase("active")).thenReturn(List.of(device1, device2));

        List<Device> devices = controller.searchByStatus("active");

        assertEquals(2, devices.size());
        assertEquals("active", devices.get(0).getStatus());
        assertEquals("active", devices.get(1).getStatus());
        verify(deviceFetchCounter).increment();
    }

    @Test
    @DisplayName("searchByStatus - should be case insensitive")
    void searchByStatus_shouldBeCaseInsensitive() {
        Device device = new Device(1L, "Laptop", "active", "Office");
        when(deviceRepository.findByStatusIgnoreCase("ACTIVE")).thenReturn(List.of(device));

        List<Device> devices = controller.searchByStatus("ACTIVE");

        assertEquals(1, devices.size());
        verify(deviceRepository).findByStatusIgnoreCase("ACTIVE");
    }

    @Test
    @DisplayName("searchByStatus - should return empty list when no matches")
    void searchByStatus_shouldReturnEmptyList() {
        when(deviceRepository.findByStatusIgnoreCase("unknown")).thenReturn(Collections.emptyList());

        List<Device> devices = controller.searchByStatus("unknown");

        assertNotNull(devices);
        assertTrue(devices.isEmpty());
    }
}
