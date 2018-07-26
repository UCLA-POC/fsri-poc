package edu.ucla.fsri.integration;

import org.apache.camel.component.amqp.AMQPConnectionDetails;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import io.hawt.config.ConfigFacade;
import io.hawt.springboot.HawtPlugin;

@SpringBootApplication
@EnableAutoConfiguration
public class Application {
	
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Loading an example plugin.
     */
    @Bean
    public HawtPlugin samplePlugin() {
        return new HawtPlugin("sample-plugin",
            "plugins",
            "",
            new String[] { "sample-plugin/js/sample-plugin.js" });
    }

    /**
     * Set things up to be in offline mode.
     */
    @Bean
    public ConfigFacade configFacade() {
        return ConfigFacade.getSingleton();
    }
    
    @Bean
    public ServletRegistrationBean camelServletRegistrationBean() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new CamelHttpTransportServlet(), "/camel/*");
        registration.setName("CamelServlet");
        return registration;
    }
    
}
