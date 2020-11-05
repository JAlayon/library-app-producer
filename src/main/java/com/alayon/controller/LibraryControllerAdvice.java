package com.alayon.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class LibraryControllerAdvice {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleRequestBody(final MethodArgumentNotValidException e) {

		final List<FieldError> errors = e.getBindingResult().getFieldErrors();
		final String errorMessage = errors.stream()
				.map(errorField -> errorField.getField() + " - " + errorField.getDefaultMessage()).sorted()
				.collect(Collectors.joining(","));
		log.error("Error message: {}", errorMessage);
		return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
	}
}
