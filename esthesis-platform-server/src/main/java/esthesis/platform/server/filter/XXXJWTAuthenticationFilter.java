package esthesis.platform.server.filter;


//@Component
public class XXXJWTAuthenticationFilter {
  //extends OncePerRequestFilter {
//
//  // JUL reference.
//  private static final Logger LOGGER = Logger.getLogger(XXXJWTAuthenticationFilter.class.getName());
//
//  private final UserService userService;
//  private final ApplicationService applicationService;
//
//  // The prefix of the token's value when it comes in the headers.
//  // A JWT logged-in user.
//  private static final String BEARER_HEADER = "Bearer";
//
//  // The name of the token when it comes as a url param.
//  private static final String BEARER_PARAM = "bearer";
//
//  private final JWTService jwtService;
//  private AntPathMatcher antPathMatcher;
//
//  @Value("${server.servlet.context-path}")
//  private String contextRoot;
//
//  private List<String> PUBLIC_URIS_PREFIXED;
//
//  public XXXJWTAuthenticationFilter(UserService userService,
//    ApplicationService applicationService, JWTService jwtService) {
//    this.userService = userService;
//    this.applicationService = applicationService;
//    this.jwtService = jwtService;
//    antPathMatcher = new AntPathMatcher();
//  }
//
//  private UsernamePasswordAuthenticationToken decodeJwt(String jwtToken) {
//    final JWTClaimsResponseDTO jwtClaimsResponseDTO = jwtService.getClaims(jwtToken);
//    if (jwtClaimsResponseDTO != null && StringUtils.isNotBlank(jwtClaimsResponseDTO.getSubject())) {
//      return new UsernamePasswordAuthenticationToken(jwtClaimsResponseDTO.getSubject(),
//        jwtClaimsResponseDTO.getClaims().get(Jwt.JWT_CLAIM_USER_ID), emptyList());
//    } else {
//      return null;
//    }
//  }
//
//  private Authentication getAuthentication(HttpServletRequest request) {
//    String authHeader = request.getHeader(AppConstants.Http.AUTHORIZATION);
//
//    if (StringUtils.isNotBlank(authHeader)) {
//      if (authHeader.startsWith(BEARER_HEADER)) {
//        return decodeJwt(authHeader.replace(BEARER_HEADER, "").trim());
//      }
//      if (authHeader.startsWith(esthesis.common.config.AppConstants.XESTHESISDT_HEADER)) {
//        String token = authHeader.replace(esthesis.common.config.AppConstants.XESTHESISDT_HEADER, "").trim();
//        try {
//          final ApplicationDTO applicationDTO = applicationService.findByToken(token);
//          if (applicationDTO.isState()) {
//            final UserDTO userDTO = userService.getUserById(applicationDTO.getUserId());
//            return new UsernamePasswordAuthenticationToken(userDTO.getUsername(), userDTO.getId(),
//              emptyList());
//          } else {
//            return null;
//          }
//        } catch (QDoesNotExistException e) {
//          LOGGER.log(Level.WARNING, MessageFormat.format("Could not find application with token "
//            + "{0}.", token));
//          return null;
//        }
//      } else {
//        LOGGER.log(Level.WARNING, "An unknown authorisation header was found: {0}.", authHeader);
//        return null;
//      }
//    } else if (StringUtils.isNotBlank(request.getParameter(BEARER_PARAM))) {
//      return decodeJwt(request.getParameter(BEARER_PARAM));
//    } else {
//      return null;
//    }
//  }
//
//  @PostConstruct
//  public void prefixPublicURLs() {
//    // Prefix public URIs with context root, so that they can be checked against incoming requests.
//    PUBLIC_URIS_PREFIXED = Arrays.stream(PUBLIC_URIS).map(s -> contextRoot + s)
//      .collect(Collectors.toList());
//  }
//
//  @Override
//  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//    FilterChain filterChain) throws ServletException, IOException {
//    // Filter private resources.
//    if (PUBLIC_URIS_PREFIXED.stream()
//      .noneMatch(p -> antPathMatcher.match(p, ((HttpServletRequest) request).getRequestURI()))) {
//      Authentication authentication = getAuthentication((HttpServletRequest) request);
//      SecurityContextHolder.getContext().setAuthentication(authentication);
//    }
//
//    // Proceed with other filters.
//    filterChain.doFilter(request, response);
//  }
}
