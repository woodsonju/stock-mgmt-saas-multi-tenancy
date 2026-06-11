package com.woodev.saas.auth.services;

import com.woodev.saas.auth.LoginRequest;
import com.woodev.saas.auth.LoginResponse;

public interface AuthenticationService {

    LoginResponse login(final LoginRequest request);
}
