package br.com.ecommerce.products.infra.exception.handlers;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import br.com.ecommerce.products.infra.exception.exceptions.CategoryNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.DepartmentNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ManufacturerNotFoundException;
import br.com.ecommerce.products.infra.exception.exceptions.ProductNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final String HTTP_MESSAGE_NOT_READABLE_EXCEPTION = "Malformed or unexpected json format";

	private final HttpStatus notFound = HttpStatus.NOT_FOUND;
	private final HttpStatus unauthorized = HttpStatus.UNAUTHORIZED;
	private final HttpStatus badRequest = HttpStatus.BAD_REQUEST;
	private final HttpStatus unsupportedMediaType = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
	private final HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;


	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError404(NoResourceFoundException ex) {
		return ResponseEntity
			.status(notFound.value())
			.body(new ResponseErrorWithoutMessage(
				notFound.value(),
				notFound.getReasonPhrase()));
	}

	@ExceptionHandler(DepartmentNotFoundException.class)
	public ResponseEntity<ResponseError> handleError400(DepartmentNotFoundException ex) {
		return ResponseEntity
			.status(unauthorized.value())
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				ex.getMessage()));
	}

	@ExceptionHandler(CategoryNotFoundException.class)
	public ResponseEntity<ResponseError> handleError400(CategoryNotFoundException ex) {
		return ResponseEntity
			.status(unauthorized.value())
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				ex.getMessage()));
	}

	@ExceptionHandler(ManufacturerNotFoundException.class)
	public ResponseEntity<ResponseError> handleError400(ManufacturerNotFoundException ex) {
		return ResponseEntity
			.status(unauthorized.value())
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				ex.getMessage()));
	}

	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ResponseError> handleError400(ProductNotFoundException ex) {
		return ResponseEntity
			.status(unauthorized.value())
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ResponseError> handleError400(MethodArgumentNotValidException ex) {
		var fields = ex.getFieldErrors().stream()
			.collect(Collectors.toMap(
				f -> f.getField().toString(), f -> f.getDefaultMessage()));
		
		return ResponseEntity.badRequest()
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				fields));
	}

	@ExceptionHandler(HandlerMethodValidationException.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError400(HandlerMethodValidationException ex) {
		return ResponseEntity
			.status(badRequest.value())
			.body(new ResponseErrorWithoutMessage(
				badRequest.value(),
				badRequest.getReasonPhrase()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResponseError> handlerErro400(IllegalArgumentException ex) {
		return ResponseEntity.badRequest()
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
				ex.getMessage()));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ResponseError> handleError400(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest()
			.body(new ResponseError(
				badRequest.value(),
				badRequest.getReasonPhrase(),
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
			.status(unsupportedMediaType.value())
			.body(new ResponseError(
				unsupportedMediaType.value(),
				unsupportedMediaType.getReasonPhrase(),
				message));
	}

	@ExceptionHandler(MissingRequestHeaderException.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handlerMissingRequestHeaderException(
		MissingRequestHeaderException ex
	) {
		String headerName = ex.getHeaderName();
		if (headerName.equalsIgnoreCase("X-auth-user-id"))
			return ResponseEntity
				.status(unauthorized.value())
				.body(new ResponseErrorWithoutMessage(
					unauthorized.value(), 
					unauthorized.getReasonPhrase()));

        return this.handleError500(ex);
    }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ResponseErrorWithoutMessage> handleError500(Exception ex) {
		ex.printStackTrace();
		return ResponseEntity.internalServerError()
			.body(new ResponseErrorWithoutMessage(
				internalServerError.value(),
				internalServerError.getReasonPhrase()));
	}

	private record ResponseError(int status, String error, Object message) {}
	private record ResponseErrorWithoutMessage(int status, Object error) {}
}