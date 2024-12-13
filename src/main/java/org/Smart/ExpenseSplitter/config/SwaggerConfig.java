package org.Smart.ExpenseSplitter.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Smart Expense Splitter API",
                version = "1.0",
                description = "API documentation for Smart Expense Splitter application"
        )
)
public class SwaggerConfig {
}
