package com.tril.clariar.http

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class GroqApiRequest(private val apiKey: String, private val image: Bitmap) {

    private fun convertBitMapToBase64(image: Bitmap): String{
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    fun extractContent(jsonString: String): String? {
        val jsonObject = JSONObject(jsonString)
        val choicesArray = jsonObject.getJSONArray("choices")
        val firstChoice = choicesArray.getJSONObject(0)
        val messageObject = firstChoice.getJSONObject("message")
        return messageObject.getString("content")
    }


    fun sendChatRequest(): String? {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        return try {

            val imageBase64 =  convertBitMapToBase64(image)
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
                                    "text": "Descreva brevemente essta imagem para um deficiente visual em no máximo 5 frases. Seja objetivo, direto e humano. Foque nos elementos principais sem se prolongar. Não se apresente!"
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
                    "stream": false,
                    "stop": null
                }
            """.trimIndent()

            // Envia o corpo da requisição
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(jsonBody.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            // Lê a resposta
            val response: String = if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
            } else {
                BufferedReader(InputStreamReader(connection.errorStream)).use {
                    val errorResponse = it.readText()
                    Log.e("GroqApiRequest", "Erro na resposta: $errorResponse")
                    return null
                }
            }

            return extractContent(response)
        } finally {
            connection.disconnect()
        }
    }
}