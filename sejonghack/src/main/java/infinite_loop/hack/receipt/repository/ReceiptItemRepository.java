package infinite_loop.hack.receipt.repository;

import infinite_loop.hack.receipt.domain.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {
    List<ReceiptItem> findAllByReceiptIdOrderByIdAsc(Long receiptId);
}