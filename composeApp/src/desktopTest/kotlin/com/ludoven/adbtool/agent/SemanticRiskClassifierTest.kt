package com.ludoven.adbtool.agent

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticRiskClassifierTest {

    @Test
    fun `payment and password operations are blocked`() {
        val payAssessment = SemanticRiskClassifier.evaluate("click", "确认支付 ¥99.00", "PAYMENT")
        assertEquals(AgentRiskLevel.BLOCKED, payAssessment.level)

        val inferredPay = SemanticRiskClassifier.evaluate("click", "立即付款")
        assertEquals(AgentRiskLevel.BLOCKED, inferredPay.level)

        val passwordAssessment = SemanticRiskClassifier.evaluate("type", "输入锁屏密码", "PASSWORD_ENTRY")
        assertEquals(AgentRiskLevel.BLOCKED, passwordAssessment.level)
    }

    @Test
    fun `messaging and publishing require confirmation`() {
        val sendAssessment = SemanticRiskClassifier.evaluate("click", "发送", "SEND_MESSAGE")
        assertEquals(AgentRiskLevel.CONFIRMATION_REQUIRED, sendAssessment.level)
        assertTrue(sendAssessment.reason.contains("发送消息"))

        val postAssessment = SemanticRiskClassifier.evaluate("click", "发布动态")
        assertEquals(AgentRiskLevel.CONFIRMATION_REQUIRED, postAssessment.level)
        assertTrue(postAssessment.reason.contains("发帖"))
    }

    @Test
    fun `destructive operations require confirmation`() {
        val deleteAssessment = SemanticRiskClassifier.evaluate("click", "清除所有数据")
        assertEquals(AgentRiskLevel.CONFIRMATION_REQUIRED, deleteAssessment.level)
        assertTrue(deleteAssessment.reason.contains("删除内容或清除数据"))
    }

    @Test
    fun `normal navigation is safe`() {
        val scrollAssessment = SemanticRiskClassifier.evaluate("swipe", "向上滑动")
        assertEquals(AgentRiskLevel.SAFE, scrollAssessment.level)

        val backAssessment = SemanticRiskClassifier.evaluate("key", "返回键")
        assertEquals(AgentRiskLevel.SAFE, backAssessment.level)

        val navAssessment = SemanticRiskClassifier.evaluate("click", "显示与亮度")
        assertEquals(AgentRiskLevel.SAFE, navAssessment.level)
    }
}
