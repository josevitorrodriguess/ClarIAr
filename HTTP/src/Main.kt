fun main() {
    val apiKey = "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" // Substitua com a sua chave de API
    val imageBase64 = imageBase64 // Substitua com a imagem em base64

    val groqApiRequest = GroqApiRequest(apiKey, imageBase64)
    val response = groqApiRequest.sendChatRequest()

    println("Resposta da API: $response")
}



