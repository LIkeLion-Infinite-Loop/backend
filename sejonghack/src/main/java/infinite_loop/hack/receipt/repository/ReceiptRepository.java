package infinite_loop.hack.receipt.repository;

import infinite_loop.hack.receipt.domain.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}