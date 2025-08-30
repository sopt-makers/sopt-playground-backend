package org.sopt.makers.internal;

import org.sopt.makers.internal.auth.external.auth.AuthClientProperty;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableConfigurationProperties(AuthClientProperty.class)
public class InternalApplication {

	public static void main(String[] args) {
		SpringApplication.run(InternalApplication.class, args);
	}

}
