package woowa.gamble.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowa.gamble.domain.RaceMode;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.dto.RaceResultDto;
import woowa.gamble.repository.UserRepository;

import java.util.*;

@Service
public class RacingService {

    @Autowired
    private UserRepository userRepository;

    private static final int FINISH_LINE = 10;
    private static final Long MIN_BET_AMOUNT = 5000L;

    @Transactional
    public RaceResultDto playRace(Long userId, RaceMode mode, String selectedCar, Long betAmount) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (betAmount < MIN_BET_AMOUNT) {
            throw new IllegalArgumentException("최소 배팅 금액은 5,000원입니다.");
        }
        if (user.getMoney() < betAmount) {
            throw new IllegalArgumentException("소지금이 부족합니다.");
        }
        user.setMoney(user.getMoney() - betAmount);

        Map<String, Integer> cars = new LinkedHashMap<>();
        int carCount = mode.getCarCount();
        for (int i = 1; i <= carCount; i++) {
            cars.put(i + "번 자동차", 0);
        }

        boolean isFinished = false;
        while (!isFinished) {
            for (String name : cars.keySet()) {
                if ((int)(Math.random() * 10) >= 4) {
                    int currentPos = cars.get(name) + 1;
                    cars.put(name, currentPos);
                    if (currentPos >= FINISH_LINE) isFinished = true;
                }
            }
        }

        List<String> winners = new ArrayList<>();
        int maxDist = 0;
        for (int pos : cars.values()) {
            if (pos > maxDist) maxDist = pos;
        }
        for (String name : cars.keySet()) {
            if (cars.get(name) == maxDist) {
                winners.add(name);
            }
        }

        boolean isUserWin = winners.contains(selectedCar);
        long reward = 0;

        if (isUserWin) {
            reward = betAmount * mode.getMultiplier();
            user.setMoney(user.getMoney() + reward);
        }
        userRepository.save(user);

        RaceResultDto result = new RaceResultDto();
        result.setWinners(winners);
        result.setUserWin(isUserWin);
        result.setReward(reward);
        result.setUserCarName(selectedCar);

        return result;
    }
}