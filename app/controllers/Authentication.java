package controllers;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import play.api.libs.Crypto;
import play.data.Form;

import play.data.validation.Constraints.*;

import play.mvc.Result;

import views.html.*;


import java.io.UnsupportedEncodingException;

import static play.data.Form.form;

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

    public static Result login() {
        session().clear();
        return ok(login.render(form(LoginForm.class)));
    }


    public static Result logout() {
        session().clear();
        flash(SUCCESS, "You've been logged out");
        return redirect(routes.Authentication.login());
    }

    public static Result authenticate() {

        Form<LoginForm> form = form(LoginForm.class).bindFromRequest();

        if ( ! form.hasErrors() ) {
            User user = User.findByUsername(form.get().username);
            if (user == null || ! user.checkPassword(form.get().password)) {
                form.reject("invalid credentials");
            } else {
                session().clear();
                populateSession(user);
                return goMenu();
            }
        }
        return badRequest(login.render(form));

    }

    public static Result showRegister() {
        session().clear();
        return ok(register.render(form(Registration.class)));
    }


    public static Result register() throws UnsupportedEncodingException {
        Form<Registration> form = form(Registration.class).bindFromRequest();

        if (form.hasErrors()) {
            System.out.println("ERROR!" + form.errorsAsJson());
            return badRequest(register.render(form));

        } else {
            User u = new User();

            User dbUser = User.findByUsername(form.get().username);
            if (dbUser != null)
                return badRequest(register.render(form));

            u.username = form.get().username;
            u.assignPassword( form.get().password );
            u.assignPin(form.get().pin);
            u.save();

            populateSession(u);
            return redirect(routes.Application.authorize());
        }
    }

}
