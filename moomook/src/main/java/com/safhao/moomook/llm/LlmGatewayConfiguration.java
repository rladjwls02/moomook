package com.safhao.moomook.llm;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmGatewayConfiguration {
    @Bean
    @ConditionalOnMissingBean(LlmGateway.class)
    public LlmGateway dummyLlmGateway() {
        return new DummyLlmGateway();
    }
}
