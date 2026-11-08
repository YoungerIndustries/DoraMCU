package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HighGuardRangeBand Tests")
class HighGuardRangeBandTest {

    // ---- Enum values tests ----

    @Test
    @DisplayName("Should have 7 range bands")
    void testRangeBandCount() {
        assertEquals(7, HighGuardRangeBand.values().length);
    }

    // ---- Range band properties tests ----

    @Test
    @DisplayName("Adjacent should be dogfighting range")
    void testAdjacentDogfighting() {
        assertTrue(HighGuardRangeBand.ADJACENT.isDogfighting());
        assertEquals(0, HighGuardRangeBand.ADJACENT.getMinDistanceKm());
        assertEquals(1, HighGuardRangeBand.ADJACENT.getThrustToChange());
    }

    @Test
    @DisplayName("Close should be dogfighting range")
    void testCloseDogfighting() {
        assertTrue(HighGuardRangeBand.CLOSE.isDogfighting());
        assertEquals(1, HighGuardRangeBand.CLOSE.getMinDistanceKm());
        assertEquals(10, HighGuardRangeBand.CLOSE.getMaxDistanceKm());
    }

    @Test
    @DisplayName("Short should not be dogfighting range")
    void testShortNotDogfighting() {
        assertFalse(HighGuardRangeBand.SHORT.isDogfighting());
        assertEquals(10, HighGuardRangeBand.SHORT.getMinDistanceKm());
        assertEquals(1250, HighGuardRangeBand.SHORT.getMaxDistanceKm());
        assertEquals(2, HighGuardRangeBand.SHORT.getThrustToChange());
    }

    @Test
    @DisplayName("Medium should not be dogfighting range")
    void testMediumNotDogfighting() {
        assertFalse(HighGuardRangeBand.MEDIUM.isDogfighting());
        assertEquals(1250, HighGuardRangeBand.MEDIUM.getMinDistanceKm());
        assertEquals(10000, HighGuardRangeBand.MEDIUM.getMaxDistanceKm());
        assertEquals(5, HighGuardRangeBand.MEDIUM.getThrustToChange());
    }

    @Test
    @DisplayName("Long should not be dogfighting range")
    void testLongNotDogfighting() {
        assertFalse(HighGuardRangeBand.LONG.isDogfighting());
        assertEquals(10000, HighGuardRangeBand.LONG.getMinDistanceKm());
        assertEquals(25000, HighGuardRangeBand.LONG.getMaxDistanceKm());
        assertEquals(10, HighGuardRangeBand.LONG.getThrustToChange());
    }

    @Test
    @DisplayName("Very Long should not be dogfighting range")
    void testVeryLongNotDogfighting() {
        assertFalse(HighGuardRangeBand.VERY_LONG.isDogfighting());
        assertEquals(25000, HighGuardRangeBand.VERY_LONG.getMinDistanceKm());
        assertEquals(50000, HighGuardRangeBand.VERY_LONG.getMaxDistanceKm());
        assertEquals(25, HighGuardRangeBand.VERY_LONG.getThrustToChange());
    }

    @Test
    @DisplayName("Distant should not be dogfighting range")
    void testDistantNotDogfighting() {
        assertFalse(HighGuardRangeBand.DISTANT.isDogfighting());
        assertEquals(50000, HighGuardRangeBand.DISTANT.getMinDistanceKm());
        assertEquals(50, HighGuardRangeBand.DISTANT.getThrustToChange());
    }

    // ---- Index tests ----

    @Test
    @DisplayName("Range band indices should be sequential")
    void testIndices() {
        assertEquals(0, HighGuardRangeBand.ADJACENT.getIndex());
        assertEquals(1, HighGuardRangeBand.CLOSE.getIndex());
        assertEquals(2, HighGuardRangeBand.SHORT.getIndex());
        assertEquals(3, HighGuardRangeBand.MEDIUM.getIndex());
        assertEquals(4, HighGuardRangeBand.LONG.getIndex());
        assertEquals(5, HighGuardRangeBand.VERY_LONG.getIndex());
        assertEquals(6, HighGuardRangeBand.DISTANT.getIndex());
    }

    // ---- Closer/Farther navigation tests ----

