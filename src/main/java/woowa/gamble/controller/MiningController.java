package woowa.gamble.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.service.MiningService;

import java.util.Collections;
import java.util.Map;

@Controller
public class MiningController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MiningService miningService;

    @GetMapping("/mining")
    public String miningPage(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return "redirect:/";

        UserEntity user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            session.invalidate();
            return "redirect:/";
        }

        model.addAttribute("user", user);
        return "game/mining";
    }

    @PostMapping("/mining/click")
    @ResponseBody // 화면 이동 없이 데이터만 반환
    public Map<String, Long> clickMining(HttpSession session) {
        Long userId = (Long) session.getAttribute("myUserId");
        if (userId == null) return null;

        // 100원 추가하고 현재 잔액 받기
        Long currentMoney = miningService.mine(userId);

        return Collections.singletonMap("money", currentMoney);
    }
}