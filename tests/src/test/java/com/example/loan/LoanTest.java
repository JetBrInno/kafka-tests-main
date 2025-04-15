package com.example.loan;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class LoanTest {

    private static KafkaProducer<String, String> producer;

    private double price = 1000;

    // 1) Предусловие - подключаемся к кафке
    // 2) Предусловие - кладем в кафку данные
    // 3) Шлем GET http://localhost:8082/loan - проверяем, что число корректно

    @BeforeAll
    public static void connect() throws UnknownHostException {
        Properties config = new Properties();
        config.put("client.id", InetAddress.getLocalHost().getHostName());
        config.put("bootstrap.servers", "localhost:29092");
        config.put("acks", "all");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producer = new KafkaProducer<>(config);
    }

    @BeforeEach
    public void createDataInTopic() {
        String message = "{\"currency\":\"USD\",\"price\":" + price + "}";
        ProducerRecord<String, String> record = new ProducerRecord<>("prices", message);
        record.headers().add("__TypeId__", "com.example.Price".getBytes());
        producer.send(record);
    }

    @Test
    public void test() {
        given().get("http://localhost:8082/loan").then().statusCode(200).body("totalPrice", equalTo((float)(price * 1.05)));
    }

    @AfterAll
    public static void disconnect() {
        producer.close();
    }
}
