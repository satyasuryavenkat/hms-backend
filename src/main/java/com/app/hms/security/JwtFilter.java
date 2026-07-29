package com.app.hms.security;

import com.app.hms.dao.UserDao;
import com.app.hms.entity.AppUser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UserDao users;

  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer "))
      try {
        Claims c = jwt.parse(h.substring(7));
        if ("access".equals(c.get("type"))
            && SecurityContextHolder.getContext().getAuthentication() == null) {
          users
              .findByUsername(c.getSubject())
              .filter(AppUser::isActive)
              .ifPresent(
                  u -> {
                    var a = new ArrayList<SimpleGrantedAuthority>();
                    a.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
                    u.getPermissions().forEach(p -> a.add(new SimpleGrantedAuthority(p)));
                    SecurityContextHolder.getContext()
                        .setAuthentication(
                            new UsernamePasswordAuthenticationToken(u.getUsername(), null, a));
                  });
        }
      } catch (Exception ignored) {
      }
    chain.doFilter(req, res);
  }
}
