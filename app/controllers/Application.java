package controllers;

import com.dwolla.java.sdk.DwollaServiceAsync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;

import com.google.gson.Gson;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import play.libs.Crypto;
import play.mvc.*;
import play.data.*;

import static play.data.Form.*;

import play.data.validation.Constraints.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import views.html.*;

import retrofit.RestAdapter;
import retrofit.Server;
import retrofit.http.GET;
import retrofit.http.Query;

//todo ee: clean up error messages, async vs. sync error handling,

@Security.Authenticated(Secured.class)
public class  Application extends BaseController {

    private static final String AUTHORIZATION_CODE = "authorization_code";


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
                return "password and confirmPassword do not match";
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

    public static Result index() {
        if (currentUser() == null)
            return redirect(routes.Authentication.login());
        else
            return redirect(routes.Application.menu());
    }

    public static Result authorize() throws UnsupportedEncodingException {
        return ok(authorize.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT_URI)));
    }

    public static Result oauthFlow(String code) {

        if (code == null)
            return goMenu();

        //Retrofit REST client, oauth step2
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(DWOLLA_API_BASEURL)
                .build();

        Dwolla dwolla = restAdapter.create(Dwolla.class);
        Token token = dwolla.getToken(DWOLLA_APP_KEY, DWOLLA_APP_SECRET, AUTHORIZATION_CODE, DWOLLA_REDIRECT_URI, code);
        flash("success", "You've been successfully authenticated with Dwolla");

        User u = currentUser();
        u.token = token.access_token;
        u.update();

        return redirect(routes.Application.menu());
    }


    public static Result editInfo() {

        Info info = new Info();
        User user = currentUser();
        info.username = user.username;
        Form<Info> form = form(Info.class).fill(info);

        return ok(editInfo.render(form));
    }

    public static Result editPassword() {
        Form<PasswordForm> form = form(PasswordForm.class);
        return ok(editPassword.render(form));
    }

    public static Result updateInfo() throws UnsupportedEncodingException {
        Form<Info> form = form(Info.class).bindFromRequest();


        if (form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            return badRequest(editInfo.render(form));
        }

        User u = currentUser();

        u.username = form.get().username;
        String pin = form.get().pin;

        if (pin != null) {
            u.pin = Crypto.encryptAES(pin, CRYPTO_SECRET);
        }
        u.update();

        return redirect(routes.Application.menu());
    }

    public static Result menu() {
        return goMenu();
    }

    public static Result updatePassword() throws UnsupportedEncodingException {
        Form<PasswordForm> form = form(PasswordForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(editPassword.render(form));
        }

        User u = currentUser();
        if ((BCrypt.checkpw(form.get().oldPassword, u.passwordHash)) && ((form.get().newPassword).equals(form.get().confirmPassword))) {
            u.passwordHash = BCrypt.hashpw(form.get().newPassword, BCrypt.gensalt());
            u.update();
            return redirect(routes.Application.menu());
        } else {
            form.reject("password mismatch");
            return badRequest(editPassword.render(form));

        }
    }


}
