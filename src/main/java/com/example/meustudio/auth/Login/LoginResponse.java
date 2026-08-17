
package com.example.meustudio.auth.Login;

import com.example.meustudio.auth.User;

public record LoginResponse(

) {
    public static LoginResponse fromEntity(User user) {
        return new LoginResponse();
    }

}