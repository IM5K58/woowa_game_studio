package woowa.gamble.repository;

import woowa.gamble.domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findTop20ByOrderByMoneyDesc();
}