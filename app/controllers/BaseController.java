package controllers;

import models.User;
import play.mvc.Controller;

public class BaseController extends Controller {

    protected static final String DWOLLA_API_BASEURL = "https://www.dwolla.com";
    protected static final String DWOLLA_DESTINATION_ID = System.getenv("DWOLLA_DESTINATION_ID");
    protected static final String DWOLLA_APP_KEY = System.getenv("DWOLLA_APP_KEY");
    protected static final String DWOLLA_APP_SECRET = System.getenv("DWOLLA_APP_SECRET");
    protected static final String DWOLLA_REDIRECT_URI = System.getenv("DWOLLA_REDIRECT_URI");     //todo ee: why is this in environment var?

    public static void populateSession(User user) {
        session("id", String.valueOf(user.id));
        session("username", user.username);
        session("isAdmin", String.valueOf(user.isAdmin));
    }

    public static boolean isAuthenticated() {
        return session("id") != null;
    }

    public static Integer currentUserId() {
        return Integer.valueOf(session("id"));
    }

    public static User currentUser() {
        User user = User.byId(currentUserId());
        return user;
    }
}