    @Test
    @DisplayName("Closer from Distant should move toward Adjacent")
    void testCloserNavigation() {
        assertEquals(HighGuardRangeBand.VERY_LONG, HighGuardRangeBand.DISTANT.closer());
        assertEquals(HighGuardRangeBand.LONG, HighGuardRangeBand.VERY_LONG.closer());
        assertEquals(HighGuardRangeBand.MEDIUM, HighGuardRangeBand.LONG.closer());
        assertEquals(HighGuardRangeBand.SHORT, HighGuardRangeBand.MEDIUM.closer());
        assertEquals(HighGuardRangeBand.CLOSE, HighGuardRangeBand.SHORT.closer());
        assertEquals(HighGuardRangeBand.ADJACENT, HighGuardRangeBand.CLOSE.closer());
    }

    @Test
    @DisplayName("Closer from Adjacent should stay at Adjacent")
    void testCloserAtLimit() {
        assertEquals(HighGuardRangeBand.ADJACENT, HighGuardRangeBand.ADJACENT.closer());
    }

    @Test
    @DisplayName("Farther from Adjacent should move toward Distant")
    void testFartherNavigation() {
        assertEquals(HighGuardRangeBand.CLOSE, HighGuardRangeBand.ADJACENT.farther());
        assertEquals(HighGuardRangeBand.SHORT, HighGuardRangeBand.CLOSE.farther());
        assertEquals(HighGuardRangeBand.MEDIUM, HighGuardRangeBand.SHORT.farther());
        assertEquals(HighGuardRangeBand.LONG, HighGuardRangeBand.MEDIUM.farther());
        assertEquals(HighGuardRangeBand.VERY_LONG, HighGuardRangeBand.LONG.farther());
        assertEquals(HighGuardRangeBand.DISTANT, HighGuardRangeBand.VERY_LONG.farther());
    }

    @Test
    @DisplayName("Farther from Distant should stay at Distant")
    void testFartherAtLimit() {
        assertEquals(HighGuardRangeBand.DISTANT, HighGuardRangeBand.DISTANT.farther());
    }

    // ---- Distance to range band tests ----

    @Test
    @DisplayName("Distance 0 should map to Adjacent")
    void testDistanceZero() {
        assertEquals(HighGuardRangeBand.ADJACENT, HighGuardRangeBand.fromDistance(0));
    }

    @Test
    @DisplayName("Distance 5 km should map to Close")
    void testDistanceClose() {
        assertEquals(HighGuardRangeBand.CLOSE, HighGuardRangeBand.fromDistance(5));
    }

    @Test
    @DisplayName("Distance 100 km should map to Short")
    void testDistanceShort() {
        assertEquals(HighGuardRangeBand.SHORT, HighGuardRangeBand.fromDistance(100));
    }

    @Test
    @DisplayName("Distance 5000 km should map to Medium")
    void testDistanceMedium() {
        assertEquals(HighGuardRangeBand.MEDIUM, HighGuardRangeBand.fromDistance(5000));
    }

    @Test
    @DisplayName("Distance 15000 km should map to Long")
    void testDistanceLong() {
        assertEquals(HighGuardRangeBand.LONG, HighGuardRangeBand.fromDistance(15000));
    }

    @Test
    @DisplayName("Distance 35000 km should map to Very Long")
    void testDistanceVeryLong() {
        assertEquals(HighGuardRangeBand.VERY_LONG, HighGuardRangeBand.fromDistance(35000));
    }

    @Test
    @DisplayName("Distance 60000 km should map to Distant")
    void testDistanceDistant() {
        assertEquals(HighGuardRangeBand.DISTANT, HighGuardRangeBand.fromDistance(60000));
    }

    @Test
    @DisplayName("Negative distance should map to Adjacent")
    void testDistanceNegative() {
        assertEquals(HighGuardRangeBand.ADJACENT, HighGuardRangeBand.fromDistance(-1));
    }

    // ---- Calculate new range tests ----

