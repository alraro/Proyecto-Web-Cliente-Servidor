package es.grupo8.backend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import es.grupo8.backend.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {
}