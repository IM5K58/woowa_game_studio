package woowa.gamble.dto;

import lombok.Builder;
import lombok.Getter;
import woowa.gamble.domain.Lotto;
import woowa.gamble.domain.WinningRank;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class LottoResultDto {
    private List<Lotto> userLottos;
    private Lotto winningLotto;
    private int bonusNumber;
    private Map<WinningRank, Integer> stats;
    private long totalPrize;
    private double earningRate;
}