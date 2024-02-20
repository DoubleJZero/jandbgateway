package jandbgateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtTokenProvider
 *
 * <pre>
 * 코드 히스토리 (필요시 변경사항 기록)
 * </pre>
 *
 * @author JandB
 * @since 1.0
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${token.secret}")
    private String SECRET;

    private static Key key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    public String getUserId(String token) {
        return getClaimsFromJwtToken(token).getSubject();
    }

    private Claims getClaimsFromJwtToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public List<String> getRoles(String token) {
        return Arrays.stream(getClaimsFromJwtToken(token).get("roles").toString().split(",")).collect(Collectors.toList());
    }

    public void validateJwtToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Exception: {}", e.getMessage());
        }
    }

}

