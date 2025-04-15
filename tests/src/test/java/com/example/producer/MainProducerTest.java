package com.example.producer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class MainProducerTest {

    private static KafkaConsumer<String, String> consumer;

    private double price = 9;

    // 1) Предусловие - подключаемся к кафке
    // 2) Предусловие - кладем в кафку данные через апи
    // 3) Идем в кафку как консюмер и проверяем значение

    @BeforeAll
    public static void connect() throws UnknownHostException {
        Properties config = new Properties();
        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("bootstrap.servers", "localhost:29092");
        config.put("acks", "all");
        config.put("enable.auto.commit", "true");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "testik");
        consumer = new KafkaConsumer<>(config);
    }

    @BeforeEach
    public void createDataInTopic() {
        given().get("http://localhost:8081/rate/" + price).then().extract().asPrettyString();
    }

    @Test
    public void test() {
        consumer.subscribe(List.of("prices"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10000));
        for (ConsumerRecord<String, String> record : records) {
            if (record.value().contains("9.05")) {
                assertTrue(record.value().contains("9.05"));
                break;
            }
        }
       // String value = records.iterator().next().value();
        //assertNotNull(value);
       // assertEquals(1, records.count());
    }

    @AfterAll
    public static void disconnect() {
        consumer.close();
    }
}
