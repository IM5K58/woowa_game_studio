package woowa.gamble.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import woowa.gamble.domain.UserEntity;
import woowa.gamble.repository.UserRepository;

import java.util.List;

@Controller
public class RankingController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/ranking")
    public String rankingPage(Model model) {
        List<UserEntity> topUsers = userRepository.findTop20ByOrderByMoneyDesc();
        model.addAttribute("topUsers", topUsers);
        return "ranking"; // templates/ranking.html 을 보여줌
    }
}