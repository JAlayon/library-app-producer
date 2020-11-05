package com.alayon.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import com.alayon.models.Book;
import com.alayon.models.LibraryEvent;
import com.alayon.producer.LibraryEventProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUTest {

	@Mock
	KafkaTemplate<Integer, String> kafkaTemplate;

	@Spy
	ObjectMapper objectMapper = new ObjectMapper();

	@InjectMocks
	LibraryEventProducer libraryEventProducer;

	@Test
	public void sendLibraryEvent_approach2_onFailure() {
		// given
		final Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka with spring").build();
		final LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();

		// when
		final SettableListenableFuture future = new SettableListenableFuture();
		future.setException(new RuntimeException("Exception calling kakfa"));
		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

		// then
		assertThrows(Exception.class, () -> libraryEventProducer.sendLibraryEventApproach2(libraryEvent).get());
	}

	@Test
	public void sendLibraryEvent_approach2_onSuccess()
			throws JsonProcessingException, InterruptedException, ExecutionException {
		// given
		final Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka with spring").build();
		final LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();
		final String record = objectMapper.writeValueAsString(libraryEvent);

		final ProducerRecord<Integer, String> producerRecord = new ProducerRecord<>("library-events",
				libraryEvent.getLibraryEventId(), record);

		final RecordMetadata recordMetadata = new RecordMetadata(new TopicPartition("library-events", 1), 1, 1, 342,
				System.currentTimeMillis(), 1, 2);

		final SendResult<Integer, String> sendResult = new SendResult<>(producerRecord, recordMetadata);

		// when
		final SettableListenableFuture future = new SettableListenableFuture();
		future.set(sendResult);

		when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);

		// then
		final ListenableFuture<SendResult<Integer, String>> listenableFuture = libraryEventProducer
				.sendLibraryEventApproach2(libraryEvent);

		final SendResult<Integer, String> response = listenableFuture.get();

		assert response.getRecordMetadata().partition() == 1;
	}

}
