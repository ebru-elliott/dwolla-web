package controllers;


import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

public class AdminSecured extends Security.Authenticator {
    @Override
    public String getUsername(Http.Context ctx) {
        String isAdmin = ctx.session().get("isAdmin");
        if (isAdmin != null && Boolean.valueOf(isAdmin)) {
            return ctx.session().get("username");
        } else {
            return null;
        }
    }
    @Override
    public Result onUnauthorized(Http.Context ctx) {
        return redirect(routes.Authentication.login());
    }
}