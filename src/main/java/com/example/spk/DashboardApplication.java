package com.example.spk;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@SpringBootApplication
public class DashboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashboardApplication.class, args);
	}

    @Bean
    public ApplicationRunner runner(ApplicationContext ctx) {
        return args -> {
            System.out.println("==== ROUTES TERDAFTAR ====");
            RequestMappingHandlerMapping mapping = ctx.getBean(RequestMappingHandlerMapping.class);
            mapping.getHandlerMethods().forEach((k, v) -> {
                System.out.println(k + " => " + v);
            });
            System.out.println("==========================");
        };
    }

}
