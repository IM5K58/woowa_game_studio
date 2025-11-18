package woowa.gamble.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import woowa.gamble.domain.User;
import woowa.gamble.dto.RaceResultDto;
import woowa.gamble.repository.UserRepository;

import java.util.*;

@Service
public class RacingService {

    @Autowired
    private UserRepository userRepository;

    private static final int FINISH_LINE = 10; // 10칸 가면 결승선 통과
    private static final int MIN_BET_AMOUNT = 5000;

    @Transactional
    public RaceResultDto playRace(Long userId, int carCount, int multiplier, String selectedCar, int betAmount) {
        // 1. 유저 검증
        User user = userRepository.findById(userId).orElseThrow();

        // 2. 돈 검증
        if (betAmount < MIN_BET_AMOUNT) {
            throw new IllegalArgumentException("최소 배팅 금액은 5,000원입니다.");
        }
        if (user.getMoney() < betAmount) {
            throw new IllegalArgumentException("소지금이 부족합니다.");
        }

        // 3. 돈 차감
        user.setMoney(user.getMoney() - betAmount);

        // 4. 자동차 생성 (Car 1, Car 2...)
        Map<String, Integer> cars = new LinkedHashMap<>();
        // 사용자가 선택한 차가 목록에 확실히 있어야 하므로 이름 생성 시 주의
        for (int i = 1; i <= carCount; i++) {
            cars.put(i + "번 자동차", 0); // 초기 위치 0
        }

        // 5. 경주 시작 (시뮬레이션)
        List<Map<String, Integer>> history = new ArrayList<>();
        boolean isFinished = false;

        while (!isFinished) {
            // 이번 라운드 결과를 담을 맵 (깊은 복사)
            Map<String, Integer> currentRound = new LinkedHashMap<>();

            for (String name : cars.keySet()) {
                int currentPos = cars.get(name);

                // 랜덤값 (0~9) 중 4 이상이면 전진 (기존 로직 활용)
                if ((int)(Math.random() * 10) >= 4) {
                    currentPos++;
                }

                cars.put(name, currentPos); // 위치 업데이트
                currentRound.put(name, currentPos); // 기록

                if (currentPos >= FINISH_LINE) {
                    isFinished = true;
                }
            }
            history.add(currentRound); // 역사에 기록
        }

        // 6. 우승자 찾기
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

        // 7. 당첨 여부 확인 및 정산
        boolean isUserWin = winners.contains(selectedCar);
        long reward = 0;

        if (isUserWin) {
            reward = (long) betAmount * multiplier;
            user.setMoney(user.getMoney() + reward);
        }
        userRepository.save(user);

        // 8. 결과 반환
        RaceResultDto result = new RaceResultDto();
        result.setHistory(history);
        result.setWinners(winners);
        result.setUserWin(isUserWin);
        result.setReward(reward);
        result.setUserCarName(selectedCar);

        return result;
    }
}