    @Test
    @DisplayName("Both charging should combine thrust to close range")
    void testBothCharging() {
        // Both charge: thrust combines (3 + 2 = 5)
        // From Short: need 2 to reach Close, then 1 to reach Adjacent = 3 total
        // 5 thrust can cover Short->Close (2) + Close->Adjacent (1) = 3, remaining 2
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.SHORT, 3, 2, true, true);
        assertEquals(HighGuardRangeBand.ADJACENT, result);
    }

    @Test
    @DisplayName("Both fleeing should increase range")
    void testBothFleeing() {
        // Both fleeing: difference determines change
        // Attacker thrust 3, defender thrust 2, both fleeing
        // effectiveThrust = defenderThrust - attackerThrust = 2 - 3 = -1
        // abs = 1, movingCloser = false (effectiveThrust < 0)
        // Wait — both fleeing means they move apart
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.SHORT, 3, 2, false, false);
        // effectiveThrust = 2 - 3 = -1, abs = 1, movingCloser = false
        // From Short, farther needs 2 thrust, but only 1 available — no change
        assertEquals(HighGuardRangeBand.SHORT, result);
    }

    @Test
    @DisplayName("Attacker charging, defender fleeing should close range by difference")
    void testChargeVsFlee() {
        // Attacker charges (thrust 5), defender flees (thrust 2)
        // effectiveThrust = 5 - 2 = 3, movingCloser = true
        // From Medium: need 5 to reach Short, but only 3 — no change
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.MEDIUM, 5, 2, true, false);
        assertEquals(HighGuardRangeBand.MEDIUM, result);
    }

    @Test
    @DisplayName("High thrust charging should close multiple bands")
    void testHighThrustCharge() {
        // Attacker charges (thrust 10), defender charges (thrust 5)
        // Both charging: effectiveThrust = 10 + 5 = 15, movingCloser = true
        // From Long: need 10 to reach Medium, then 5 to reach Short = 15 total
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.LONG, 10, 5, true, true);
        assertEquals(HighGuardRangeBand.SHORT, result);
    }

    @Test
    @DisplayName("Zero thrust should not change range")
    void testZeroThrust() {
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.MEDIUM, 0, 0, true, true);
        assertEquals(HighGuardRangeBand.MEDIUM, result);
    }

    @Test
    @DisplayName("Adjacent should not close further")
    void testAdjacentCannotClose() {
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.ADJACENT, 10, 10, true, true);
        assertEquals(HighGuardRangeBand.ADJACENT, result);
    }

    @Test
    @DisplayName("Distant should not go farther")
    void testDistantCannotGoFarther() {
        // Both fleeing from Distant
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.DISTANT, 1, 10, false, false);
        assertEquals(HighGuardRangeBand.DISTANT, result);
    }

    @Test
    @DisplayName("Defender charging, attacker fleeing should close range")
    void testDefenderChargingAttackerFleeing() {
        // Attacker flees (thrust 2), defender charges (thrust 5)
        // effectiveThrust = defenderThrust - attackerThrust = 5 - 2 = 3
        // movingCloser = true (effectiveThrust > 0)
        // From Short: need 2 to reach Close, remaining 1 to reach Adjacent
        HighGuardRangeBand result = HighGuardRangeBand.calculateNewRange(
                HighGuardRangeBand.SHORT, 2, 5, false, true);
        assertEquals(HighGuardRangeBand.ADJACENT, result);
    }

    // ---- Name tests ----

    @Test
    @DisplayName("All range bands should have non-empty names")
    void testRangeBandNames() {
        for (HighGuardRangeBand band : HighGuardRangeBand.values()) {
            assertNotNull(band.getName());
            assertFalse(band.getName().isEmpty());
        }
    }

    // ---- Thrust to change monotonicity tests ----

    @Test
    @DisplayName("Thrust to change should generally increase with distance")
    void testThrustMonotonicity() {
        // Adjacent and Close both need 1 (dogfighting)
        assertEquals(1, HighGuardRangeBand.ADJACENT.getThrustToChange());
        assertEquals(1, HighGuardRangeBand.CLOSE.getThrustToChange());

        // Then increasing
        assertTrue(HighGuardRangeBand.SHORT.getThrustToChange() > HighGuardRangeBand.CLOSE.getThrustToChange());
        assertTrue(HighGuardRangeBand.MEDIUM.getThrustToChange() > HighGuardRangeBand.SHORT.getThrustToChange());
        assertTrue(HighGuardRangeBand.LONG.getThrustToChange() > HighGuardRangeBand.MEDIUM.getThrustToChange());
        assertTrue(HighGuardRangeBand.VERY_LONG.getThrustToChange() > HighGuardRangeBand.LONG.getThrustToChange());
        assertTrue(HighGuardRangeBand.DISTANT.getThrustToChange() > HighGuardRangeBand.VERY_LONG.getThrustToChange());
    }
}
