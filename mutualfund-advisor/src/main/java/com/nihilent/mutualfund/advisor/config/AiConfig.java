package com.nihilent.mutualfund.advisor.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AiConfig {

    @Bean
    public EmbeddingModel embeddingModel() {
        try {
            return new AllMiniLmL6V2EmbeddingModel();
        } catch (Exception e) {
            log.error("EmbeddingModel initialization failed — application cannot start", e);
            throw e;
        }
    }
}