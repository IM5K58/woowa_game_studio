package woowa.gamble.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import woowa.gamble.domain.RaceMode;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.dto.RaceResultDto;
import woowa.gamble.dto.UserDto; // DTO 사용
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
        UserEntity user = getUser(session);
        if (user == null) return "redirect:/";

        model.addAttribute("user", new UserDto(user));
        return "game/race_lobby";
    }

    @GetMapping("/{mode}")
    public String raceRoom(@PathVariable("mode") String modeStr, HttpSession session, Model model) {
        UserEntity user = getUser(session);
        if (user == null) return "redirect:/";

        try {
            RaceMode mode = RaceMode.from(modeStr);
            setupRaceRoomModel(model, user, mode);

        } catch (IllegalArgumentException e) {
            return "redirect:/race";
        }
        return "game/race_room";
    }

    @PostMapping("/play")
    public String playRace(@RequestParam int multiplier,
                           @RequestParam String selectedCar,
                           @RequestParam Long betAmount,
                           Model model, HttpSession session) {
        UserEntity user = getUser(session);
        if (user == null) return "redirect:/";

        try {
            RaceMode mode = RaceMode.fromMultiplier(multiplier);

            setupRaceRoomModel(model, user, mode);
            RaceResultDto result = racingService.playRace(user.getId(), mode, selectedCar, betAmount);

            model.addAttribute("result", result);
            model.addAttribute("user", new UserDto(userRepository.findById(user.getId()).get())); // 갱신된 돈 반영

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            return "redirect:/";
        }

        return "game/race_room";
    }

    private void setupRaceRoomModel(Model model, UserEntity user, RaceMode mode) {
        List<String> carList = new ArrayList<>();
        for(int i=1; i<= mode.getCarCount(); i++) carList.add(i+"번 자동차");

        model.addAttribute("user", new UserDto(user));
        model.addAttribute("title", mode.getTitle());
        model.addAttribute("multiplier", mode.getMultiplier());
        model.addAttribute("carCount", mode.getCarCount());
        model.addAttribute("carList", carList);
        model.addAttribute("multiplierStr", mode.getUrlPath());
    }

    private UserEntity getUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return null;
        return userRepository.findById(userId).orElse(null);
    }

}