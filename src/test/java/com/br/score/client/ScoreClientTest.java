package com.br.score.client;

import com.br.score.dto.ScoreResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreClientTest {

	@Mock(answer = RETURNS_DEEP_STUBS)
	private RestClient restClient;

	@InjectMocks
	private ScoreClient scoreClient;

	@Test
	@DisplayName("deve retornar score quando a chamada HTTP for bem-sucedida")
	void deveRetornarScoreQuandoChamadaHttpForBemSucedida() {
		ScoreResponseDTO expected = new ScoreResponseDTO("12345678901", 750, "GOOD");

		when(restClient.get().uri("/scores/{cpf}", "12345678901").retrieve().body(ScoreResponseDTO.class))
				.thenReturn(expected);

		ScoreResponseDTO result = scoreClient.getScoreByCpf("12345678901");

		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("deve retornar score quando cpf for vazio")
	void deveRetornarScoreQuandoCpfForVazio() {
		ScoreResponseDTO expected = new ScoreResponseDTO("", 300, "LOW");

		when(restClient.get().uri("/scores/{cpf}", "").retrieve().body(ScoreResponseDTO.class))
				.thenReturn(expected);

		ScoreResponseDTO result = scoreClient.getScoreByCpf("");

		assertThat(result).isEqualTo(expected);
	}

	@Test
	@DisplayName("deve retornar null quando o response body vier vazio")
	void deveRetornarNullQuandoResponseBodyVierVazio() {
		when(restClient.get().uri("/scores/{cpf}", "12345678901").retrieve().body(ScoreResponseDTO.class))
				.thenReturn(null);

		ScoreResponseDTO result = scoreClient.getScoreByCpf("12345678901");

		assertThat(result).isNull();
	}

	@Test
	@DisplayName("deve propagar exceção quando get do RestClient falhar")
	void devePropagarExcecaoQuandoGetDoRestClientFalhar() {
		when(restClient.get()).thenThrow(new IllegalStateException("falha ao iniciar request"));

		assertThatThrownBy(() -> scoreClient.getScoreByCpf("12345678901"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("falha ao iniciar request");
	}

	@Test
	@DisplayName("deve propagar exceção quando retrieve falhar")
	void devePropagarExcecaoQuandoRetrieveFalhar() {
		when(restClient.get().uri("/scores/{cpf}", "12345678901").retrieve())
				.thenThrow(new RuntimeException("erro no retrieve"));

		assertThatThrownBy(() -> scoreClient.getScoreByCpf("12345678901"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("erro no retrieve");
	}

	@Test
	@DisplayName("deve propagar exceção quando body falhar")
	void devePropagarExcecaoQuandoBodyFalhar() {
		when(restClient.get().uri("/scores/{cpf}", "12345678901").retrieve().body(ScoreResponseDTO.class))
				.thenThrow(new RuntimeException("erro no body"));

		assertThatThrownBy(() -> scoreClient.getScoreByCpf("12345678901"))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("erro no body");
	}
}