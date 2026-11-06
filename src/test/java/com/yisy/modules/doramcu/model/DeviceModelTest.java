package com.yisy.modules.doramcu.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Device Model Tests")
class DeviceModelTest {

    // ---- Constructors ----

    @Test
    @DisplayName("Default constructor - should create empty device")
    void defaultConstructor_shouldCreateEmptyDevice() {
        Device device = new Device();

        assertNull(device.getId());
        assertNull(device.getName());
        assertNull(device.getStatus());
        assertNull(device.getLocation());
    }

    @Test
    @DisplayName("Parameterized constructor - should set all fields")
    void parameterizedConstructor_shouldSetAllFields() {
        Device device = new Device(1L, "Sensor-001", "active", "Building A");

        assertEquals(1L, device.getId());
        assertEquals("Sensor-001", device.getName());
        assertEquals("active", device.getStatus());
        assertEquals("Building A", device.getLocation());
    }

    // ---- Getters and Setters ----

    @Test
    @DisplayName("setId - should update id")
    void setId_shouldUpdateId() {
        Device device = new Device();
        device.setId(42L);

        assertEquals(42L, device.getId());
    }

    @Test
    @DisplayName("setName - should update name")
    void setName_shouldUpdateName() {
        Device device = new Device();
        device.setName("Controller-001");

        assertEquals("Controller-001", device.getName());
    }

    @Test
    @DisplayName("setStatus - should update status")
    void setStatus_shouldUpdateStatus() {
        Device device = new Device();
        device.setStatus("inactive");

        assertEquals("inactive", device.getStatus());
    }

    @Test
    @DisplayName("setLocation - should update location")
    void setLocation_shouldUpdateLocation() {
        Device device = new Device();
        device.setLocation("Server Room");

        assertEquals("Server Room", device.getLocation());
    }

    @Test
    @DisplayName("setLocation - should allow null location")
    void setLocation_shouldAllowNull() {
        Device device = new Device(1L, "Sensor", "active", "Building A");
        device.setLocation(null);

        assertNull(device.getLocation());
    }

    // ---- Multiple updates ----

    @Test
    @DisplayName("Multiple setters - should update all fields")
    void multipleSetters_shouldUpdateAllFields() {
        Device device = new Device(1L, "Old", "inactive", "Old Location");
        device.setName("New");
        device.setStatus("active");
        device.setLocation("New Location");

        assertEquals(1L, device.getId());
        assertEquals("New", device.getName());
        assertEquals("active", device.getStatus());
        assertEquals("New Location", device.getLocation());
    }
}
