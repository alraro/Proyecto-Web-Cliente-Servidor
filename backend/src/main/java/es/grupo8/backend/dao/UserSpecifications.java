package es.grupo8.backend.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.springframework.data.jpa.domain.Specification;

import es.grupo8.backend.entity.AdminEntity;
import es.grupo8.backend.entity.Captain;
import es.grupo8.backend.entity.Coordinator;
import es.grupo8.backend.entity.PartnerEntityManager;
import es.grupo8.backend.entity.UserEntity;

/**
 * Specifications for filtering UserEntity queries dynamically.
 */
public class UserSpecifications {

    /**
     * Filters users by search term (name or ID).
     */
    public static Specification<UserEntity> hasSearchTerm(String search) {
        return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (search == null || search.trim().isEmpty()) {
                return cb.conjunction();
            }
            String searchTrimmed = search.trim();
            String like = "%" + searchTrimmed.toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("name")), like);
            
            // Try to parse as integer for ID search
            try {
                Integer id = Integer.parseInt(searchTrimmed);
                Predicate idEqual = cb.equal(root.get("idUser"), id);
                return cb.or(nameLike, idEqual);
            } catch (NumberFormatException e) {
                // Not a number, just search by name
                return nameLike;
            }
        };
    }

    /**
     * Filters users by role.
     * Valid roles: ADMIN, COORDINATOR, CAPTAIN, PARTNER_ENTITY_MANAGER
     */
    public static Specification<UserEntity> hasRole(String role) {
        return (Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (role == null || role.trim().isEmpty()) {
                return cb.conjunction();
            }
            String normalizedRole = role.trim().toUpperCase();

            return switch (normalizedRole) {
                case "ADMIN" -> {
                    Subquery<Integer> adminSub = query.subquery(Integer.class);
                    Root<AdminEntity> adminRoot = adminSub.from(AdminEntity.class);
                    adminSub.select(adminRoot.get("idUser"));
                    yield cb.in(root.get("idUser")).value(adminSub);
                }
                case "COORDINATOR" -> {
                    Subquery<Integer> coordSub = query.subquery(Integer.class);
                    Root<Coordinator> coordRoot = coordSub.from(Coordinator.class);
                    coordSub.select(coordRoot.get("idUser").get("idUser"));
                    yield cb.in(root.get("idUser")).value(coordSub);
                }
                case "CAPTAIN" -> {
                    Subquery<Integer> capSub = query.subquery(Integer.class);
                    Root<Captain> capRoot = capSub.from(Captain.class);
                    capSub.select(capRoot.get("idUser").get("idUser"));
                    yield cb.in(root.get("idUser")).value(capSub);
                }
                case "PARTNER_ENTITY_MANAGER" -> {
                    Subquery<Integer> pemSub = query.subquery(Integer.class);
                    Root<PartnerEntityManager> pemRoot = pemSub.from(PartnerEntityManager.class);
                    pemSub.select(pemRoot.get("id"));
                    yield cb.in(root.get("idUser")).value(pemSub);
                }
                default -> cb.conjunction();
            };
        };
    }
}
