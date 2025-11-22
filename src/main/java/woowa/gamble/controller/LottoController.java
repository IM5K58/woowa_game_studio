package woowa.gamble.controller;

import woowa.gamble.domain.UserEntity;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.service.LottoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class LottoController {

    @Autowired
    private LottoService lottoService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/game/lotto")
    public String lottoPage(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        UserEntity user = userRepository.findById(userId).orElseThrow();
        model.addAttribute("user", user);
        return "game/lotto";
    }

    // 2. 로또 구매 및 결과 처리
    @PostMapping("/game/lotto/buy")
    public String buyLotto(@RequestParam("quantity") int quantity,
                           Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        try {
            // 서비스에게 게임 진행 시킴
            Map<String, Object> result = lottoService.playLotto(userId, quantity);

            model.addAttribute("result", result);
            model.addAttribute("message", quantity + "장을 구매했습니다!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }

        UserEntity user = userRepository.findById(userId).orElseThrow();
        model.addAttribute("user", user);

        return "game/lotto";
    }
}