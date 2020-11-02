package com.alayon.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("local") // just run when you are in local profile.
public class AutoCreateConfig {

	@Bean
	public NewTopic libraryEvents() {
		return TopicBuilder.name("library-events").partitions(1).replicas(1).build();
	}
}
