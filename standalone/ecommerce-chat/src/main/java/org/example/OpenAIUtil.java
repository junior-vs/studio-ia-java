package org.example;

import com.theokanning.openai.service.OpenAiService;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;
import java.util.Objects;

public class OpenAIUtil {
    public static OpenAiService criarServico(int seconds) {
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("OPENAI_TOKEN");
        return new OpenAiService(Objects.requireNonNull(token), Duration.ofSeconds(seconds));

    }
}
