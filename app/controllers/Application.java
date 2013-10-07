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

//todo ee: figure out the authorization on certain links, bootstrap admin values, isAdmin check on the usermenu
//check cancel,clean up error messages, async vs. sync error handling, update redirect_uri to be localhost vs. the whole path

@Security.Authenticated(Secured.class)
public class Application extends BaseController {

    private static final String TOKEN = "token";
    private static final String AUTHORIZATION_CODE = "authorization_code";

    //todo ee:refactor constants to ?
    public static final String DWOLLA_API_BASEURL = "https://www.dwolla.com";
    public static final String DWOLLA_DESTINATION_ID = System.getenv("DWOLLA_DESTINATION_ID");
    public static final String DWOLLA_APP_KEY = System.getenv("DWOLLA_APP_KEY");
    private static final String DWOLLA_APP_SECRET = System.getenv("DWOLLA_APP_SECRET");
    private static final String DWOLLA_REDIRECT_URI = System.getenv("DWOLLA_REDIRECT_URI");     //todo ee: why is this in environment var?


    public static final String CRYPTO_SECRET = System.getenv("APP_SECRET").substring(0, 16);

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

    public static class Registration {
        @Required
        public String username;
        @Required
        public String password;
        @Required
        public String pin;
        @Required
        public String confirmPassword;

        public String validate() {
            if (password != null && !password.equals(confirmPassword)) {
                return "password and confirmPassword do not match";
            }
            return null;
        }
    }


    public static class UserForm {
        @Required
        public String username;
        @Required
        public String pin;
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


    // -- Actions

    /**
     * Home page
     */
    public static Result index() throws UnsupportedEncodingException {
        return ok(index.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT_URI)));
    }

    public static Result oauthFlow(String code) {

        System.out.println("oauthFlow");
        Form<Registration> form = form(Registration.class);

        //Retrofit REST client, oauth step2
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(DWOLLA_API_BASEURL)
                .build();

        Dwolla dwolla = restAdapter.create(Dwolla.class);
        Token token = dwolla.getToken(DWOLLA_APP_KEY, DWOLLA_APP_SECRET, AUTHORIZATION_CODE, DWOLLA_REDIRECT_URI, code);
        session(TOKEN, token.access_token);

        //todo ee: session(TOKEN) a good idea?
        if ( ! isAuthenticated() )
            return ok(register.render(form));
        else
        {
            User u = currentUser();
            u.token = token.access_token;
            u.update();

            return ok(usermenu.render());
        }
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

        return ok(usermenu.render());
    }


    public static Result updatePassword() throws UnsupportedEncodingException {
        Form<PasswordForm> form = form(PasswordForm.class).bindFromRequest();

        if (form.hasErrors()) {
            return badRequest(editPassword.render(form));
        }

        User u = User.findByUsername(session("username"));
        if ((BCrypt.checkpw(form.get().oldPassword, u.passwordHash)) && ((form.get().newPassword).equals(form.get().confirmPassword))) {
            u.passwordHash = BCrypt.hashpw(form.get().newPassword, BCrypt.gensalt());
            u.update();
            return ok(usermenu.render());
        } else {
            form.reject("password mismatch");
            return badRequest(editPassword.render(form));

        }
    }

    public static Result usermenu() throws UnsupportedEncodingException {
        return ok(usermenu.render());
    }


    public static Result saveUser() throws UnsupportedEncodingException {
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
            u.pin = Crypto.encryptAES(form.get().pin, CRYPTO_SECRET);
            u.passwordHash = BCrypt.hashpw(form.get().password, BCrypt.gensalt());
            u.token = session(TOKEN);
            u.save();

            session("username", form.get().username);
            return ok(usermenu.render());
        }
    }


    public static String dwollaAppKey() throws UnsupportedEncodingException {
        return encode(DWOLLA_APP_KEY);
    }

    public static String dwollaRedirectUri() throws UnsupportedEncodingException {
        return encode(DWOLLA_REDIRECT_URI);
    }


    public static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }


}
