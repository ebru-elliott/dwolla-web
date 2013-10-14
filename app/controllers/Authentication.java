package controllers;

import models.User;
import play.Logger;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import views.html.login;
import views.html.register;

import static play.data.Form.form;

/**
 * Controller for authentication tasks (login, logout, authenticate the user..)
 */
public class Authentication extends BaseController {

    public static class LoginForm {
        @Required
        public String username;
        @Required
        public String password;
    }

    public static class Registration {
        @Required
        public String username;
        @Required
        public String password;
        @Required
        public String confirmPassword;
        public String pin;

        public String validate() {
            if (password != null && !password.equals(confirmPassword)) {
                return "password and confirmPassword do not match";
            }
            return null;
        }
    }

    /**
     * Renders the login page. Clears the session.
     *
     * @return login page
     */
    public static Result login() {
        session().clear();
        return ok(login.render(form(LoginForm.class)));
    }

    /**
     * Logouts the user from the application. Clears the session.
     *
     * @return login page
     */
    public static Result logout() {
        session().clear();
        flash(SUCCESS, "You've been logged out");
        Logger.info("user is logged out, redirecting to the login");
        return redirect(routes.Authentication.login());
    }

    /**
     * Authenticates the user credentials against what is stored in the database.
     *
     * @return user menu
     */
    public static Result authenticate() {

        Form<LoginForm> form = form(LoginForm.class).bindFromRequest();

        if (!form.hasErrors()) {
            User user = User.findByUsername(form.get().username);
            if (user == null || !user.checkPassword(form.get().password)) {
                form.reject("invalid credentials");
            } else {
                session().clear();
                populateSession(user);
                Logger.info("username:" + user.username + " authenticated, redirecting to the user menu");
                return goMenu();
            }
        }

        Logger.info("loginForm has errors:" + form.errors());
        return badRequest(login.render(form));

    }

    /**
     * Renders the user registration page.
     *
     * @return user registration page
     */
    public static Result showRegister() {
        session().clear();
        Logger.info("render user registration");
        return ok(register.render(form(Registration.class)));
    }

    /**
     * Saves the user in the database.
     *
     * @return authorization page to obtain the Dwolla token
     */
    public static Result register() {
        Form<Registration> form = form(Registration.class).bindFromRequest();

        if (form.hasErrors()) {
            Logger.info("registrationForm has errors:" + form.errors());
            return badRequest(register.render(form));

        } else {
            User u = new User();

            User dbUser = User.findByUsername(form.get().username);
            if (dbUser != null) {
                form.reject("username already in use");
                return badRequest(register.render(form));
            }

            u.username = form.get().username;
            u.assignPassword(form.get().password);
            u.assignPin(form.get().pin);
            u.save();

            populateSession(u);
            Logger.info("username:" + u.username + " registered, redirecting to the authorization");
            return redirect(routes.Application.authorize());
        }
    }

}
