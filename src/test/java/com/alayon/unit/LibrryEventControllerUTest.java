package com.alayon.unit;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.alayon.controller.LibraryController;
import com.alayon.models.Book;
import com.alayon.models.LibraryEvent;
import com.alayon.producer.LibraryEventProducer;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(LibraryController.class)
@AutoConfigureMockMvc
public class LibrryEventControllerUTest {

	private static final String API_PATH = "/v1/libraryevent";

	@Autowired
	MockMvc mock;

	@Autowired
	ObjectMapper objMapper;

	@MockBean
	LibraryEventProducer producer;

	@Test
	public void postLibraryEvent() throws Exception {
		// given
		final Book book = Book.builder().bookId(123).bookAuthor("Dilip").bookName("Kafka with spring").build();
		final LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(book).build();

		final String json = objMapper.writeValueAsString(libraryEvent);

		doNothing().when(producer).sendLibraryEventApproach2((libraryEvent));

		// when
		mock.perform(post(API_PATH).content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

	}

	@Test
	public void postLibraryEvent_4xx() throws Exception {
		// given
		final LibraryEvent libraryEvent = LibraryEvent.builder().libraryEventId(null).book(null).build();

		final String json = objMapper.writeValueAsString(libraryEvent);

		doNothing().when(producer).sendLibraryEventApproach2((libraryEvent));

		// expect
		final String expectedErrorMessage = "book - must not be null";
		mock.perform(post(API_PATH).content(json).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().is4xxClientError()).andExpect(content().string(expectedErrorMessage));

	}
}
