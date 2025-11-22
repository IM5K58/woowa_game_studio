package woowa.gamble.controller;

import woowa.gamble.domain.UserEntity;
import woowa.gamble.repository.UserRepository;
import woowa.gamble.dto.UserDto;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String mainPage(Model model, HttpSession session) {
        Long currentUserId = (Long) session.getAttribute("myUserId");

        if (currentUserId != null) {
            UserEntity userEntity = userRepository.findById(currentUserId).orElse(null);

            if (userEntity != null) {
                UserDto userDto = new UserDto(userEntity);
                model.addAttribute("user", userDto);
            }
        } else {
            model.addAttribute("user", null);
        }
        return "main";
    }

    @PostMapping("/start-game")
    public String startGame(@RequestParam("nickname") String nickname, HttpSession session) {
        UserEntity newUser = new UserEntity(nickname);
        UserEntity savedUser = userRepository.save(newUser);
        session.setAttribute("myUserId", savedUser.getId());
        return "redirect:/";
    }
}