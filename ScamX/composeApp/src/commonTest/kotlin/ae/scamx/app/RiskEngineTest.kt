package ae.scamx.app

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RiskEngineTest {
    @Test fun taskRechargeIsHighRisk() {
        val result = RiskEngine.check("Part-time task job. Recharge your account to unlock commission.")
        assertEquals(RiskLevel.HIGH, result.level)
    }

    @Test fun bankOtpThreatIsHighRisk() {
        val result = RiskEngine.check("Bank security: send your OTP immediately or your account will be suspended.")
        assertEquals(RiskLevel.HIGH, result.level)
    }

    @Test fun safetyAdviceDoesNotTriggerOtpRequest() {
        val result = RiskEngine.check("Never share your OTP or password. Contact the bank using its official number.")
        assertTrue(result.reasons.none { it.titleEn == "Requests sensitive information" })
    }

    @Test fun shortenedDeliveryLinkIsHighRisk() {
        val result = RiskEngine.check("Your parcel is held. Pay a delivery fee at https://bit.ly/fee now.")
        assertEquals(RiskLevel.HIGH, result.level)
    }

    @Test fun ordinaryReminderIsLowRisk() {
        val result = RiskEngine.check("Reminder: your clinic appointment is tomorrow at 10:00.")
        assertEquals(RiskLevel.LOW, result.level)
    }

    @Test fun communityPolicyFindsLinksAndNumbers() {
        val result = checkCommunitySafety("This happened at https://example.com and the number was 0501234567.")
        assertTrue(!result.acceptable)
        assertTrue(result.reasonsEn.size >= 2)
    }
}
