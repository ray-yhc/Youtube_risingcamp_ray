package com.example.demo.src.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/app")
@Getter
@Setter
public class OauthController {

//    https://accounts.google.com/o/oauth2/v2/auth/identifier
//    ?response_type=code
//    &client_id=789244417023-epok34fi90c33qgulifbadjh1h9p6aps.apps.googleusercontent.com
//    &scope=email%20profile
//    &state=aqpD9qxLhDsOzO-diVbBtF2195--eHI84FbUSGVwIj4%3D
//    &redirect_uri=http%3A%2F%2Flocalhost%3A9000%2Flogin%2Foauth2%2Fcode%2Fgoogle
//    &flowName=GeneralOAuthFlow






//    @GetMapping("/")
//    public String index(){
//        return "index";
//    }
//
//    @GetMapping("/user")
//    public String user(){
//        return "user";
//    }
//
//    private static final String authorizationRequestBaseUri = "oauth2/authorization";
//    Map<String,String> oauth2AuthenticationUrls = new HashMap<>();
//    private final ClientRegistrationRepository clientRegistrationRepository;
//
//    @GetMapping("/login")
//    public String getLoginPage(Model model) throws Exception {
//        System.out.println("hi");
//        Iterable<ClientRegistration> clientRegistrations = null;
//        ResolvableType type = ResolvableType.forInstance(clientRegistrationRepository)
//                .as(Iterable.class);
//
//        if (type != ResolvableType.NONE &&
//            ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
//            clientRegistrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
//        }
//        assert clientRegistrations != null;
//        clientRegistrations.forEach(registration ->
//                oauth2AuthenticationUrls.put(registration.getClientName(),
//                        authorizationRequestBaseUri + "/" + registration.getRegistrationId()));
//        model.addAttribute("urls",oauth2AuthenticationUrls);
//
//        return "login";
//    }
//
//    @GetMapping("/accessDenied")
//    public String accessDenied () {return "accessDenied"; }
}
