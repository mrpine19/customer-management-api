package com.br.customer.repository;

import com.br.customer.model.Customer;
import com.br.customer.model.StatusEnum;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CustomerJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_BY_STATUS_SQL = """
            SELECT id, name, cpf, email, phone, status, birth_date, created_at, updated_at
            FROM customer
            WHERE status = ?
            """;

    private static final RowMapper<Customer> CUSTOMER_ROW_MAPPER = new CustomerRowMapper();

    public CustomerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Customer> findByStatus(StatusEnum status) {
        return jdbcTemplate.query(FIND_BY_STATUS_SQL, CUSTOMER_ROW_MAPPER, status.name());
    }

    private static class CustomerRowMapper implements RowMapper<Customer> {
        @Override
        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
            Customer customer = new Customer();
            customer.setId(rs.getLong("id"));
            customer.setName(rs.getString("name"));
            customer.setCpf(rs.getString("cpf"));
            customer.setEmail(rs.getString("email"));
            customer.setPhone(rs.getString("phone"));
            customer.setStatus(StatusEnum.valueOf(rs.getString("status")));
            customer.setBirthDate(rs.getDate("birth_date").toLocalDate());
            customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return customer;
        }
    }
}