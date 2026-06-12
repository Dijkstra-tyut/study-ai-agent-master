package study.studyai.config;

import com.alibaba.dashscope.aigc.generation.Generation;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import study.studyai.config.properties.DashScopeAiProperties;

@Configuration
@EnableConfigurationProperties(DashScopeAiProperties.class)
public class DashScopeAiConfig {

    @Bean
    public Generation generation() {
        return new Generation();
    }
}
