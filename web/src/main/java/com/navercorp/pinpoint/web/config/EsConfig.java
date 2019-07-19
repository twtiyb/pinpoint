package com.navercorp.pinpoint.web.config;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author chunchun.xu on 2019/2/20.
 * @version 1.0
 * @descripte
 */
@Configuration
public class EsConfig {

	public @Bean
	HttpClientConfig clientConfig() {
		String connectionUrl = System.getProperty("es.url", "http://192.168.6.32:9200");
		HttpClientConfig clientConfig = new HttpClientConfig
				.Builder(connectionUrl)
				.multiThreaded(true)
				//Per default this implementation will create no more than 2 concurrent connections per given route
				.defaultMaxTotalConnectionPerRoute(2)
				// and no more 20 connections in total
				.maxTotalConnection(20)
				.build();
		return clientConfig;
	}

	public @Bean
	JestClient jestClient() {
		JestClientFactory factory = new JestClientFactory();
		factory.setHttpClientConfig(clientConfig());
		return factory.getObject();
	}

}
