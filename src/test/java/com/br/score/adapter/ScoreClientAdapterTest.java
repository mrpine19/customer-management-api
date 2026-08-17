package com.br.score.adapter;

import com.br.score.client.ScoreClient;
import com.br.score.dto.ScoreResponseDTO;
import com.br.score.exceptions.ScoreNotFoundException;
import com.br.score.exceptions.ScoreServiceTimeoutException;
import com.br.score.exceptions.ScoreServiceUnavailableException;
import com.br.score.exceptions.ScoreUnexpectedResponseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreClientAdapterTest {

	@Mock
	private ScoreClient scoreClient;

	@InjectMocks
	private ScoreClientAdapter scoreClientAdapter;

	@Test
	@DisplayName("deve retornar o score quando o client responder com sucesso")
	void deveRetornarScoreQuandoClientResponderComSucesso() {
		ScoreResponseDTO expected = new ScoreResponseDTO("12345678901", 750, "GOOD");
		when(scoreClient.getScoreByCpf("12345678901")).thenReturn(expected);

		ScoreResponseDTO result = scoreClientAdapter.getScoreByCpf("12345678901");

		assertThat(result).isEqualTo(expected);
		verify(scoreClient).getScoreByCpf("12345678901");
	}

	@Test
	@DisplayName("deve retornar score quando CPF for nulo e o client aceitar")
	void deveRetornarScoreQuandoCpfForNulo() {
		ScoreResponseDTO expected = new ScoreResponseDTO(null, 500, "MEDIUM");
		when(scoreClient.getScoreByCpf(null)).thenReturn(expected);

		ScoreResponseDTO result = scoreClientAdapter.getScoreByCpf(null);

		assertThat(result).isEqualTo(expected);
		verify(scoreClient).getScoreByCpf(null);
	}

	@Test
	@DisplayName("deve lançar ScoreServiceTimeoutException quando houver timeout por SocketTimeoutException")
	void deveLancarScoreServiceTimeoutExceptionQuandoSocketTimeout() {
		when(scoreClient.getScoreByCpf("12345678901"))
				.thenThrow(new ResourceAccessException("timeout", new SocketTimeoutException("Read timed out")));

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreServiceTimeoutException.class)
				.hasMessage("Timeout connecting to score service. Please try again later.");
	}

	@Test
	@DisplayName("deve lançar ScoreServiceTimeoutException quando houver timeout por HttpTimeoutException")
	void deveLancarScoreServiceTimeoutExceptionQuandoHttpTimeout() {
		when(scoreClient.getScoreByCpf("12345678901"))
				.thenThrow(new ResourceAccessException("timeout", new HttpTimeoutException("request timed out")));

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreServiceTimeoutException.class)
				.hasMessageContaining("Please try again later");
	}

	@Test
	@DisplayName("deve lançar ScoreServiceUnavailableException quando ResourceAccessException não for timeout")
	void deveLancarScoreServiceUnavailableExceptionQuandoResourceAccessNaoForTimeout() {
		when(scoreClient.getScoreByCpf("12345678901"))
				.thenThrow(new ResourceAccessException("connection refused"));

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreServiceUnavailableException.class)
				.hasMessageContaining("connection refused");
	}

	@Test
	@DisplayName("deve lançar ScoreServiceUnavailableException quando o score service retornar erro 5xx")
	void deveLancarScoreServiceUnavailableExceptionQuandoScoreRetornar5xx() {
		HttpServerErrorException exception = HttpServerErrorException.create(
				HttpStatus.SERVICE_UNAVAILABLE,
				"Service Unavailable",
				HttpHeaders.EMPTY,
				new byte[0],
				StandardCharsets.UTF_8
		);
		when(scoreClient.getScoreByCpf("12345678901")).thenThrow(exception);

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreServiceUnavailableException.class)
				.hasMessageContaining("503 SERVICE_UNAVAILABLE");
	}

	@Test
	@DisplayName("deve lançar ScoreNotFoundException quando score não for encontrado")
	void deveLancarScoreNotFoundExceptionQuandoScoreNaoForEncontrado() {
		HttpClientErrorException exception = HttpClientErrorException.create(
				HttpStatus.NOT_FOUND,
				"Not Found",
				HttpHeaders.EMPTY,
				new byte[0],
				StandardCharsets.UTF_8
		);
		when(scoreClient.getScoreByCpf("12345678901")).thenThrow(exception);

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreNotFoundException.class)
				.hasMessageContaining("12345678901");
	}

	@Test
	@DisplayName("deve lançar ScoreUnexpectedResponseException quando o score service retornar erro 4xx diferente de 404")
	void deveLancarScoreUnexpectedResponseExceptionQuandoRetornar4xxDiferenteDe404() {
		HttpClientErrorException exception = HttpClientErrorException.create(
				HttpStatus.BAD_REQUEST,
				"Bad Request",
				HttpHeaders.EMPTY,
				new byte[0],
				StandardCharsets.UTF_8
		);
		when(scoreClient.getScoreByCpf("12345678901")).thenThrow(exception);

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreUnexpectedResponseException.class)
				.hasMessageContaining("400 BAD_REQUEST");
	}

	@Test
	@DisplayName("deve lançar ScoreUnexpectedResponseException quando ocorrer erro inesperado")
	void deveLancarScoreUnexpectedResponseExceptionQuandoErroInesperado() {
		when(scoreClient.getScoreByCpf("12345678901"))
				.thenThrow(new IllegalStateException("erro inesperado"));

		assertThatThrownBy(() -> scoreClientAdapter.getScoreByCpf("12345678901"))
				.isInstanceOf(ScoreUnexpectedResponseException.class)
				.hasMessageContaining("erro inesperado");
	}

	@Test
	@DisplayName("deve chamar o client apenas uma vez no caminho feliz")
	void deveChamarClientApenasUmaVezNoCaminhoFeliz() {
		ScoreResponseDTO expected = new ScoreResponseDTO("12345678901", 800, "EXCELLENT");
		when(scoreClient.getScoreByCpf("12345678901")).thenReturn(expected);

		scoreClientAdapter.getScoreByCpf("12345678901");

		verify(scoreClient).getScoreByCpf("12345678901");
		verify(scoreClient, never()).getScoreByCpf("00000000000");
	}

}