package com.alayon.producer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.alayon.models.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LibraryEventProducer {

	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	private final String topic = "library-events";

	public void sendLibraryEvent(final LibraryEvent libraryEvent) throws JsonProcessingException {
		final Integer key = libraryEvent.getLibraryEventId();
		final String value = objectMapper.writeValueAsString(libraryEvent);

		final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(final SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(final Throwable ex) {
				handleFailure(key, value, ex);

			}
		});
	}

	public void sendLibraryEventApproach2(final LibraryEvent libraryEvent) throws JsonProcessingException {
		final Integer key = libraryEvent.getLibraryEventId();
		final String value = objectMapper.writeValueAsString(libraryEvent);

		final ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value, topic);

		final ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(final SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(final Throwable ex) {
				handleFailure(key, value, ex);

			}
		});
	}

	private ProducerRecord<Integer, String> buildProducerRecord(final Integer key, final String value,
			final String topic2) {
		final List<Header> headers = List.of(new RecordHeader("event-source", "scanner".getBytes()));
		return new ProducerRecord<Integer, String>(topic, null, key, value, headers);
	}

	public SendResult<Integer, String> sendLibraryEventSynchronous(final LibraryEvent libraryEvent) throws Exception {
		final Integer key = libraryEvent.getLibraryEventId();
		final String value = objectMapper.writeValueAsString(libraryEvent);
		SendResult<Integer, String> sendResult = null;

		try {
			sendResult = kafkaTemplate.sendDefault(key, value).get();
		} catch (InterruptedException | ExecutionException e) {
			log.error("Error sending the Message and the exception is {}", e.getMessage());
			throw e;
		}
		return sendResult;

	}

	private void handleFailure(final Integer key, final String value, final Throwable ex) {
		log.error("Error sending the Message and the exception is {}", ex.getMessage());

	}

	private void handleSuccess(final Integer key, final String value, final SendResult<Integer, String> result) {
		log.info("Message sent successfully for the key: {} and value:{}, partition is: {}", key, value,
				result.getRecordMetadata().partition());

	}
}
