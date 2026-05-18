package analysis

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LlmClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    fun ask(prompt: String): ArchitectureReport {

        val json = """
        {
          "model": "llama3",
          "prompt": ${JSONObject.quote(buildPrompt(prompt))},
          "stream": false
        }
        """.trimIndent()

        val body = json.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("http://localhost:11434/api/generate")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val raw = response.body?.string()
            ?: error("Empty response")

        val content = JSONObject(raw).getString("response")

        return parse(content)
    }

    private fun buildPrompt(input: String): String =
        """
You are a senior software architect.

Return ONLY valid JSON.

Format strictly:

{
  "summary": "...",
  "problems": ["..."],
  "suggestions": ["..."],
  "dataFlows": ["A -> B -> C"]
}

Tasks:
1. Detect strong coupling
2. Find architecture problems
3. Suggest improvements
4. Describe data flow between classes as chains (A -> B -> C)

$input
""".trimIndent()

    private fun parse(text: String): ArchitectureReport {
        return try {

            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}")

            val rawJson = if (jsonEnd != -1) {
                text.substring(jsonStart, jsonEnd + 1)
            } else {
                text.substring(jsonStart) + "}"
            }

            val obj = JSONObject(rawJson)

            ArchitectureReport(
                summary = obj.optString("summary"),
                problems = obj.optJSONArray("problems")?.toStringList() ?: emptyList(),
                suggestions = obj.optJSONArray("suggestions")?.toStringList() ?: emptyList(),
                couplings = emptyList(),
                dataFlows = obj.optJSONArray("dataFlows")?.toStringList() ?: emptyList()
            )

        } catch (e: Exception) {
            println("⚠️ Invalid JSON from LLM:")
            println(text)

            ArchitectureReport(
                summary = "LLM response parsing failed",
                problems = emptyList(),
                suggestions = emptyList(),
                couplings = emptyList(),
                dataFlows = emptyList()
            )
        }
    }

    private fun org.json.JSONArray.toStringList(): List<String> =
        (0 until length()).mapNotNull {
            optString(it, null)
        }

    private fun parseCouplings(array: org.json.JSONArray): List<Coupling> =
        (0 until array.length()).mapNotNull { i ->
            val value = array.get(i)

            when (value) {
                is org.json.JSONObject -> {
                    val classes = value.optJSONArray("classes") ?: return@mapNotNull null
                    if (classes.length() != 2) return@mapNotNull null

                    Coupling(
                        from = classes.getString(0),
                        to = classes.getString(1),
                        type = value.optString("type", "unknown")
                    )
                }

                is String -> {
                    // fallback если LLM вернул строку
                    val parts = value.split("->")
                    if (parts.size == 2) {
                        Coupling(
                            from = parts[0].trim(),
                            to = parts[1].trim(),
                            type = "unknown"
                        )
                    } else null
                }

                else -> null
            }
        }
}