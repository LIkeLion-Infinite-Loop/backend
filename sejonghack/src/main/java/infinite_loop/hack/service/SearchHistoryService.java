package infinite_loop.hack.service;

import infinite_loop.hack.domain.ProductSearchHistory;
import infinite_loop.hack.domain.User;
import infinite_loop.hack.repository.ProductSearchHistoryRepository;
import infinite_loop.hack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final ProductSearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;

    // 검색 키워드 저장
    public void saveSearchKeyword(Long userId, String keyword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        ProductSearchHistory history = ProductSearchHistory.builder()
                .user(user)
                .keyword(keyword)
                .build();

        searchHistoryRepository.save(history);
    }

    // 최근 검색 기록 10개 조회
    public List<String> getRecentSearchKeywords(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        return searchHistoryRepository.findTop10ByUserOrderBySearchedAtDesc(user)
                .stream()
                .map(ProductSearchHistory::getKeyword)
                .toList();
    }
}
