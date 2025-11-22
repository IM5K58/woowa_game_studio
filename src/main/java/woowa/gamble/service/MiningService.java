package woowa.gamble.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.repository.UserRepository;

@Service
public class MiningService {

    @Autowired
    private UserRepository userRepository;

    // 클릭 한 번당 100원 추가
    @Transactional
    public Long mine(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();

        user.setMoney(user.getMoney() + 100L);
        userRepository.save(user);

        return user.getMoney(); // 갱신된 돈 반환
    }

    @Transactional
    public boolean claimHiddenReward(Long userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow();

        if (user.isHiddenRewardReceived()) {
            return false;
        }

        // 안 받았으면 100억 지급
        user.setMoney(user.getMoney() + 10_000_000_000L);
        user.setHiddenRewardReceived(true); // 받음 표시

        return true;
    }
}