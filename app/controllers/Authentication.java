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

public class Authentication extends Controller {

    public static class LoginForm {

        @Constraints.Required
        public String username;

        @Constraints.Required
        public String password;

    }

    public static Result login() {
        return ok(login.render(form(LoginForm.class)));
    }


    public static Result logout() {
        session().clear();
        flash("success", "You've been logged out");
        return redirect(routes.Authentication.login());
    }

    public static Result authenticate() {
        // the validate method of the form has already been called
        Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();

        if(loginForm.hasErrors()) {
            return badRequest(login.render(loginForm));
        } else {

            String username = loginForm.get().username;
            String password = loginForm.get().password;



            User user = User.findByUsername(username);
            if (user == null) {
                return badRequest("invalid username");
            }
            if ( ! BCrypt.checkpw(password, user.password) ) {
                return badRequest("invalid password");
            }

            session().clear();
            session("username", username);

            return ok(usermenu.render(null, null)); //encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT2_URI)));
        }
    }


}
