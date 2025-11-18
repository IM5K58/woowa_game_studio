package woowa.gamble.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Map;

@Getter @Setter
public class RaceResultDto {
    private List<Map<String, Integer>> history; // 매 라운드별 자동차들의 위치 (Key: 차이름, Value: 거리)
    private List<String> winners;               // 최종 우승자 이름 목록
    private boolean isUserWin;                  // 사용자가 이겼는지 여부
    private long reward;                        // 획득한 상금
    private String userCarName;                 // 사용자가 선택한 차
}