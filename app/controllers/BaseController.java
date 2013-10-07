package controllers;

import models.User;
import play.mvc.Controller;

public class BaseController extends Controller {
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
