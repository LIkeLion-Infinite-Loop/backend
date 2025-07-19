package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.BarcodeGuide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarcodeGuideRepository extends JpaRepository<BarcodeGuide, Long> {

    Optional<BarcodeGuide> findByProductNameContaining(String productName);

    Optional<BarcodeGuide> findByBarcode(String barcode);
}