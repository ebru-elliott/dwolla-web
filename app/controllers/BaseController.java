package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.menu;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BaseController extends Controller {

    protected static final String DWOLLA_API_BASEURL = "https://www.dwolla.com";
    protected static final String DWOLLA_API_URL = DWOLLA_API_BASEURL + "/oauth/rest";
    protected static final String DWOLLA_DESTINATION_ID = System.getenv("DWOLLA_DESTINATION_ID");
    protected static final String DWOLLA_APP_KEY = System.getenv("DWOLLA_APP_KEY");
    protected static final String DWOLLA_APP_SECRET = System.getenv("DWOLLA_APP_SECRET");
    protected static final String DWOLLA_REDIRECT_URI = System.getenv("BASE_HOST_URL") + "/oauth";

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String IS_ADMIN = "isAdmin";

    public static final String CRYPTO_SECRET = System.getenv("APP_SECRET").substring(0, 16);


    public static void populateSession(User user) {
        session(ID, String.valueOf(user.id));
        session(USERNAME, user.username);
        session(IS_ADMIN, String.valueOf(user.isAdmin));
    }

    public static boolean isAuthenticated() {
        return session(ID) != null;
    }

    public static Integer currentUserId() {
        return Integer.valueOf(session(ID));
    }

    public static User currentUser() {
        User user = User.byId(currentUserId());
        return user;
    }

    public static String dwollaAppKey() throws UnsupportedEncodingException {
        return encode(DWOLLA_APP_KEY);
    }

    public static String dwollaRedirectUri() throws UnsupportedEncodingException {
        return encode(DWOLLA_REDIRECT_URI);
    }


    public static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    public static Result goMenu()
    {
        return ok(menu.render(currentUser()));
    }

}
