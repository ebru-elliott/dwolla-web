package controllers;

import models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Result;
import play.mvc.Security;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Query;
import views.html.authorize;
import views.html.editInfo;
import views.html.editPassword;
import views.html.menu;

import static play.data.Form.form;


/**
 * Controller for all the user actions (Edit info, authorize Dwolla, etc..)
 */
@Security.Authenticated(Secured.class)
public class Application extends BaseController {

    public static Logger logger = LoggerFactory.getLogger(Admin.class);


    public static class Info {
        @Required
        public String username;
        public String pin;
    }

    public static class PasswordForm {
        @Required
        public String oldPassword;
        @Required
        public String newPassword;
        @Required
        public String confirmPassword;

        public String validate() {
            if (newPassword != null && !newPassword.equals(confirmPassword)) {
                return "new password and confirm password do not match";
            }
            return null;
        }

    }

    public static class Token {
        String access_token;
        String error;
        String error_description;
    }

    interface Dwolla {
        @GET("/oauth/v2/token")
        Token getToken(@Query("client_id") String key, @Query("client_secret") String secret, @Query("grant_type") String grantType, @Query("redirect_uri") String redirectUri, @Query("code") String code);
    }

    /**
     * Renders the index page. If the user is authorized, he will be redirected to the user menu. Otherwise, it will be
     * the login page.
     *
     * @return login page for the first time, and user menu page after a successful authorization
     */
    public static Result index() {
        if (currentUser() == null) {
            logger.info("no authorized user, redirecting to the login");
            return redirect(routes.Authentication.login());
        } else {
            logger.info("redirecting to the user menu");
            return goMenu();
        }
    }

    /**
     * Renders the link to initiate the Dwolla authorization. Dwolla Oauth Flow Step 1.
     * See <a href="https://developers.dwolla.com/dev/pages/auth#oauth-token">OAuth Flow</a>
     *
     * @return page with the authorization link
     */
    public static Result authorize() {
        logger.info("render the Dwolla authorization");
        return ok(authorize.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT_URI)));
    }

    /**
     * Retrieves the token from the Dwolla API after providing the code obtained from Step 1. Dwolla Oauth Flow Step 2.
     * The token gets persisted in the database with the associated user.
     * See <a href="https://developers.dwolla.com/dev/pages/auth#oauth-token">OAuth Flow</a>
     *
     * @param code verification code returned from Dwolla after the Step 1 of Oauth Flow.
     * @return user menu
     */
    public static Result oauthFlow(String code) {
        if (code == null) {
            logger.info("unsuccessful authentication");
            flash(ERROR, "unsuccessful authentication");
        } else {
            //Retrofit REST client, oauth step2
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setServer(DWOLLA_API_BASEURL)
                    .build();

            Dwolla dwolla = restAdapter.create(Dwolla.class);
            Token token = dwolla.getToken(DWOLLA_APP_KEY, DWOLLA_APP_SECRET, AUTHORIZATION_CODE, DWOLLA_REDIRECT_URI, code);
            if (token.access_token != null) {
                flash(SUCCESS, "successful authentication");
                User u = currentUser();
                u.token = token.access_token;
                u.update();                    //persist the token
                logger.info("successful authentication");
            } else {
                logger.info("unsuccessful authentication");
                flash(ERROR, token.error_description);
            }
        }
        return goMenu();
    }

    /**
     * Renders the user edit info page.
     *
     * @return user edit page
     */
    public static Result editInfo() {
        Info info = new Info();
        User user = currentUser();
        info.username = user.username;
        Form<Info> form = form(Info.class).fill(info);

        logger.info("render the edit info");
        return ok(editInfo.render(form));
    }

    /**
     * Renders the user edit password page.
     *
     * @return user edit password page
     */

    public static Result editPassword() {
        Form<PasswordForm> form = form(PasswordForm.class);
        logger.info("render the edit password");
        return ok(editPassword.render(form));
    }

    /**
     * Updates the user username and/or pin.
     *
     * @return user menu if successful
     */
    public static Result updateInfo() {
        Form<Info> form = form(Info.class).bindFromRequest();

        if (form.hasErrors()) {
            logger.info("infoForm has errors:{}", form.errors());
            return badRequest(editInfo.render(form));
        }

        User u = currentUser();

        String username = form.get().username;
        String pin = form.get().pin;

        if (!u.username.equals(username)) {
            User dbUser = User.findByUsername(username);
            if (dbUser != null) {
                form.reject("username already in use");
                return badRequest(editInfo.render(form));
            }
        }

        u.username = username;

        if (pin != null) {
            u.assignPin(pin);
        }
        u.update();

        flash(SUCCESS, "update successful");
        logger.info("username:{} updated", u.username);
        return goMenu();
    }

    /**
     * Updates the user password.
     *
     * @return user menu if successful
     */

    public static Result updatePassword() {
        Form<PasswordForm> form = form(PasswordForm.class).bindFromRequest();
        if (!form.hasErrors()) {
            User u = currentUser();
            if (u.checkPassword(form.get().oldPassword) && ((form.get().newPassword).equals(form.get().confirmPassword))) {
                u.assignPassword(form.get().newPassword);
                u.update();
                flash(SUCCESS, "password updated");
                logger.info("password updated for username:{}", u.username);
                return goMenu();
            } else {
                form.reject("password mismatch");
            }
        }

        logger.info("passwordForm has errors:{}", form.errors());
        return badRequest(editPassword.render(form));
    }

    /**
     * Renders the user menu.
     *
     * @return user menu
     */
    public static Result menu() {
        return ok(menu.render(currentUser()));
    }

}
