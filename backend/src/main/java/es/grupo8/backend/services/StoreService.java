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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ChainRepository chainRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final StoreMapper storeMapper;

    public PaginatedResponse<StoreResponseDto> findAll(Integer chainId, Integer localityId, Integer zoneId, int page, int size) {
        List<es.grupo8.backend.entity.Store> stores = storeRepository.findAllByOrderByIdAsc();

        if (chainId != null)
            stores = stores.stream()
                    .filter(s -> s.getIdChain() != null && chainId.equals(s.getIdChain().getIdChain()))
                    .collect(Collectors.toList());
        if (localityId != null)
            stores = stores.stream()
                    .filter(s -> s.getPostalCode() != null
                            && s.getPostalCode().getIdLocality() != null
                            && localityId.equals(s.getPostalCode().getIdLocality().getId()))
                    .collect(Collectors.toList());
        if (zoneId != null)
            stores = stores.stream()
                    .filter(s -> s.getPostalCode() != null
                            && s.getPostalCode().getIdLocality() != null
                            && s.getPostalCode().getIdLocality().getIdZone() != null
                            && zoneId.equals(s.getPostalCode().getIdLocality().getIdZone().getId()))
                    .collect(Collectors.toList());

        List<StoreResponseDto> all = storeMapper.toDTOList(stores);

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        long totalElements = all.size();
        int  totalPages = (int) Math.ceil((double) totalElements / size);
        int  from = page * size;
        int  to = Math.min(from + size, (int) totalElements);
        List<StoreResponseDto> content = from >= all.size() ? List.of() : all.subList(from, to);

        return new PaginatedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page < totalPages - 1,
                page > 0
        );
    }

    public StoreResponseDto findById(Integer id) {
        es.grupo8.backend.entity.Store store = storeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Store not found"));
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
                .orElseThrow(() -> new RuntimeException("Store not found"));
        StoreComponents c = resolve(req);
        store.setName(c.name());
        store.setAddress(c.address());
        store.setPostalCode(c.postalCode());
        store.setIdChain(c.chain());
        return storeMapper.toDTO(storeRepository.save(store));
    }

    public void delete(Integer id) {
        if (!storeRepository.existsById(id))
            throw new RuntimeException("Store not found");
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
