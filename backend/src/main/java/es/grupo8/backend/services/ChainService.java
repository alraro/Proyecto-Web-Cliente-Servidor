package es.grupo8.backend.services;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dto.ChainDTO;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.mapper.ChainMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class ChainService {

    private final ChainRepository chainRepository;
    private final ChainMapper chainMapper;

    public List<ChainDTO> findAll() {
        return chainMapper.toDTOList(chainRepository.findAllByOrderByIdChainAsc());
    }

    public ChainDTO findById(Integer id) {
        ChainEntity entity = chainRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chain not found"));
        return chainMapper.toDTO(entity);
    }

    public ChainDTO create(ChainDTO request) {
        validate(request);
        if (chainRepository.existsByCode(request.getCode().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A chain with that code already exists");
        }
        ChainEntity entity = new ChainEntity();
        entity.setName(request.getName().trim());
        entity.setCode(request.getCode().trim());
        entity.setParticipation(request.getParticipation() != null ? request.getParticipation() : false);
        return chainMapper.toDTO(chainRepository.save(entity));
    }

    public ChainDTO update(Integer id, ChainDTO request) {
        validate(request);
        ChainEntity entity = chainRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chain not found"));

        String newCode = request.getCode().trim();
        if (!newCode.equals(entity.getCode()) && chainRepository.existsByCode(newCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A chain with that code already exists");
        }
        entity.setName(request.getName().trim());
        entity.setCode(newCode);
        if (request.getParticipation() != null) entity.setParticipation(request.getParticipation());
        return chainMapper.toDTO(chainRepository.save(entity));
    }

    public void delete(Integer id) {
        if (!chainRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Chain not found");
        }
        chainRepository.deleteById(id);
    }

    private void validate(ChainDTO req) {
        if (req == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");
        String name = req.getName() == null ? "" : req.getName().trim();
        String code = req.getCode() == null ? "" : req.getCode().trim();
        if (name.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        if (name.length() > 255) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        if (code.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code is required");
        if (!code.matches("^[A-Za-z0-9_\\-]+$") || code.length() > 50)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Code can only contain letters, numbers, hyphens and underscores (max 50 characters)");
    }
}