package es.grupo8.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves static assets for JSP views.
 *
 * /css/**        → classpath:/static/css/  (admin JSPs from dev)
 *                  then file:../frontend/css/ (coordinator/captain JSPs)
 * /javascript/** → file:../frontend/javascript/
 * /assets/**     → classpath:/static/assets/
 */
@Configuration
public class WebStaticConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**")
                .addResourceLocations(
                        "classpath:/static/css/",
                        "file:../frontend/css/");

        registry.addResourceHandler("/javascript/**")
                .addResourceLocations("file:../frontend/javascript/");

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        registry.addResourceHandler("/header.html")
                .addResourceLocations("file:../frontend/");
    }
}
