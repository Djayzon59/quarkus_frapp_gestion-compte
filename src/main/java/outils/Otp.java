package outils;

import java.util.Random;

public class Otp {
    private static final int OTP_LENGTH = 6;

    public static String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i=0; i<OTP_LENGTH; i++){
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
