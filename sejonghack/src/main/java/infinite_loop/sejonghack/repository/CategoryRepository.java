package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}