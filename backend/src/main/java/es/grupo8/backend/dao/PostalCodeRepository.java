package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.PostalCode;

public interface PostalCodeRepository extends JpaRepository<PostalCode, String> {
}