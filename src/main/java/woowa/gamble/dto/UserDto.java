package woowa.gamble.dto;

import woowa.gamble.domain.UserEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private String userName;
    private Long money;

    public UserDto(UserEntity user) {
        this.userName = user.getUserName();
        this.money = user.getMoney();
    }
}