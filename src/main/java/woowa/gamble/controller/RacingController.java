package woowa.gamble.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import woowa.gamble.domain.User;
import woowa.gamble.dto.RaceResultDto;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.service.RacingService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/game/race")
public class RacingController {

    @Autowired
    private RacingService racingService;
    @Autowired
    private UserRepository userRepository;

    // 1. 난이도 선택 로비 (2배, 4배, 8배...)
    @GetMapping("")
    public String lobby(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        User user = userRepository.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "game/race_lobby";
    }

    // 2. 실제 게임 방 (배팅 및 자동차 선택)
    @GetMapping("/{multiplier}")
    public String raceRoom(@PathVariable("multiplier") String multiplierStr,
                           HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate(); // 세션 삭제 (로그아웃)
            return "redirect:/";  // 메인으로 이동
        }

        int multiplier;
        int carCount;
        String title;

        // 배율에 따른 자동차 수 설정
        if ("???".equals(multiplierStr)) {
            multiplier = 1000;
            carCount = 80;
            title = "??? (지옥의 레이스)";
        } else {
            try {
                multiplier = Integer.parseInt(multiplierStr);
                // 요구사항에 맞춘 배율별 자동차 수
                if (multiplier == 2) carCount = 2;
                else if (multiplier == 4) carCount = 4;
                else if (multiplier == 8) carCount = 10;
                else if (multiplier == 16) carCount = 20;
                else return "redirect:/game/race"; // 잘못된 배율
                title = multiplier + "배 레이스";
            } catch (NumberFormatException e) {
                return "redirect:/game/race";
            }
        }

        // 선택할 수 있는 자동차 목록 생성
        List<String> carList = new ArrayList<>();
        for(int i=1; i<=carCount; i++) carList.add(i+"번마");

        model.addAttribute("user", user);
        model.addAttribute("multiplier", multiplier);
        model.addAttribute("carCount", carCount);
        model.addAttribute("title", title);
        model.addAttribute("carList", carList);

        return "game/race_room";
    }

    // 3. 게임 시작 및 결과 처리 (AJAX 대신 HTML 반환 방식으로 구현)
    @PostMapping("/play")
    public String playRace(@RequestParam int multiplier,
                           @RequestParam int carCount,
                           @RequestParam String selectedCar,
                           @RequestParam int betAmount,
                           Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        try {
            RaceResultDto result = racingService.playRace(userId, carCount, multiplier, selectedCar, betAmount);
            model.addAttribute("result", result);

            // 게임이 끝나고 다시 같은 방 정보를 보여주기 위해 필요한 정보들
            model.addAttribute("multiplier", multiplier);
            model.addAttribute("carCount", carCount);
            model.addAttribute("title", multiplier == 1000 ? "??? (지옥의 레이스)" : multiplier + "배 레이스");

            List<String> carList = new ArrayList<>();
            for(int i=1; i<=carCount; i++) carList.add(i+"번마");
            model.addAttribute("carList", carList);

            // 갱신된 돈 정보
            User user = userRepository.findById(userId).orElse(null);
            model.addAttribute("user", user);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/game/race/" + (multiplier == 1000 ? "???" : multiplier);
        }

        return "game/race_room";
    }
}