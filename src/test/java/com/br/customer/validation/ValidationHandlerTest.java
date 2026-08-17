package com.br.customer.validation;

import com.br.customer.exceptions.CustomerNotFoundException;
import com.br.customer.exceptions.DuplicateCpfException;
import com.br.score.exceptions.ScoreNotFoundException;
import com.br.score.exceptions.ScoreServiceTimeoutException;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import com.br.score.exceptions.ScoreUnexpectedResponseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidationHandlerTest {

	@InjectMocks
	private ValidationHandler validationHandler;

	@Test
	@DisplayName("deve retornar bad request com lista de erros de validação")
	void deveRetornarBadRequestComErrosDeValidacao() {
		BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "customerRequestDTO");
		bindingResult.addError(new FieldError("customerRequestDTO", "name", "Name is required"));
		bindingResult.addError(new FieldError("customerRequestDTO", "cpf", "CPF must contain exactly 11 digits"));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<List<ValidationHandler.ValidationErrorResponse>> response =
				validationHandler.handleMethodArgumentNotValid(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).hasSize(2);
		assertThat(response.getBody().get(0).field()).isEqualTo("name");
		assertThat(response.getBody().get(0).message()).isEqualTo("Name is required");
		assertThat(response.getBody().get(1).field()).isEqualTo("cpf");
	}

	@Test
	@DisplayName("deve retornar bad request com lista vazia quando não houver field errors")
	void deveRetornarBadRequestComListaVaziaQuandoSemFieldErrors() {
		BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "customerRequestDTO");
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		ResponseEntity<List<ValidationHandler.ValidationErrorResponse>> response =
				validationHandler.handleMethodArgumentNotValid(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEmpty();
	}

	@Test
	@DisplayName("deve retornar not found para CustomerNotFoundException")
	void deveRetornarNotFoundParaCustomerNotFoundException() {
		CustomerNotFoundException exception = new CustomerNotFoundException("Customer not found with id: 99");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleCustomerNotFound(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().mensagem()).isEqualTo("Customer not found with id: 99");
		assertThat(response.getBody().codigoStatus()).isEqualTo(404);
	}

	@Test
	@DisplayName("deve retornar conflict para DuplicateCpfException")
	void deveRetornarConflictParaDuplicateCpfException() {
		DuplicateCpfException exception = new DuplicateCpfException("12345678901");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleDuplicateCpf(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody().mensagem()).contains("12345678901");
		assertThat(response.getBody().codigoStatus()).isEqualTo(409);
	}

	@Test
	@DisplayName("deve retornar bad request para MethodArgumentTypeMismatchException")
	void deveRetornarBadRequestParaMethodArgumentTypeMismatchException() {
		MethodArgumentTypeMismatchException exception =
				new MethodArgumentTypeMismatchException("abc", Long.class, "id", null, null);

		ResponseEntity<ValidationHandler.ErrorResponse> response =
				validationHandler.handleMethodArgumentTypeMismatch(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody().mensagem()).isEqualTo("Invalid value 'abc' for parameter 'id'");
		assertThat(response.getBody().codigoStatus()).isEqualTo(400);
	}

	@Test
	@DisplayName("deve retornar gateway timeout para ScoreServiceTimeoutException")
	void deveRetornarGatewayTimeoutParaScoreServiceTimeoutException() {
		ScoreServiceTimeoutException exception = new ScoreServiceTimeoutException("Timeout connecting to score service.");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleScoreTimeout(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
		assertThat(response.getBody().mensagem()).isEqualTo("Timeout connecting to score service.");
		assertThat(response.getBody().codigoStatus()).isEqualTo(504);
	}

	@Test
	@DisplayName("deve retornar service unavailable para ScoreServiceUnavailableException")
	void deveRetornarServiceUnavailableParaScoreServiceUnavailableException() {
		ScoreServiceUnavailableException exception = new ScoreServiceUnavailableException("Score service error: 503");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleScoreUnavailable(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(response.getBody().mensagem()).isEqualTo("Score service error: 503");
		assertThat(response.getBody().codigoStatus()).isEqualTo(503);
	}

	@Test
	@DisplayName("deve retornar bad gateway para ScoreUnexpectedResponseException")
	void deveRetornarBadGatewayParaScoreUnexpectedResponseException() {
		ScoreUnexpectedResponseException exception = new ScoreUnexpectedResponseException("Unexpected response: 400");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleScoreUnexpected(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
		assertThat(response.getBody().mensagem()).isEqualTo("Unexpected response: 400");
		assertThat(response.getBody().codigoStatus()).isEqualTo(502);
	}

	@Test
	@DisplayName("deve retornar not found para ScoreNotFoundException")
	void deveRetornarNotFoundParaScoreNotFoundException() {
		ScoreNotFoundException exception = new ScoreNotFoundException("Score not found for CPF: 12345678901");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleScoreNotFound(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody().mensagem()).isEqualTo("Score not found for CPF: 12345678901");
		assertThat(response.getBody().codigoStatus()).isEqualTo(404);
	}

	@Test
	@DisplayName("deve retornar internal server error para excecao generica")
	void deveRetornarInternalServerErrorParaExcecaoGenerica() {
		Exception exception = new Exception("erro inesperado");

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleGenericException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().mensagem()).isEqualTo("Internal server error: erro inesperado");
		assertThat(response.getBody().codigoStatus()).isEqualTo(500);
	}

	@Test
	@DisplayName("deve tratar mensagem nula em excecao generica")
	void deveTratarMensagemNulaEmExcecaoGenerica() {
		Exception exception = new Exception((String) null);

		ResponseEntity<ValidationHandler.ErrorResponse> response = validationHandler.handleGenericException(exception);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
		assertThat(response.getBody().mensagem()).isEqualTo("Internal server error: null");
		assertThat(response.getBody().codigoStatus()).isEqualTo(500);
	}

}