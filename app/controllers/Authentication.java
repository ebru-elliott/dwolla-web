package controllers;

import models.User;
import org.mindrot.jbcrypt.BCrypt;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.login;
import views.html.usermenu;

import java.io.UnsupportedEncodingException;

import static play.data.Form.form;

public class Authentication extends BaseController {

    public static class LoginForm {

        @Constraints.Required
        public String username;

        @Constraints.Required
        public String password;

    }

    public static Result login() {
        session().clear();
        return ok(login.render(form(LoginForm.class)));
    }


    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.Authentication.login());
    }

    public static Result authenticate() {

        Form<LoginForm> form = form(LoginForm.class).bindFromRequest();

        if(form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            return badRequest(login.render(form));
        } else {

            User user = User.findByUsername(form.get().username);
            if (user == null) {
                form.reject("invalid username");
                return badRequest(login.render(form));
            }
            if ( ! BCrypt.checkpw(form.get().password, user.passwordHash) ) {
                form.reject("invalid password");
                return badRequest(login.render(form));
            }

            session().clear();

            populateSession(user);

            return redirect(controllers.routes.Application.usermenu());
        }
    }


}
