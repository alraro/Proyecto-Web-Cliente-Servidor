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
import es.grupo8.backend.dto.StoreDTO;
import es.grupo8.backend.entity.ChainEntity;
import es.grupo8.backend.entity.PostalCode;
import es.grupo8.backend.mapper.StoreMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final ChainRepository chainRepository;
    private final PostalCodeRepository postalCodeRepository;
    private final StoreMapper storeMapper;

    public Map<String, Object> findAll(Integer chainId, Integer localityId, Integer zoneId, int page, int size) {
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

        List<StoreDTO> all = storeMapper.toDTOList(stores);

        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        long totalElements = all.size();
        int  totalPages = (int) Math.ceil((double) totalElements / size);
        int  from = page * size;
        int  to = Math.min(from + size, (int) totalElements);
        List<StoreDTO> content = from >= all.size() ? List.of() : all.subList(from, to);

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("hasNext", page < totalPages - 1);
        response.put("hasPrevious", page > 0);
        return response;
    }

    public StoreDTO findById(Integer id) {
        es.grupo8.backend.entity.Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        return storeMapper.toDTO(store);
    }

    public StoreDTO create(StoreDTO req) {
        StoreComponents c = resolve(req);
        es.grupo8.backend.entity.Store store = new es.grupo8.backend.entity.Store();
        store.setName(c.name());
        store.setAddress(c.address());
        store.setPostalCode(c.postalCode());
        store.setIdChain(c.chain());
        return storeMapper.toDTO(storeRepository.save(store));
    }

    public StoreDTO update(Integer id, StoreDTO req) {
        es.grupo8.backend.entity.Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found"));
        StoreComponents c = resolve(req);
        store.setName(c.name());
        store.setAddress(c.address());
        store.setPostalCode(c.postalCode());
        store.setIdChain(c.chain());
        return storeMapper.toDTO(storeRepository.save(store));
    }

    public void delete(Integer id) {
        if (!storeRepository.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Store not found");
        storeRepository.deleteById(id);
    }

    private StoreComponents resolve(StoreDTO req) {
        if (req == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body is required");

        String name = trimToNull(req.getName());
        String address = trimToNull(req.getAddress());
        String cp = trimToNull(req.getPostalCode());

        if (name == null)        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        if (name.length() > 255) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot exceed 255 characters");
        if (address != null && address.length() > 500)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Address cannot exceed 500 characters");

        PostalCode postalCode = null;
        if (cp != null) {
            if (!cp.matches("^[0-9]{5}$"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code must be exactly 5 digits");
            postalCode = postalCodeRepository.findById(cp)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code not found"));
        }

        ChainEntity chain = null;
        if (req.getChainId() != null)
            chain = chainRepository.findById(req.getChainId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chain not found"));

        return new StoreComponents(name, address, postalCode, chain);
    }

    private static String trimToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private record StoreComponents(String name, String address, PostalCode postalCode, ChainEntity chain) {}
}