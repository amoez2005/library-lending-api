package io.github.amoez2005.library.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Configures API documentation and representation-based cache validation. */
@Configuration
public class ApiConfiguration {
  /** Allows the assignment's Swagger Editor to call the local demo API. */
  @Bean
  public WebMvcConfigurer swaggerEditorCors() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins("https://editor.swagger.io")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("Content-Type", "If-None-Match")
            .exposedHeaders("ETag", "Location");
      }
    };
  }

  /** Generates entity tags for API GET responses and handles conditional requests. */
  @Bean
  public FilterRegistrationBean<ShallowEtagHeaderFilter> etagFilter() {
    var registration = new FilterRegistrationBean<>(new ShallowEtagHeaderFilter());
    registration.addUrlPatterns("/api/*");
    return registration;
  }

  /** Supplies descriptive metadata for the generated OpenAPI contract. */
  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Shelf Library API")
                .version("1.0.0")
                .description(
                    "Stateless library catalog with HAL links, conditional GET and validated lending transitions. Educational demo; no authentication."));
  }
}
