package mapper;

import com.secondhand.backend.dto.LoginResponseDto;
import com.secondhand.backend.dto.RegisterRequestDto;
import com.secondhand.backend.dto.UserDto;
import com.secondhand.backend.entity.User;
import org.springframework.stereotype.Component;

@Component

public class UserMapper {
    public UserDto toDto(User user){
        if (user == null){
            return null ;
        }
        UserDto dto = new UserDto() ;
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhoneNumber());
        dto.setRole(user.getRole());
        return dto ;
    }
    public User toEntity(RegisterRequestDto dto){
        if (dto == null){
            return null ;
        }
        User user = new User() ;
        dto.setId(String.valueOf(user.getId()));
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUserName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhoneNumber());
        //dto.setRole(user.getRole());   how to ask role for user
        return user ;
    }
    public LoginResponseDto toLoginResponse(User user){
        if (user == null){
            return null ;
        }
        LoginResponseDto dto = new LoginResponseDto() ;

        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setUsername(user.getUserName());
        dto.setRole(user.getRole());
        return dto ;
    }

}
