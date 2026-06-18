/**
 * Servicio de negocio para la gestion de cadenas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dto.ChainRequestDto;
import es.grupo8.backend.dto.ChainResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.mapper.ChainMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class ChainService {

    private final ChainRepository chainRepository;
    private final ChainMapper chainMapper;

    public List<ChainResponseDto> findAll() {
        return chainMapper.toDTOList(chainRepository.findAllByOrderByIdChainAsc());
    }

    public ChainResponseDto findById(Integer id) {
        ChainEntity entity = chainRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chain not found"));
        return chainMapper.toDTO(entity);
    }

    public ChainResponseDto create(ChainRequestDto request) {
        validate(request);
        if (chainRepository.existsByCode(request.getCode().trim())) {
            throw new IllegalArgumentException("A chain with that code already exists");
        }
        ChainEntity entity = new ChainEntity();
        entity.setName(request.getName().trim());
        entity.setCode(request.getCode().trim());
        entity.setParticipation(request.getParticipation() != null ? request.getParticipation() : false);
        return chainMapper.toDTO(chainRepository.save(entity));
    }

    public ChainResponseDto update(Integer id, ChainRequestDto request) {
        validate(request);
        ChainEntity entity = chainRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chain not found"));

        String newCode = request.getCode().trim();
        if (!newCode.equals(entity.getCode()) && chainRepository.existsByCode(newCode)) {
            throw new IllegalArgumentException("A chain with that code already exists");
        }
        entity.setName(request.getName().trim());
        entity.setCode(newCode);
        if (request.getParticipation() != null) entity.setParticipation(request.getParticipation());
        return chainMapper.toDTO(chainRepository.save(entity));
    }

    public void delete(Integer id) {
        if (!chainRepository.existsById(id)) {
            throw new NoSuchElementException("Chain not found");
        }
        chainRepository.deleteById(id);
    }

    private void validate(ChainRequestDto req) {
        if (req == null)
            throw new IllegalArgumentException("Request body is required");
        String name = req.getName() == null ? "" : req.getName().trim();
        String code = req.getCode() == null ? "" : req.getCode().trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name is required");
        if (name.length() > 255) throw new IllegalArgumentException("Name cannot exceed 255 characters");
        if (code.isEmpty()) throw new IllegalArgumentException("Code is required");
        if (!code.matches("^[A-Za-z0-9_\\-]+$") || code.length() > 50)
            throw new IllegalArgumentException("Code can only contain letters, numbers, hyphens and underscores (max 50 characters)");
    }
}
