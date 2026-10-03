package com.purgatorio.core.alma;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AlmaRulesTest {
	private static final double EPS = 1e-9;

	@Test
	void bonusIsLinearAtKeyPoints() {
		assertEquals(1.0, AlmaRules.damageMultiplier(AlmaRules.fromPoints(0)), EPS);
		assertEquals(1.025, AlmaRules.damageMultiplier(AlmaRules.fromPoints(25)), EPS);
		assertEquals(1.05, AlmaRules.damageMultiplier(AlmaRules.fromPoints(50)), EPS);
		assertEquals(1.075, AlmaRules.damageMultiplier(AlmaRules.fromPoints(75)), EPS);
		assertEquals(1.10, AlmaRules.damageMultiplier(AlmaRules.fromPoints(100)), EPS);
	}

	@Test
	void bonusNeverExceedsTenPercentEvenWithAbsurdInput() {
		assertTrue(AlmaRules.damageMultiplier(Integer.MAX_VALUE) <= 1.10 + EPS);
		assertTrue(AlmaRules.damageMultiplier(AlmaRules.MAX + 1) <= 1.10 + EPS);
		assertTrue(AlmaRules.damageMultiplier(Integer.MIN_VALUE) >= 1.0 - EPS);
		for (int c = 0; c <= AlmaRules.MAX; c += 7) {
			assertTrue(AlmaRules.damageMultiplier(c) <= 1.10 + EPS, "bonus > 10% con " + c);
		}
	}

	@Test
	void addSaturatesAtBothEnds() {
		assertEquals(AlmaRules.MAX, AlmaRules.add(AlmaRules.MAX, 1));
		assertEquals(AlmaRules.MAX, AlmaRules.add(9990, 1_000_000_000_000L));
		assertEquals(0, AlmaRules.add(0, -1));
		assertEquals(0, AlmaRules.add(500, -1_000_000_000_000L));
		assertEquals(1500, AlmaRules.add(1000, 500));
	}

	@Test
	void clampAndFromPointsStayInRange() {
		assertEquals(0, AlmaRules.clamp(-5));
		assertEquals(AlmaRules.MAX, AlmaRules.clamp(Long.MAX_VALUE));
		assertEquals(AlmaRules.MAX, AlmaRules.fromPoints(5000));
		assertEquals(0, AlmaRules.fromPoints(-3));
	}

	@Test
	void deathLosesThirtyPercent() {
		assertEquals(AlmaRules.fromPoints(70), AlmaRules.afterDeath(AlmaRules.fromPoints(100)));
		assertEquals(AlmaRules.fromPoints(49), AlmaRules.afterDeath(AlmaRules.fromPoints(70)));
		assertEquals(3430, AlmaRules.afterDeath(AlmaRules.fromPoints(49)));
		assertEquals(0, AlmaRules.afterDeath(0));
	}

	@Test
	void repeatedDeathsNeverReachNegativeAndShrinkLoss() {
		int a = AlmaRules.MAX;
		int previousLoss = Integer.MAX_VALUE;
		for (int i = 0; i < 40; i++) {
			int loss = AlmaRules.lostOnDeath(a);
			assertTrue(loss <= previousLoss, "la perdida debe decrecer");
			previousLoss = loss;
			a = AlmaRules.afterDeath(a);
			assertTrue(a >= 0);
		}
	}

	@Test
	void lostPlusKeptEqualsOriginal() {
		for (int c = 0; c <= AlmaRules.MAX; c += 13) {
			assertEquals(c, AlmaRules.afterDeath(c) + AlmaRules.lostOnDeath(c));
		}
	}

	@Test
	void spendRequiresEnoughAlmaAndRejectsNegativeCost() {
		assertTrue(AlmaRules.canSpend(AlmaRules.fromPoints(30), AlmaRules.fromPoints(30)));
		assertFalse(AlmaRules.canSpend(AlmaRules.fromPoints(29), AlmaRules.fromPoints(30)));
		assertFalse(AlmaRules.canSpend(AlmaRules.fromPoints(80), -1));
	}

	@Test
	void displayMatchesBar() {
		assertEquals(0, AlmaRules.displayLevel(0));
		assertEquals(0.0F, AlmaRules.displayProgress(0), 0F);
		assertEquals(50, AlmaRules.displayLevel(AlmaRules.fromPoints(50)));
		assertEquals(0.5F, AlmaRules.displayProgress(AlmaRules.fromPoints(50) + 50), 1e-6F);
		assertEquals(100, AlmaRules.displayLevel(AlmaRules.MAX));
		assertEquals(1.0F, AlmaRules.displayProgress(AlmaRules.MAX), 0F);
	}
}
