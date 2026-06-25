package com.mining.safety.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI safeMineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeMine — Mine Incident & Safety Reporting System")
                        .description("""
                                ## Overview
                                SafeMine is a centralised platform for reporting, tracking, and resolving
                                workplace incidents in mining operations.

                                ## Authentication
                                All endpoints except `/api/auth/login` require a **JWT Bearer token**.
                                Obtain a token via `POST /api/auth/login`, then click **Authorize** and
                                enter `<your-token>` (the field adds the `Bearer ` prefix automatically).

                                ## Roles & Access
                                | Role | Description |
                                |------|-------------|
                                | `ADMIN` | Full system access — manage users, incidents, dashboard |
                                | `SAFETY_OFFICER` | Create/update incidents, view dashboard |
                                | `MANAGER` | Update incident status and root cause, view dashboard |
                                | `WORKER` | Report incidents only |

                                ## Default Credentials (dev)
                                | Email | Password | Role |
                                |-------|----------|------|
                                | admin@safemine.co.za | Admin@123 | ADMIN |
                                | safety@safemine.co.za | Safety@123 | SAFETY_OFFICER |
                                | worker@safemine.co.za | Worker@123 | WORKER |

                                ---
                                <img src="/safemine-logo.svg" alt="SafeMine Logo" height="60"/>
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SafeMine Platform Team")
                                .email("support@safemine.co.za")
                                .url("https://safemine.co.za"))
                        .license(new License()
                                .name("Proprietary — SafeMine")
                                .url("https://safemine.co.za")))
                .externalDocs(new ExternalDocumentation()
                        .description("SafeMine User Guide")
                        .url("https://safemine.co.za/docs"))
                .tags(List.of(
                        new Tag().name("Authentication").description("Login and token management"),
                        new Tag().name("Incidents").description("Create, retrieve, and update mine incidents"),
                        new Tag().name("Users").description("User account management — Admin only"),
                        new Tag().name("Dashboard").description("Aggregated statistics and KPI metrics")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT token returned by `/api/auth/login`")));
    }
}
