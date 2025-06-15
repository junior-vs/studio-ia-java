package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.theokanning.openai.OpenAiHttpException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AnaliseDeSentimentos {
    private static final Logger logger = LogManager.getLogger(AnaliseDeSentimentos.class);

    public static void main(String[] args) throws InterruptedException {
        var arquivosDeAvaliacoes = carregarArquivosDeAvaliacoes();

        for (var arquivo : arquivosDeAvaliacoes) {
            logger.info("Iniciando analise do produto: " +arquivo.getFileName());

            var resposta = enviarRequisicao(arquivo);
            salvarArquivoDeAnaliseDeSentimento(arquivo, resposta);

            logger.info("Analise finalizada");
        }
    }

    private static String enviarRequisicao(Path arquivo) throws InterruptedException {

        var service = Basico.getOpenAiService(15L);

        var promptSistema = """
                Você é um analisador de sentimentos de avaliações de produtos.
                Escreva um parágrafo com até 50 palavras resumindo as avaliações e depois atribua qual o sentimento geral para o produto.
                Identifique também 3 pontos fortes e 3 pontos fracos identificados a partir das avaliações.
                                
                #### Formato de saída
                Nome do produto:
                Resumo das avaliações: [resuma em até 50 palavras]
                Sentimento geral: [deve ser: POSITIVO, NEUTRO ou NEGATIVO]
                Pontos fortes: [3 bullets points]
                Pontos fracos: [3 bullets points]
                """;
        var promptUsuario = lerConteudoDoArquivo(arquivo);

        var request = ChatCompletionRequest
                .builder()
                .model("gpt-4-1106-preview")
                .messages(Arrays.asList(
                        new ChatMessage(
                                ChatMessageRole.SYSTEM.value(),
                                promptSistema),
                        new ChatMessage(
                                ChatMessageRole.USER.value(),
                                promptUsuario)))
                .build();

        var segundoParaProximaTentiva = 5;
        var tentativas = 0;
        while (tentativas++ != 5) {
            try {
                return service
                        .createChatCompletion(request)
                        .getChoices().get(0).getMessage().getContent();
            } catch (OpenAiHttpException ex) {
                var errorCode = ex.statusCode;
                switch (errorCode) {
                    case 401 -> throw new RuntimeException("Erro com a chave da API!", ex);
                    case 429 -> {
                        logger.info("Rate Limit atingido! Nova tentativa em instantes");
                        Thread.sleep(1000 * segundoParaProximaTentiva);
                        segundoParaProximaTentiva *= 2;
                    }
                    case 500, 503 -> {
                        logger.info("API fora do ar! Nova tentativa em instantes");
                        Thread.sleep(1000 * segundoParaProximaTentiva);
                        segundoParaProximaTentiva *= 2;
                    }
                }
            }
        }
        throw new RuntimeException("API Fora do ar! Tentativas finalizadas sem sucesso!");
    }

    private static List<Path> carregarArquivosDeAvaliacoes() {
        try {
            var diretorioAvaliacoes = Path.of("src/main/resources/avaliacoes");
            return Files
                    .walk(diretorioAvaliacoes, 1)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar os arquivos de avaliacoes!", e);
        }
    }

    private static String lerConteudoDoArquivo(Path arquivo) {
        try {
            return Files.readAllLines(arquivo).toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler conteudo do arquivo!", e);
        }
    }

    private static void salvarArquivoDeAnaliseDeSentimento(Path arquivo, String analise) {
        try {
            var nomeProduto = arquivo
                    .getFileName()
                    .toString()
                    .replace(".txt", "")
                    .replace("avaliacoes-", "");
            var diretorio = Path.of("src/main/resources/analises");
            // Cria o diretório se ele não existir
            Files.createDirectories(diretorio);
        
            var path = diretorio.resolve("analise-sentimentos-" + nomeProduto + ".txt");
            Files.writeString(path, analise, StandardOpenOption.CREATE);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar o arquivo!", e);
        }
    }

}