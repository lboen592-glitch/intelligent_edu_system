package com.demo_system.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "PLEASE_CHANGE_TO_A_LONG_RANDOM_SECRET_KEY";
    private static final long EXPIRE_MS = 7L * 24 * 3600 * 1000; // 7天
    private static Algorithm algorithm() {
        return Algorithm.HMAC256(SECRET);
    }
    /** 生成 token：把 userId 放到 subject，username 放到 claim */
    public static String generateToken(Long userId, String username) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + EXPIRE_MS);

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm());
    }
    /** 校验并解析：失败会抛 JWTVerificationException */
    public static DecodedJWT verify(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(algorithm()).build();
        return verifier.verify(token);
    }
}
