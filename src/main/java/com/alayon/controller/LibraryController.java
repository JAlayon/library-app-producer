package com.alayon.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.alayon.models.LibraryEvent;
import com.alayon.models.LibraryEventType;
import com.alayon.producer.LibraryEventProducer;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class LibraryController {

	@Autowired
	private LibraryEventProducer libraryEventProducer;

	@PostMapping("/v1/libraryevent")
	public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody final LibraryEvent libraryEvent)
			throws Exception {

		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		log.info("Before sendLibraryEvent");
		libraryEventProducer.sendLibraryEventApproach2(libraryEvent);
		log.info("After sendLibraryEvent");

		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}
}
