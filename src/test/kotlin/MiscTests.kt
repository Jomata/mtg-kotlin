import org.json.simple.JSONObject
import org.junit.Test
import kotlin.test.assertEquals

//Container for tests

class MiscTests {
    companion object {
        @Test
        fun manaCostTest() {
            val testCard = MTGCard(
                name="Test",
                manaCost="{1}{U}{R}",
                oracle = "Test",
                types = listOf("Test"),
                json = JSONObject(),
                backside = null,
            )

            assertEquals(3,testCard.cmc)
        }
    }
}