package controllers;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public static Logger logger = LoggerFactory.getLogger(Authentication.class);

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
        logger.info("user is logged out, redirecting to the login");
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
                logger.info("username:{} authenticated, redirecting to the user menu", user.username);
                return goMenu();
            }
        }

        logger.info("loginForm has errors:{}", form.errors());
        return badRequest(login.render(form));

    }

    /**
     * Renders the user registration page.
     *
     * @return user registration page
     */
    public static Result showRegister() {
        session().clear();
        logger.info("render user registration");
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
            logger.info("registrationForm has errors:{}", form.errors());
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
            logger.info("username:{} registered, redirecting to the authorization", u.username);
            return redirect(routes.Application.authorize());
        }
    }

}
