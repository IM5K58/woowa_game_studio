package woowa.gamble.service;

import woowa.gamble.domain.Lotto;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.domain.WinningRank;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.dto.LottoResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class LottoService {

    @Autowired
    private UserRepository userRepository;

    private static final int TICKET_PRICE = 2000; // 1장당 2000원
    private final String LESS_MONEY = "돈이 부족합니다!";

    @Transactional
    public LottoResultDto playLotto(Long userId, int quantity) {
        UserEntity user = userRepository.findById(userId).orElseThrow();

        long totalCost = (long) quantity * TICKET_PRICE;
        if (user.getMoney() < totalCost) {
            throw new IllegalArgumentException(LESS_MONEY);
        }
        user.setMoney(user.getMoney() - totalCost);

        List<Lotto> userLottos = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            userLottos.add(generateRandomLotto());
        }

        Lotto winningLotto = generateRandomLotto();
        int bonusNumber = generateBonusNumber(winningLotto);

        Map<WinningRank, Integer> resultStats = new HashMap<>();
        long totalPrize = 0;

        for (Lotto userLotto : userLottos) {
            int matchCount = userLotto.matchCount(winningLotto);
            boolean matchBonus = userLotto.contains(bonusNumber);

            WinningRank rank = WinningRank.valueOf(matchCount, matchBonus);

            resultStats.put(rank, resultStats.getOrDefault(rank, 0) + 1);
            totalPrize += rank.getPrize();
        }
        user.setMoney(user.getMoney() + totalPrize);
        userRepository.save(user); // 변경된 돈 저장

        return LottoResultDto.builder()
                .userLottos(userLottos)
                .winningLotto(winningLotto)
                .bonusNumber(bonusNumber)
                .stats(resultStats)
                .totalPrize(totalPrize)
                .earningRate((double) totalPrize / totalCost * 100)
                .build();
    }

    // 1~45 사이 랜덤 로또 번호 생성기
    private Lotto generateRandomLotto() {
        List<Integer> numbers = IntStream.rangeClosed(1, 45)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(numbers); // 섞기
        List<Integer> lottoNumbers = numbers.subList(0, 6);
        lottoNumbers.sort(Comparator.naturalOrder());
        return new Lotto(lottoNumbers);
    }

    private int generateBonusNumber(Lotto winningLotto) {
        while (true) {
            int bonus = (int) (Math.random() * 45) + 1;
            if (!winningLotto.contains(bonus)) {
                return bonus;
            }
        }
    }
}