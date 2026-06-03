package com.example.data.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object GeminiClient {
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiClient", "API Key is empty or placeholder! Simulating AI response.")
            return@withContext getSimulatedResponse(prompt)
        }

        try {
            val requestJson = JSONObject()
            
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstObj)
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e("GeminiClient", "Unsuccessful API call: ${response.code} $errBody")
                    return@withContext "Error: Request failed with code ${response.code}. Fallback:\n\n${getSimulatedResponse(prompt)}"
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text") ?: "No response components generated."
                        }
                    }
                }
                "Response format was unrecognized. Raw: $responseBodyStr"
            }
        } catch (e: Exception) {
            Log.e("GeminiClient", "Exception during Gemini Call", e)
            "Error querying AI Coach: ${e.message}. Simulation Mode activated:\n\n${getSimulatedResponse(prompt)}"
        }
    }

    private fun getSimulatedResponse(prompt: String): String {
        return when {
            prompt.contains("categorize", ignoreCase = true) -> """
                {
                  "category": "Study",
                  "priorityScore": 90,
                  "estimatedDurationMinutes": 60,
                  "reason": "This brain dump relates directly to student syllabus preparation and tracking."
                }
            """.trimIndent()
            
            prompt.contains("decode", ignoreCase = true) || prompt.contains("question", ignoreCase = true) -> """
                ### Question Analysis

                **Subject:** Accountancy (Commerce)
                **Chapter:** Recording of Transactions - I (Journal)
                **Topic:** Journal Entry Practice
                **Difficulty:** Medium

                #### What is the examiner asking?
                The question asks to record a credit purchase transaction: "Purchased Goods from Ramesh for Rs. 50,000 at 10% Trade Discount and 2% Cash Discount. Half the payment made immediately."
                This tests your understanding of trade discounts vs. cash discounts, and how to account for partial payments.

                #### Important Concepts
                *   **Trade Discount:** This discount is calculated on the list price and is NOT recorded in ledger books (only net price is recorded). List Price Rs. 50000 - 10% (Rs. 5000) = Rs. 45000 Purchases Value.
                *   **Cash Discount:** This discount is allowed ONLY on the payment actually received inside the cash discount period. Here, half is paid: Rs. 22,500. Cash discount: 2% of Rs. 22,500 = Rs. 450.

                #### Step-by-Step Solving Approach
                1.  **Calculate cost after Trade Discount:** Rs. 50,000 - 10% = Rs. 45,000.
                2.  **Determine partial payment amount:** Half of Rs. 45,000 = Rs. 22,500 is paid; half (Rs. 22,500) remains outstanding on Ramesh's debt.
                3.  **Apply 2% Cash Discount to paid portion:** Rs. 22,500 * 2% = Rs. 450.
                4.  **Actual Cash Paid:** Rs. 22,500 - Rs. 450 = Rs. 22,050.
                5.  **Draft Journal Entry:**
                    *   Debit: **Purchases Account** — Rs. 45,000
                    *   Credit: **Cash Account** — Rs. 22,050
                    *   Credit: **Discount Received** — Rs. 450
                    *   Credit: **Ramesh A/c (Creditor)** — Rs. 22,500

                #### Common Mistakes
                *   Calculating the cash discount on the entire list price of Rs. 50,000.
                *   Entering trade discount as a separate line item. Remember, Trade Discount is adjusted directly before journaling!

                #### Similar Questions
                1.  "Sold goods to Suresh for Rs. 20,000 at 5% Cash Discount, half received in Cash."
                2.  Explain why trade discount is not shown separately in accounting ledgers.
            """.trimIndent()
            
            prompt.contains("how do you feel", ignoreCase = true) || prompt.contains("energy", ignoreCase = true) || prompt.contains("feel", ignoreCase = true) -> """
                Based on your **Tired** energy level, your agenda is updated for safe recovery:
                
                *   **Difficult Calculations Deferred:** Journal Entry practice is moved to tomorrow morning.
                *   **Easy Spaced Revision Assigned:** Spend 15 minutes reviewing the interactive flashcards of Accounts Terminology.
                *   **Symmetry and Pace:** Balance is key to stopping procrastination. Let's start slow and rest!
            """.trimIndent()
            
            prompt.contains("emergency", ignoreCase = true) -> """
                ⚡ **EXAM EMERGENCY ENGINE: ON** ⚡
                The upcoming exams are inside 30 days. Action plan applied:
                
                1.  **Double-down on Accountancy:** Frequency of spaced repetitions increased from weekly to once every 2 days.
                2.  **Mock Test Drill:** Practice mock questions from Accountancy (Journal entries and Ledger columns) every Friday and Sunday.
                3.  **Personalized Revision Checklist:** Review formula lists and accounting standards notes daily.
            """.trimIndent()
            
            prompt.contains("search", ignoreCase = true) || prompt.contains("rag", ignoreCase = true) || prompt.contains("notes", ignoreCase = true) || prompt.contains("document", ignoreCase = true) -> """
                ### NotebookLM Grounded Response

                **Source Name:** NCERT_Accounts_Ch1_Intro.pdf
                **Topic:** Objectives and Qualitative Characteristics of Accounting
                **Chapter:** Chapter 1 — Introduction to Accounting
                **Page Number:** Page 8
                **Confidence Score:** 98%

                #### Verified Reference Snippets
                > "The primary objectives of accounting are to maintain systematic records of financial transactions, calculate profit or loss, depict the financial position of the business, and communicate financial information to stakeholders for decision making."

                #### Concept Summary
                According to your uploaded NCERT chapter, accounting serves as the "language of business."
                There are four key qualitative characteristics:
                1. **Reliability:** Records must be factual and verifiable.
                2. **Relevance:** Must meet user needs on time.
                3. **Understandability:** Presented clearly to help users interpret patterns.
                4. **Comparability:** Allows consistent period-by-period tracking.

                Would you like to generate a **One-Page Revision Note** or a **Quick Flashcard Pack** from this source?
            """.trimIndent()
            
            else -> """
                ### Mission Control Study Coach Guidance

                Welcome back to your Study Command Deck! Let's examine your active goals:
                
                *   **Target:** Crack Class 11 Exams with Elite performance and build a solid foundation for your **Chartered Accountant (CA)** aspiration.
                *   **Status Check:** Your Economics and Business Studies retention are excellent, but **Accountancy Chapter 3 (Journal Entries)** stands at a weak 20% retention screen.
                
                #### Procrastination Alert
                Your task *"Revise Accounts Chapter 3"* has been open for 4 days. Let's break it down into micro-steps:
                1. Open commerce companion numerical practice.
                2. Complete just **two guided journal entries** for 10 minutes.
                
                Let me know if you would like me to draft a daily planner or answer a practice question right now!
            """.trimIndent()
        }
    }
}
