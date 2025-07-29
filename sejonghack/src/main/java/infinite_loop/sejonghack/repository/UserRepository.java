package infinite_loop.sejonghack.repository;

import infinite_loop.sejonghack.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 찾기 (로그인, 인증용)
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 찾기 (중복 체크 등)
    Optional<User> findByNickname(String nickname);

    // 이메일 중복 여부 체크 (회원가입 시 필요)
    boolean existsByEmail(String email);


}