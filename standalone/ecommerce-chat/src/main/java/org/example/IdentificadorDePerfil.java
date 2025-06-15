package org.example;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class IdentificadorDePerfil {
    private static final Logger logger = LogManager.getLogger(IdentificadorDePerfil.class);

    public static void main(String[] args) {

        var promptSystem = """
                Identifique o perfil de compra de cada cliente.
                A resposta deve ser:
                Cliente - descreva o perfil do cliente em três palavras
                """;


        var clientes = carregarClientesDoArquivo();
        var quantidadeTokens =ContadorTokens.contadorTokens(clientes);
        var modelo = "gpt-3.5-turbo";
        var tamanhoRespostaEsperada = 2048;
        if (quantidadeTokens > 4096 - tamanhoRespostaEsperada) {
            modelo = "gpt-3.5-turbo-16k";
        }


        var service = Basico.getOpenAiService(40L);
        var request = ChatCompletionRequest
                .builder()
                .model(modelo)
                .maxTokens(tamanhoRespostaEsperada)
                .messages(Arrays.asList(
                        new ChatMessage(
                                ChatMessageRole.SYSTEM.value(),
                                promptSystem),
                        new ChatMessage(
                                ChatMessageRole.SYSTEM.value(),
                                clientes)))
                .build();
        var response = service.createChatCompletion(request);
        logger.info("Resposta: {}", response.getChoices().get(0).getMessage().getContent());


    }

    private static String carregarClientesDoArquivo() {
        try {
            var path = Path.of(ClassLoader
                    .getSystemResource("docs/lista_de_compras_100_clientes.csv")
                    .toURI());
            return Files.readAllLines(path, StandardCharsets.UTF_8).toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar o arquivo!", e);
        }
    }
}
