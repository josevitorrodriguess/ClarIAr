package com.tril.clariar.http

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class GroqApiRequest(private val apiKey: String, private val image: Bitmap) {

    private fun convertBitMapToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun processStreamLine(line: String): String? {
        try {
            // Verifica se o chunk contém conteúdo válido
            val jsonObject = JSONObject(line)
            if (jsonObject.has("choices")) {
                val choicesArray = jsonObject.getJSONArray("choices")
                val firstChoice = choicesArray.getJSONObject(0)
                if (firstChoice.has("delta")) {
                    val deltaObject = firstChoice.getJSONObject("delta")
                    return deltaObject.optString("content", null)
                }
            }
        } catch (e: Exception) {
            Log.e("GroqApiRequest", "Erro ao processar linha de stream: $line", e)
        }
        return null
    }

    fun sendChatRequest(): String? {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        return try {
            val imageBase64 = convertBitMapToBase64(image)
            // Configura o método e os cabeçalhos da requisição
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.doOutput = true

            // Define o corpo da requisição com a imagem base64 no formato de URL embutida
            val jsonBody = """
                {
                    "messages": [
                        {
                            "role": "user",
                            "content": [
                                {
                                    "type": "text",
                                    "text": "Descreva brevemente esta imagem para um deficiente visual em no máximo 5 frases. Seja objetivo, direto e humano. Foque nos elementos principais sem se prolongar. Não se apresente!"
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

            // Envia o corpo da requisição
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            // Lê a resposta como stream
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseBuilder = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        // Processa cada linha recebida no stream
                        val content = processStreamLine(line)
                        if (content != null) {
                            responseBuilder.append(content)
                            Log.d("GroqApiRequest", "Chunk recebido: $content")
                        }
                    }
                }
                return responseBuilder.toString()
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use {
                    val errorResponse = it.readText()
                    Log.e("GroqApiRequest", "Erro na resposta: $errorResponse")
                    return null
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
