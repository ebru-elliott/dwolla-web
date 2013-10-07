package controllers;

import com.dwolla.java.sdk.DwollaServiceAsync;
import com.dwolla.java.sdk.DwollaTypedBytes;
import com.dwolla.java.sdk.requests.SendRequest;
import com.google.gson.Gson;
import models.User;
import play.mvc.Controller;
import play.data.Form;
import play.data.validation.Constraints.*;
import play.libs.Crypto;
import play.mvc.Result;
import play.mvc.Security;
import retrofit.RestAdapter;
import retrofit.Server;


import java.io.UnsupportedEncodingException;
import java.util.List;

import views.html.*;


import static play.data.Form.form;

@Security.Authenticated(AdminSecured.class)
public class Admin extends BaseController {


    public static class ChargeForm {
        @Required
        public Double amount;
    }


    public static class Info {
        @Required
        public String username;

        @Required
        public boolean isAdmin;
    }


    public static Result menu() {
        return ok(adminmenu.render(User.all()));
    }

    public static Result editAmount(Integer id) {
        Form<ChargeForm> form = form(ChargeForm.class);
        User user = User.byId(id);
        return ok(charge.render(user.username, user.id, form));
    }


    public static Result chargeUser(Integer id) {
        Form<ChargeForm> form = form(ChargeForm.class).bindFromRequest();

        User user = User.byId(id);

        if (form.hasErrors()) {
            System.out.println("ERROR!" + form.errorsAsJson());
            return badRequest(charge.render(user.username, user.id, form));

        } else {

            //todo ee: async vs sync
            DwollaServiceAsync service = new RestAdapter.Builder().setServer(
                    new Server(Application.DWOLLA_API_BASEURL + "/oauth/rest")).build().create(DwollaServiceAsync.class);

            String pin = Crypto.decryptAES(user.pin, Application.CRYPTO_SECRET);
            System.out.println("decrypted pin: " + pin);
            //todo ee: update this with error handling
            service.send(new DwollaTypedBytes(new Gson(),
                    new SendRequest(user.token, pin, Application.DWOLLA_DESTINATION_ID, form.get().amount)), new SendCallback());

            flash("success", "Money transfer done.");
            return ok(adminmenu.render(User.all()));

        }

    }

    public static Result deleteUser(Integer id) {
        User.delete(id);
        flash("success", "user deleted");
        return ok(adminmenu.render(User.all()));
    }

    public static Result editInfo(Integer id) {
        User u = User.byId(id);

        Info info = new Info();
        info.username = u.username;
        info.isAdmin = u.isAdmin;

        Form<Info> form = form(Info.class).fill(info);

        if (form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            System.out.println("ERROR");
            return badRequest(editInfo2.render(id, form));
        }


        return ok(editInfo2.render(id, form));
    }


    public static Result updateInfo(Integer id) throws UnsupportedEncodingException {
        Form<Info> form = form(Info.class).bindFromRequest();

        if (form.hasErrors()) {
            form.reject("form has errors: " + form.errorsAsJson());
            return badRequest(editInfo2.render(id, form));
        }

        User u = User.byId(id);
        u.username = form.get().username;
        u.isAdmin = form.get().isAdmin;
        u.update();

        return ok(adminmenu.render(User.all()));
    }


}
