package mavis.task.util.common;

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
open class JacksonConfig {
    @Bean
    open fun customObjectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.findAndRegisterModules()
        return mapper
    }
}