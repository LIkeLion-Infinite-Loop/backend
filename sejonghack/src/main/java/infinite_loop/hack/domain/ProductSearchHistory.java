package infinite_loop.hack.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_search_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 검색한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 검색 키워드
    @Column(nullable = false, length = 255)
    private String keyword;

    // 검색 시각
    @Column(name = "searched_at")
    private LocalDateTime searchedAt = LocalDateTime.now();
}
