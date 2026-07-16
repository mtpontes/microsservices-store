package br.com.ecommerce.products.infra.exception.handlers;

import static org.springframework.http.HttpStatus.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.ecommerce.common.exception.CustomForbiddenException;
import br.com.ecommerce.common.exception.InvalidTokenException;
import br.com.ecommerce.products.infra.exception.exceptions.CategoryNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.DepartmentNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ManufacturerNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ProductNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION = "Malformed or unexpected json format";

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError404(NoResourceFoundException ex) {
		return ResponseEntity
				.status(NOT_FOUND.value())
				.body(new ResponseErrorWithoutMessage(
						NOT_FOUND.value(),
						NOT_FOUND.getReasonPhrase()));
	}

	@ExceptionHandler(DepartmentNotFoundException.class)
	public ResponseEntity<ResponseError> handleError404(DepartmentNotFoundException ex) {
		return ResponseEntity
				.status(NOT_FOUND.value())
				.body(new ResponseError(
						NOT_FOUND.value(),
						NOT_FOUND.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<ResponseError> handleError404(CategoryNotFoundException ex) {
		return ResponseEntity
				.badRequest()
				.body(new ResponseError(
						BAD_REQUEST.value(),
						BAD_REQUEST.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(ManufacturerNotFoundException.class)
	public ResponseEntity<ResponseError> handleError404(ManufacturerNotFoundException ex) {
		return ResponseEntity
				.status(NOT_FOUND.value())
				.body(new ResponseError(
						NOT_FOUND.value(),
						NOT_FOUND.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ResponseError> handleError404(ProductNotFoundException ex) {
		return ResponseEntity
				.status(NOT_FOUND.value())
				.body(new ResponseError(
						NOT_FOUND.value(),
						NOT_FOUND.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseError> handleError400(MethodArgumentNotValidException ex) {
		Map<String, String> fields = ex.getFieldErrors().stream()
				.collect(Collectors.toMap(
						f -> f.getField().toString(), f -> f.getDefaultMessage()));

		return ResponseEntity
				.badRequest()
				.body(new ResponseError(
						BAD_REQUEST.value(),
						BAD_REQUEST.getReasonPhrase(),
						fields));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError400(HandlerMethodValidationException ex) {
		return ResponseEntity
				.badRequest()
				.body(new ResponseErrorWithoutMessage(
						BAD_REQUEST.value(),
						BAD_REQUEST.getReasonPhrase()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResponseError> handlerErro400(IllegalArgumentException ex) {
		return ResponseEntity
				.badRequest()
				.body(new ResponseError(
						BAD_REQUEST.value(),
						BAD_REQUEST.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseError> handleError400(HttpMessageNotReadableException ex) {
		return ResponseEntity
				.badRequest()
				.body(new ResponseError(
						BAD_REQUEST.value(),
						BAD_REQUEST.getReasonPhrase(),
						HTTP_MESSAGE_NOT_READABLE_EXCEPTION));
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<ResponseError> handlerErro415(HttpMediaTypeNotSupportedException ex) {
		String unsupported = Optional.ofNullable(ex.getContentType())
				.map(media -> media.getType() + "/" + media.getSubtype())
				.orElse("unknown");

		String supported = ex.getSupportedMediaTypes().stream()
				.map(mediaType -> mediaType.getType() + "/" + mediaType.getSubtype())
				.collect(Collectors.joining(", "));

		String message = String.format(
				"Unsupported media type '%s'. Supported media types are: %s",
				unsupported,
				supported);

		return ResponseEntity
				.status(UNSUPPORTED_MEDIA_TYPE.value())
				.body(new ResponseError(
						UNSUPPORTED_MEDIA_TYPE.value(),
						UNSUPPORTED_MEDIA_TYPE.getReasonPhrase(),
						message));
	}

	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<?> handleError401(InvalidTokenException ex) {
		log.debug("INVALID TOKEN EXCEPTION MESSAGE: {}", ex.getMessage());
		return ResponseEntity
				.status(UNAUTHORIZED.value())
				.body(new ResponseError(
						UNAUTHORIZED.value(),
						UNAUTHORIZED.getReasonPhrase(),
						InvalidTokenException.DEFAULT_MESSAGE));
	}

	@ExceptionHandler(CustomForbiddenException.class)
	public ResponseEntity<?> handleError403(CustomForbiddenException ex) {
		return ResponseEntity
				.status(FORBIDDEN.value())
				.body(new ResponseError(
						FORBIDDEN.value(),
						FORBIDDEN.getReasonPhrase(),
						ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError500(Exception ex) {
		log.error("Erro interno do servidor", ex);
		return ResponseEntity
				.internalServerError()
				.body(new ResponseErrorWithoutMessage(
						INTERNAL_SERVER_ERROR.value(),
						INTERNAL_SERVER_ERROR.getReasonPhrase()));
	}

	private record ResponseError(int status, String error, Object message) {
	}

	private record ResponseErrorWithoutMessage(int status, Object error) {
	}
}