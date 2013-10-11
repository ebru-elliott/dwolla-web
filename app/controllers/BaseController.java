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

    protected static final String ID = "id";
    protected static final String USERNAME = "username";
    protected static final String IS_ADMIN = "isAdmin";

    protected static final String SUCCESS = "success";
    protected static final String ERROR = "error";


    public static void populateSession(User user) {
        session(ID, user.id);
        session(USERNAME, user.username);
        session(IS_ADMIN, String.valueOf(user.isAdmin));
    }

    public static boolean isAuthenticated() {
        return session(ID) != null;
    }

    public static String currentUserId() {
        return session(ID);
    }

    public static User currentUser() {
        return User.byId(currentUserId());
    }

    public static String dwollaAppKey() {
        return encode(DWOLLA_APP_KEY);
    }

    public static String dwollaRedirectUri() {
        return encode(DWOLLA_REDIRECT_URI);
    }


    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException(uee);
        }
    }

    public static Result goMenu() {
        return redirect(routes.Application.menu());
    }

}
