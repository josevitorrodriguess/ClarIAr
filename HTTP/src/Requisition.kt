import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

class GroqApiRequest(private val apiKey: String, private val imageBase64: String) {

    fun sendChatRequest(): String? {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val connection = url.openConnection() as HttpURLConnection

        return try {
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
                                    "text": "Descreva essa imagem para um deficiente visual. Seja objetivo, direto e humano. Não se apresente!"
                                },
                                {
                                    "type": "image_url",
                                    "image_url": {
                                        "url": "data:image/jpeg;base64,$imageBase64"
                                    }
                                }
                            ]
                        }
                    ],
                    "model": "llama-3.2-90b-vision-preview",
                    "temperature": 1,
                    "max_tokens": 1024,
                    "top_p": 1,
                    "stream": false,
                    "stop": null
                }
            """.trimIndent()

            // Envia o corpo da requisição
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(jsonBody.toByteArray())
            outputStream.flush()
            outputStream.close()

            // Lê a resposta
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(connection.inputStream)).use {
                    it.readText()
                }
            } else {
                // Lê a mensagem de erro para entender melhor o problema
                BufferedReader(InputStreamReader(connection.errorStream)).use {
                    val errorResponse = it.readText()
                    println("Erro na resposta: $errorResponse")
                    null
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}