package esthesis.backend.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

/**
 * An aspect handling security annotations.
 */
@Aspect
@Component
public class OperationsAspect {

//  @Autowired
//  private JWTService jwtService;

  @Before("@annotation(isAdmin)")
  public void anyOperation(JoinPoint jp, IsAdmin isAdmin) {
//    evaluateOperations(INCLUSION.ANY, hasAnyOperation.value(), null);
  }


}
