package infinite_loop.hack.repository;

import infinite_loop.hack.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}