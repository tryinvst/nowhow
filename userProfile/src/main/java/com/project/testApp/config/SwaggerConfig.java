package com.project.testApp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@OpenAPIDefinition(info = @Info(title="api"))
public class SwaggerConfig {
    @Bean
public OpenAPI customOpenApi () {
    return new OpenAPI().info (new io.swagger.v3.oas.models.info.Info().title("мой API")
      .version ( "1.0.0" )
      .description("документация"));

}
}
