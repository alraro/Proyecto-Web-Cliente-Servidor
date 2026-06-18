/**
 * Servicio de negocio para la gestion de tiendas.
 *
 * Autores:
 * - Alejandra Ortiz: 80%
 * - IA Generativa: 20%
 */
package es.grupo8.backend.services;

import es.grupo8.backend.dao.ChainRepository;
import es.grupo8.backend.dao.PostalCodeRepository;
import es.grupo8.backend.dao.StoreRepository;
import es.grupo8.backend.dto.PaginatedResponse;
import es.grupo8.backend.dto.StoreRequestDto;
import es.grupo8.backend.dto.StoreResponseDto;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.PostalCode;
import es.grupo8.backend.mapper.StoreMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@AllArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ChainRepository chainRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final StoreMapper storeMapper;

    public PaginatedResponse<StoreResponseDto> findAll(String search, Integer chainId, Integer localityId, Integer zoneId, int page, int size, String sort) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        int offset = page * size;

        UtilsService.SortInfo sortInfo = UtilsService.parseSort(sort);

        List<es.grupo8.backend.entity.Store> stores = switch (sortInfo.field()) {
            case "name" -> sortInfo.order().equals("asc")
                    ? storeRepository.findAllByNameAsc(search, chainId, localityId, zoneId, size, offset)
                    : storeRepository.findAllByNameDesc(search, chainId, localityId, zoneId, size, offset);
            default -> sortInfo.order().equals("asc")
                    ? storeRepository.findAllByIdAsc(search, chainId, localityId, zoneId, size, offset)
                    : storeRepository.findAllByIdDesc(search, chainId, localityId, zoneId, size, offset);
        };

        long total = storeRepository.countWithFilters(search, chainId, localityId, zoneId);
        int totalPages = (int) Math.ceil((double) total / size);

        return new PaginatedResponse<>(
                storeMapper.toDTOList(stores),
                page,
                size,
                total,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }

    public StoreResponseDto findById(Integer id) {
        es.grupo8.backend.entity.Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Store not found"));
        return storeMapper.toDTO(store);
    }

    public StoreResponseDto create(StoreRequestDto req) {
        StoreComponents c = resolve(req);
        es.grupo8.backend.entity.Store store = new es.grupo8.backend.entity.Store();
        store.setName(c.name());
        store.setAddress(c.address());
        store.setPostalCode(c.postalCode());
        store.setIdChain(c.chain());
        return storeMapper.toDTO(storeRepository.save(store));
    }

    public StoreResponseDto update(Integer id, StoreRequestDto req) {
        es.grupo8.backend.entity.Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Store not found"));
        StoreComponents c = resolve(req);
        store.setName(c.name());
        store.setAddress(c.address());
        store.setPostalCode(c.postalCode());
        store.setIdChain(c.chain());
        return storeMapper.toDTO(storeRepository.save(store));
    }

    public void delete(Integer id) {
        if (!storeRepository.existsById(id))
            throw new NoSuchElementException("Store not found");
        storeRepository.deleteById(id);
    }

    private StoreComponents resolve(StoreRequestDto req) {
        if (req == null)
            throw new IllegalArgumentException("Request body is required");

        String name = UtilsService.trimToNull(req.getName());
        String address = UtilsService.trimToNull(req.getAddress());
        String cp = UtilsService.trimToNull(req.getPostalCode());

        if (name == null)        throw new IllegalArgumentException("Name is required");
        if (name.length() > 255) throw new IllegalArgumentException("Name cannot exceed 255 characters");
        if (address != null && address.length() > 500)
            throw new IllegalArgumentException("Address cannot exceed 500 characters");

        PostalCode postalCode = null;
        if (cp != null) {
            if (!cp.matches("^[0-9]{5}$"))
                throw new IllegalArgumentException("Postal code must be exactly 5 digits");
            postalCode = postalCodeRepository.findById(cp)
                    .orElseThrow(() -> new IllegalArgumentException("Postal code not found"));
        }

        ChainEntity chain = null;
        if (req.getChainId() != null)
            chain = chainRepository.findById(req.getChainId())
                    .orElseThrow(() -> new IllegalArgumentException("Chain not found"));

        return new StoreComponents(name, address, postalCode, chain);
    }

    private record StoreComponents(String name, String address, PostalCode postalCode, ChainEntity chain) {}
}
