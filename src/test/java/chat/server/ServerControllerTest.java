package chat.server;

import lombok.ToString;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.StompSubProtocolHandler;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;

import java.io.Serializable;
import java.security.Principal;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;
import static sun.plugin.javascript.navig.JSType.URL;

public class ServerControllerTest {

    ChatRepository chatRepo;
    UserRepository userRepo;
    ServerController controller;
    SimpMessagingTemplate messagingTemplate;
    OAuth2Authentication mockAuth;

    private static final String SUBSCRIBE_ALLCHATS_ENDPOINT = "/topic/allChats/testUser/";
    private static final String SUBSCRIBE_USER_ENDPOINT = "/topic/testUser/";
    private static final String SUBSCRIBE_NEWCHAT_ENDPOINT = "/topic/newChat/testUser/";
    private static final String SUBSCRIBE_HISTORY_ENDPOINT = "/topic/history/testUser/";
    private static final String SEND_APP_ENDPOINT = "/app/testUser/";
    private static final String SEND_HISTORY_ENDPOINT = "/app/history/";
    private static final String SEND_UPDATESEEN_ENDPOINT = "/app/updateSeen/testUser/";

    @ToString
    private class OAuthUser implements Principal {
        private User user;

        OAuthUser(User user) {

            this.user = user;
        }

        @Override
        public String getName() {
            return this.user.getId();
        }

    }

    private OAuth2Request getOauth2Request () {
        String clientId = "oauth-client-id";
        Map<String, String> requestParameters = Collections.emptyMap();
        boolean approved = true;
        String redirectUrl = "";
        Set<String> responseTypes = Collections.emptySet();
        Set<String> scopes = Collections.emptySet();
        Set<String> resourceIds = Collections.emptySet();
        Map<String, Serializable> extensionProperties = Collections.emptyMap();
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("Everything");

        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId, authorities,
                approved, scopes, resourceIds, redirectUrl, responseTypes, extensionProperties);

        return oAuth2Request;
    }

    private Authentication getAuthentication() {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("Everything");

        User userPrincipal = User.builder()
                .id("testUser")
                .name("Test User")
                .avatarUrl("avatar_url")
                .build();

        HashMap<String, String> details = new HashMap<String, String>();
        details.put("name", "Test User");
        details.put("login", "testUser");
        details.put("avatar_url", "avatar_url");

        OAuthUser oAuthUser = new OAuthUser(userPrincipal);

        TestingAuthenticationToken token = new TestingAuthenticationToken(oAuthUser,null, authorities);
        token.setAuthenticated(true);
        token.setDetails(details);

        return token;
    }


    private OAuth2Authentication getOauthTestAuthentication() {
        return new OAuth2Authentication(getOauth2Request(), getAuthentication());
    }

    @Before
    public void setupForAllTests() {

        userRepo = mock(UserRepository.class);
        chatRepo = mock(ChatRepository.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        controller = new ServerController(userRepo, chatRepo, messagingTemplate);
    }

    @Test
    public void getUsers_Calls_Repo_GetAll() {

        controller.getUsers();
        verify(userRepo).findAll();
    }

    @Test
    public void user_Calls_Repo_Save() {
        OAuth2Authentication authentication = getOauthTestAuthentication();
        System.out.println(authentication);
        System.out.println(authentication.getPrincipal());
        controller.user(authentication);


        User newUser = User.builder()
                .id("testUser")
                .name("Test User")
                .avatarUrl("avatar_url")
                .build();

        verify(userRepo).save(newUser);
    }

    @Test
    public void socket_Subscribe_Messages_AllChats() {

        User newUser = User.builder()
                .id("testUser")
                .name("Test User")
                .avatarUrl("avatar_url")
                .build();

        Map<String, String> nativeHeaders = new HashMap<>();
        nativeHeaders.put("id", "sub-0");
        nativeHeaders.put("destination", "/topic/testUser" );
        Message message = MessageBuilder.withPayload("")
                .setHeader("nativeHeaders", nativeHeaders)
                .setHeader("simpUser", getOauthTestAuthentication())
                .setHeader("simpDestination", "/topic/allChats/testUser")
                .build();

        SessionSubscribeEvent event = new SessionSubscribeEvent(new StompSubProtocolHandler(), message,
                new OAuthUser(newUser));
        controller.handleSubscribeEvent(event);

        verify(chatRepo).findAllByUsers("testUser");
    }
}
