package com.br.customer.dtos;

import com.br.customer.model.StatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CustomerRequestDTO(

        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 120, message = "Name must have between 3 and 120 characters")
        String name,

        @NotBlank(message = "CPF is required")
        @Pattern(regexp = "^\\d{11}$", message = "CPF must contain exactly 11 digits")
        String cpf,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email must have at most 150 characters")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\d{10,11}$", message = "Phone must contain 10 or 11 digits")
        String phone,

        @NotNull(message = "Status is required")
        StatusEnum status,

        @NotNull(message = "Birth date is required")
        @Past(message = "Birth date must be in the past")
        LocalDate birthDate
) {
}
