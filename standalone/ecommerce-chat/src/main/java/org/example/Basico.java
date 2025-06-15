package org.example;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Arrays;

public class Basico {

    public static void main(String[] args) {
        var user = "Gere 5 produtos";
        var system = "Você é um gerador de produtos fictícios para um ecommerce e deve gerar apenas o nome dos produtos solicitados pelo usuário";

        var service = getOpenAiService(40L);

        var completionRequest = ChatCompletionRequest
                .builder()
                .model("gpt-4")
                .messages(Arrays.asList(
                        new ChatMessage(ChatMessageRole.USER.value(), user),
                        new ChatMessage(ChatMessageRole.SYSTEM.value(), system)
                ))
                .build();

        service.createChatCompletion(completionRequest)
                .getChoices()
                .forEach(System.out::println);


    }

    @NotNull
    public static OpenAiService getOpenAiService(long seconds) {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("OPENAI_TOKEN");
        var service = new OpenAiService(token, Duration.ofSeconds(seconds));
        return service;
    }
}