package woowa.gamble.domain;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum RaceMode {
    X2("2", 2, 2, "2배 레이스"),
    X4("4", 4, 4, "4배 레이스"),
    X8("8", 8, 10, "8배 레이스"),
    X16("16", 16, 20, "16배 레이스"),
    HELL("hell", 1000, 80, "??? (지옥의 레이스)");

    private final String urlPath;   // URL 경로로 들어오는 값 (예: "2", "hell")
    private final int multiplier;   // 실제 배율
    private final int carCount;     // 자동차 수
    private final String title;     // 화면에 보여줄 제목

    RaceMode(String urlPath, int multiplier, int carCount, String title) {
        this.urlPath = urlPath;
        this.multiplier = multiplier;
        this.carCount = carCount;
        this.title = title;
    }

    // URL 경로 값으로 적절한 모드를 찾아주는 메서드
    public static RaceMode from(String path) {
        return Arrays.stream(values())
                .filter(mode -> mode.urlPath.equals(path))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게임 모드입니다."));
    }

    // 배율 정수값으로 모드 찾기 (Play 요청 처리용)
    public static RaceMode fromMultiplier(int multiplier) {
        return Arrays.stream(values())
                .filter(mode -> mode.multiplier == multiplier)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 배율입니다."));
    }
}