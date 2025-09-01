package com.beyond.ordersystem.common.auth;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider {
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.expirationAt}")
    private int expirationAt;

    @Value("${jwt.secretKeyAt}")
    private String secretKeyAt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    private Key secret_at_key;
    private Key secret_rt_key;

    // Qualifier는 기본적으로 메서드를 통한 주입 가능. 그래서, 이 경우 생성자 주입 방식을 해야 Qualifier 사용 가능.
    @Autowired
    public JwtTokenProvider(MemberRepository memberRepository, @Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate) {
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
    }

    // 스프링 빈이 만들어지는 시점에 빈이 만들어진 직후에 아래 메서드가 바로 실행
    @PostConstruct
    public void init() {
        secret_at_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyAt), SignatureAlgorithm.HS512.getJcaName());
        secret_rt_key = new SecretKeySpec(java.util.Base64.getDecoder().decode(secretKeyRt), SignatureAlgorithm.HS512.getJcaName());
    }


    public String createAtToken(Member member) {
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드 (사용자 정보)
        Claims claims = Jwts.claims().setSubject(email); // 주된 정보 1개 (식별자)
        // 주된 키 값을 제외한 나머지 사용자 정보는 put사용하여 key:value 세팅
        claims.put("role", role);

        Date now = new Date();
        String accessToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationAt*60*1000L)) // 30분을 세팅
                // secret 키를 통해 signiture 생성
                .signWith(secret_at_key)
                .compact();

        return accessToken;
    }

    public String createRtToken(Member member) {
        // 유효기간이 긴 rt 토큰 생성
        String email = member.getEmail();
        String role = member.getRole().toString();

        // claims는 페이로드 (사용자 정보)
        Claims claims = Jwts.claims().setSubject(email); // 주된 정보 1개 (식별자)
        // 주된 키 값을 제외한 나머지 사용자 정보는 put사용하여 key:value 세팅
        claims.put("role", role);

        Date now = new Date();
        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationRt*60*1000L)) // 30분을 세팅
                // secret 키를 통해 signiture 생성
                .signWith(secret_rt_key)
                .compact();

        // rt 토큰을 redis에 저장 : key-value 형식으로 set
//        redisTemplate.opsForValue().set(member.getEmail(), refreshToken);
        redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 200, TimeUnit.DAYS); // 200일 ttl


        return refreshToken;
    }

    public Member validateRt(String refreshToken) {
        // rt 그 자체를 검증
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secret_rt_key)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();

        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자"));

        // redis의 값과 비교하는 검증
        String redisRt = redisTemplate.opsForValue().get(member.getEmail());
        if(!redisRt.equals(refreshToken)) {
            throw new IllegalArgumentException("잘못된 토큰입니다.");
        }

        return member;
    }

}
