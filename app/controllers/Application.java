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


//@Security.Authenticated(Secured.class)
public class Application extends Controller {

    private static final String TOKEN = "token";
    private static final String AUTHORIZATION_CODE = "authorization_code";

    private static final String DWOLLA_API_BASEURL = "https://www.dwolla.com";
    private static final String DWOLLA_DESTINATION_ID = System.getenv("DWOLLA_DESTINATION_ID");
    private static final String DWOLLA_APP_KEY = System.getenv("DWOLLA_APP_KEY");
    private static final String DWOLLA_APP_SECRET = System.getenv("DWOLLA_APP_SECRET");
    private static final String DWOLLA_REDIRECT_URI = System.getenv("DWOLLA_REDIRECT_URI");     //todo ee: why is this in environment var?
    private static final String DWOLLA_REDIRECT2_URI = "http://localhost:9000/usermenu"; //todo ee? how about vars

    private static final String CRYPTO_SECRET = System.getenv("APP_SECRET").substring(0,16);


    public static class ChargeForm {
        @Required
        public String username;
        @Required
        public Double amount;
    }

    public static class PasswordForm {
        @Required
        public String oldPassword;
        @Required
        public String newPassword;
        @Required
        public String confirmPassword;
    }

    public static class UserForm
    {
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

    public static String dwollaAppKey() throws UnsupportedEncodingException {
        return encode(DWOLLA_APP_KEY);
    }

    public static String dwollaRedirectUri() throws UnsupportedEncodingException {
        return encode(DWOLLA_REDIRECT_URI);
    }

    public static Result oauthFlow(String code) {
        Form<User> userForm = form(User.class);

        //Retrofit REST client, oauth step2
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setServer(DWOLLA_API_BASEURL)
                .build();

        Dwolla dwolla = restAdapter.create(Dwolla.class);
        Token token = dwolla.getToken(DWOLLA_APP_KEY, DWOLLA_APP_SECRET, AUTHORIZATION_CODE, DWOLLA_REDIRECT_URI, code);
        session(TOKEN, token.access_token);

        return ok(user.render(userForm));

    }

    public static Result editInfo() {

        User user = User.findByUsername(session("username"));
        Form<User> form = form(User.class).fill(user);

        return ok(editInfo.render(form));

    }

    public static Result editPassword() {
        Form<PasswordForm> form = form(PasswordForm.class);
        return ok(editPassword.render(form));
    }

    public static Result updateInfo() throws UnsupportedEncodingException {
        Form<User> form = form(User.class).bindFromRequest();

        User user = User.findByUsername(session("username"));
        user.username =  form.get().username;
        user.pin = Crypto.encryptAES(form.get().pin, CRYPTO_SECRET);
        user.update();

        return ok(usermenu.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT2_URI)));


    }


        public static Result updatePassword() throws UnsupportedEncodingException {
        Form<PasswordForm> form = form(PasswordForm.class);

        String username = session("username");
        User user = User.findByUsername(username);

        if ((BCrypt.checkpw(form.get().oldPassword, user.password)) && ((form.get().newPassword).equals(form.get().confirmPassword)))
        {
            user.password = BCrypt.hashpw(form.get().newPassword, BCrypt.gensalt());
            user.update();
            return ok(usermenu.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT2_URI)));
        }
        else
        {
            return badRequest(editPassword.render(form));

        }


    }

    public static Result usermenu() throws UnsupportedEncodingException {
        return ok(usermenu.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT2_URI)));
    }


    // encyrpt pin before saving, decrypt before using it , app key in system env
    // bcyrpt with the password
    // add isAdmin to the user table, populate it with admin
    // admin can update : username, isAdmin
    // user can update : 1) change username, pin, 2) change password, 3) reauthorize (pin, pwd not displayed)
    //  admin menu: user (Edit | charge) listUsers page, chargeUser  (Remove user should be under Edit)

    public static Result saveUser() throws UnsupportedEncodingException {
        Form<User> form = form(User.class).bindFromRequest();

        if (form.hasErrors()) {
            System.out.println("ERROR!" + form.errorsAsJson());
            return badRequest(user.render(form));

        } else {
            User u = form.get();

            //todo ee: is this the right place to check it (vs. validation/client side)
            User dbUser = User.findByUsername(u.username);
            if (dbUser !=null)
                return badRequest(user.render(form));  // todo ee: badRequest or?

            u.pin = Crypto.encryptAES(u.pin, CRYPTO_SECRET);
            u.password = BCrypt.hashpw(u.password, BCrypt.gensalt());
            u.token = session(TOKEN);

            u.save();
            return ok(usermenu.render(encode(DWOLLA_APP_KEY), encode(DWOLLA_REDIRECT2_URI)));
        }
    }

    public static Result listUsers() {
        Form<ChargeForm> chargeForm = form(ChargeForm.class);

        List<String> names = User.listUsers();
        return ok(users.render(names, chargeForm));
    }


    public static Result chargeUser() {
        Form<ChargeForm> chargeForm = form(ChargeForm.class).bindFromRequest();
        ChargeForm form = chargeForm.get();

        String username = form.username;
        Double amount = form.amount;

        User user = User.finder.byId(username);


        if (chargeForm.hasErrors()) {
            System.out.println("ERROR!" + chargeForm.errorsAsJson());
            return badRequest(users.render(User.listUsers(), chargeForm));  //todo ee: anyway to avoid hitting db again

        } else {

            //todo ee: async vs sync
            DwollaServiceAsync service = new RestAdapter.Builder().setServer(
                    new Server(DWOLLA_API_BASEURL + "/oauth/rest")).build().create(DwollaServiceAsync.class);

            String pin = Crypto.decryptAES(user.pin, CRYPTO_SECRET);
            System.out.println("decrypted pin: " + pin);
            //todo ee: update this with error handling
            service.send(new DwollaTypedBytes(new Gson(),
                    new SendRequest(user.token, pin, DWOLLA_DESTINATION_ID, amount)), new SendCallback());
            return ok("Thanks for payment!");

        }

    }

    private static String encode(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }


}
