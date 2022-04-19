package base.calculator

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TestThree
 *
 * @author tiankang
 * @description:
 * @date :2022/4/19 15:02
 */
class TestThree {

    @Test
    fun testCalculate() {
        val calculator = Three()
        val res1 = calculator.calculate("1+2")
        assertEquals("3", res1)
    }
}