package com.tril.clariar.http
import com.tril.clariar.AudioUtils
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.tril.clariar.TextToSpeechHandler
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GroqApiRequest(private val apiKey: String, private val image: Bitmap, private val ttsHandler: TextToSpeechHandler) {

    private fun convertBitMapToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun processStreamLine(line: String): String? {
        val trimmedLine = line.trim()
        if (trimmedLine.isEmpty()) {
            return null
        }

        val jsonLine = if (trimmedLine.startsWith("data:")) {
            trimmedLine.substringAfter("data:").trim()
        } else {
            trimmedLine
        }
        if (jsonLine.isEmpty()) {
            return null
        }
        if (jsonLine == "[DONE]") {
            return null
        }

        if (jsonLine.startsWith("{")) {
            try {
                val jsonObject = JSONObject(jsonLine)
                if (jsonObject.has("choices")) {
                    val choicesArray = jsonObject.getJSONArray("choices")
                    if (choicesArray.length() > 0) {
                        val firstChoice = choicesArray.getJSONObject(0)
                        if (firstChoice.has("delta")) {
                            val deltaObject = firstChoice.getJSONObject("delta")
                            return deltaObject.optString("content", null)
                        } else if (firstChoice.has("message")) {
                            // Handle the initial message if needed
                            val messageObject = firstChoice.getJSONObject("message")
                            return messageObject.optString("content", null)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GroqApiRequest", "Erro ao processar linha de stream: $jsonLine", e)
            }
        } else {
            // The line is not a JSON object; it might be an array or invalid JSON
            Log.e("GroqApiRequest", "Linha não é um JSONObject: $jsonLine")
        }
        return null
    }

    fun sendChatRequest(): String? {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        return try {

            AudioUtils().bipSong()
            val imageBase64 = convertBitMapToBase64(image)

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true

            // Defines the request body with the base64 image in the embedded url format
            val jsonBody = """
                {
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "Descreva brevemente esta imagem para um deficiente visual em no máximo 5 frases. Seja objetivo, direto e humano. Foque nos elementos principais sem se prolongar. Não se apresente e parta diretamente para a descrição!"
                                },
                                {
                                    "type": "image_url",
                                    "image_url": {
                                        "url": "data:image/png;base64,$imageBase64"
                                    }
                                }
                            ]
                        }
                    ],
                    "model": "llama-3.2-90b-vision-preview",
                    "temperature": 0.5,
                    "max_tokens": 200,
                    "top_p": 1,
                    "stream": true,
                    "stop": null
                }
            """.trimIndent()

            // Send the request body
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            // Read the response as a stream
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBuilder = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        val content = processStreamLine(line)
                        if (content != null) {
                            responseBuilder.append(content)
                            //Log.d("GroqApiRequest", "Chunk recebido: $content")
                        }
                    }
                }
                return responseBuilder.toString()
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use {
                    val errorResponse = it.readText()
                    Log.e("GroqApiRequest", "Erro na resposta: $errorResponse")
                    ttsHandler.speak("Não foi possível descrever a imagem!")
                    return null
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}