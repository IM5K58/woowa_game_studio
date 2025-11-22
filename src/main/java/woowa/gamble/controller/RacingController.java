package woowa.gamble.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.dto.RaceResultDto;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.service.RacingService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/race")
public class RacingController {

    @Autowired
    private RacingService racingService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("")
    public String lobby(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        UserEntity user = userRepository.findById(userId).orElse(null);
        model.addAttribute("user", user);
        return "game/race_lobby";
    }

    @GetMapping("/{multiplier}")
    public String raceRoom(@PathVariable("multiplier") String multiplierStr,
                           HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/";
        }

        int multiplier = 0;
        int carCount = 0;
        String title = "";

        // [수정] ??? 배율 처리 로직 강화
        if ("???".equals(multiplierStr)) {
            multiplier = 1000;
            carCount = 80;
            title = "??? (지옥의 레이스)";
        } else {
            try {
                multiplier = Integer.parseInt(multiplierStr);
                if (multiplier == 2) carCount = 2;
                else if (multiplier == 4) carCount = 4;
                else if (multiplier == 8) carCount = 10;
                else if (multiplier == 16) carCount = 20;
                else return "redirect:/race";
                title = multiplier + "배 레이스";
            } catch (NumberFormatException e) {
                return "redirect:/race";
            }
        }

        List<String> carList = new ArrayList<>();
        for(int i=1; i<=carCount; i++) carList.add(i+"번 자동차");

        model.addAttribute("user", user);
        model.addAttribute("multiplier", multiplier); // 여기에는 숫자(1000)가 들어감
        model.addAttribute("carCount", carCount);
        model.addAttribute("title", title);
        model.addAttribute("carList", carList);

        // HTML에서 링크 생성할 때 쓰기 위해 원래 문자열도 넘겨줌
        model.addAttribute("multiplierStr", multiplierStr);

        return "game/race_room";
    }

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

            // 결과 화면 유지용 데이터 세팅
            String multiplierStr = (multiplier == 1000) ? "???" : String.valueOf(multiplier);
            String title = (multiplier == 1000) ? "??? (지옥의 레이스)" : multiplier + "배 레이스";

            model.addAttribute("multiplier", multiplier);
            model.addAttribute("multiplierStr", multiplierStr); // 중요!
            model.addAttribute("carCount", carCount);
            model.addAttribute("title", title);

            List<String> carList = new ArrayList<>();
            for(int i=1; i<=carCount; i++) carList.add(i+"번 자동차");
            model.addAttribute("carList", carList);

            UserEntity user = userRepository.findById(userId).orElse(null);
            model.addAttribute("user", user);

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            // 에러 발생 시 리다이렉트 주소 처리
            String redirectUrl = (multiplier == 1000) ? "???" : String.valueOf(multiplier);
            return "redirect:/race/" + redirectUrl;
        }

        return "game/race_room";
    }
}