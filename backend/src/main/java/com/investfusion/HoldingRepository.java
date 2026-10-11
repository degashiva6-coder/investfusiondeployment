package com.investfusion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface HoldingRepository extends JpaRepository<Holding, Long> {
    List<Holding> findAllByOwnerId(Long ownerId);
    Optional<Holding> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
