package outils;

import java.util.regex.Pattern;

public class Validator {

    private Validator() {
    }

    public static boolean isValidPassword(String password) {
        String pattern = "^(?=.[0-9])(?=.[a-z])(?=.[A-Z])(?=.[+-/*//%$&#]).{8,20}$";
        return Pattern.compile(pattern).matcher(password).matches();
    }

    public static boolean isMailValid(String mail){
        String alphaN1 = "[A-Za-z0-9_-]";
        String alphaN2 = "[A-Za-z0-9-]";

        String pattern = String.format("^(?=.{1,64}@)%s+(\\.%s+)*@[^-]%s+(\\.%s+)*(\\.[A-Za-z]{2,})$", alphaN1, alphaN1, alphaN2, alphaN2);

        return Pattern.compile(pattern).matcher(mail).matches();
    }
}
