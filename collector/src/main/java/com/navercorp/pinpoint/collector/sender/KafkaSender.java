package com.navercorp.pinpoint.collector.sender;

import com.navercorp.pinpoint.collector.config.CollectorConfiguration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author chunchun.xu on 2019/2/15.
 * @version 1.0
 * @descripte
 */
public class KafkaSender {

	private KafkaProducer kafkaProducer;
	private String topic;

	public KafkaSender(CollectorConfiguration config) {
		if (config.isKafkaEnable()) {
			Properties props = new Properties();
			props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaServer());
			props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
			props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

			topic = config.getKafkaTopic();
			kafkaProducer = new KafkaProducer(props);
		}
	}


	public boolean send(String data) {
		if (kafkaProducer == null) {
			return false;
		}
		ProducerRecord record = new ProducerRecord(topic, data);
		kafkaProducer.send(record);
		return true;
	}
}
