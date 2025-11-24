// woowa.gamble.controller.RacingController.java (수정 제안)

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
    public String raceRoom(@PathVariable("multiplier") String multiplierStr, HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";
        try {
            loadRaceRoomAttributes(userId, multiplierStr, model);
        } catch (IllegalArgumentException e) {
            return "redirect:/race";
        }
        return "game/race_room";
    }

    @PostMapping("/play")
    public String playRace(@RequestParam int multiplier,
                           @RequestParam int carCount,
                           @RequestParam String selectedCar,
                           @RequestParam Long betAmount,
                           Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        try {
            String multiplierStr = (multiplier == 1000) ? "hell" : String.valueOf(multiplier);
            loadRaceRoomAttributes(userId, multiplierStr, model);

            RaceResultDto result = racingService.playRace(userId, carCount, multiplier, selectedCar, betAmount);
            model.addAttribute("result", result);

            UserEntity updatedUser = userRepository.findById(userId).orElse(null);
            model.addAttribute("user", updatedUser); // 갱신된 소지금 반영

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());

        } catch (Exception e) {
            session.invalidate();
            return "redirect:/";
        }

        return "game/race_room";
    }

    private void loadRaceRoomAttributes(Long userId, String multiplierStr, Model model) {
        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        int multiplier;
        int carCount;
        String title;

        if ("hell".equals(multiplierStr)) {
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
                else throw new IllegalArgumentException("잘못된 배율입니다.");
                title = multiplier + "배 레이스";
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("잘못된 배율 형식입니다.");
            }
        }

        List<String> carList = new ArrayList<>();
        for(int i=1; i<=carCount; i++) carList.add(i+"번 자동차");

        model.addAttribute("user", user);
        model.addAttribute("multiplier", multiplier);
        model.addAttribute("carCount", carCount);
        model.addAttribute("title", title);
        model.addAttribute("carList", carList);
        model.addAttribute("multiplierStr", multiplierStr);
    }
}