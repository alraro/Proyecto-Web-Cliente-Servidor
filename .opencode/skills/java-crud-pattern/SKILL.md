---
name: java-crud-pattern
description: How to create a new CRUD module (Entity → DTO → Mapper → Repository → Service → Controller REST)
---

## Steps to create a new CRUD module

### 1. Entity (`entity/`)

```java
@Entity
@Table(name = "table_name")
public class XxxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // fields with @Column
}
```

### 2. DTOs (`dto/`)

- `XxxRequestDto.java` with `@Data` (Lombok) for input
- `XxxResponseDto.java` as `record` for output

### 3. Mapper (`mapper/`)

```java
@Component
public class XxxMapper extends MapperDTO<XxxResponseDto, XxxEntity> {
    @Override
    public XxxResponseDto toDTO(XxxEntity entity) { ... }
}
```

### 4. Repository (`dao/`)

```java
public interface XxxRepository extends JpaRepository<XxxEntity, Integer> {
    // custom @Query methods if needed
}
```

### 5. Service (`services/`)

Inject the repository and mapper. Use `mapper.toDTO()` and `mapper.toDTOList()` — never manual mapping.

### 6. Controller REST (`controllers/rest/`)

```java
@RestController
@RequestMapping("/api/xxx")
public class XxxController {
    @Autowired private XxxService xxxService;
    @Autowired private AuthService authService;
    @Autowired private UserService userService;

    private void checkAdmin(String auth) { ... }

    @GetMapping
    public ResponseEntity<List<XxxResponseDto>> getAll(@RequestHeader("Authorization") String auth) {
        checkAdmin(auth);
        return ResponseEntity.ok(xxxService.getAll());
    }
    // POST, PUT, DELETE similarly
}
```

## Important

- Services never do manual mapping — always delegate to the mapper
- Every REST endpoint must have a `checkAdmin()` or `checkAdminOrEntityManager()` guard
- Use `@ExceptionHandler(AuthException.class)` at the bottom
