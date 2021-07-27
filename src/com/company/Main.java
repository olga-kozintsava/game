package com.company;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.*;
import java.util.*;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException {
        {
            if (Input.checkArgs(args)) {
                byte[] randomNonce = Computer.getRandomNonce();
                SecretKey key = Computer.getKey(randomNonce);
                int count = args.length;
                String computerMove = args[Computer.getMove(count)];
                System.out.println("HMAC: " + Computer.getHMAC(key, computerMove));
                String userMove = User.userMove(args);
                if (userMove != null) {
                    System.out.println(Winner.pickWinner(computerMove, userMove, args));
                    System.out.println("HMAC key: " + Computer.toHex(randomNonce));
                }
            }
        }
    }
}

class Input {
    public static Boolean checkArgs(String[] args) {
        if (args.length < 3) {
            System.out.println("The number of arguments must be 3 or more");
            return false;
        } else if (new HashSet<String>(Arrays.asList(args)).size() != args.length) {
            System.out.println("Must be only unique values");
            return false;
        } else if (args.length % 2 == 0) {
            System.out.println("The number of arguments must be odd");
            return false;
        } else {
            return true;
        }
    }
}

class Computer {
    public static byte[] getRandomNonce() {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static SecretKey getKey(byte[] randomNonce) {
        return new SecretKeySpec(randomNonce, "HmacSHA256");
    }

    public static int getMove(int count) {
        Random random = new Random();
        return random.nextInt(count);
    }

    public static String getHMAC(Key key, String message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        sha256_HMAC.init(key);
        byte[] result = sha256_HMAC.doFinal(message.getBytes());
        return toHex(result);
    }

    public static String toHex(byte[] hmac) {
        return String.format("%032x", new BigInteger(1, hmac));
    }
}


class User {
    public static String userMove(String[] args) {
        Scanner inputMove = new Scanner(System.in);
        System.out.println("Available moves:");
        for (int i = 0; i < args.length; i++) {
            System.out.println(i + 1 + " - " + args[i]);
        }
        System.out.println("0 - exit ");
        System.out.println("Enter your move: ");
        int userMove;
        try {
            userMove = inputMove.nextInt();
        } catch (InputMismatchException exception) {
            System.out.println("Unavailable choice");
            return userMove(args);
        }
        if (userMove <= args.length & userMove > 0) {
            System.out.println("Your move: " + args[userMove - 1]);
            return args[userMove - 1];
        } else if (userMove == 0) {
            return null;
        } else {
            System.out.println("Unavailable choice");
            return userMove(args);
        }
    }
}

class Winner {
    public static String pickWinner(String computerMove, String userMove, String[] args) {
        int len = args.length;
        int count = len / 2;
        int userMoveIndex = Arrays.asList(args).indexOf(userMove);
        int computerMoveIndex = Arrays.asList(args).indexOf(computerMove);
        int[] loseIndex = new int[count];
        int[] winIndex = new int[count];
        for (int i = 0; i < count; i++) {
            loseIndex[i] = (userMoveIndex + 1 + i) % len;
        }
        for (int i = 0; i < count; i++) {
            winIndex[i] = (userMoveIndex + 1 + count + i) % len;
        }
        System.out.println("Computer move:" + computerMove);
        if (IntStream.of(loseIndex).anyMatch(x -> x == computerMoveIndex)) {
            return ("You lose!");
        } else if (IntStream.of(winIndex).anyMatch(x -> x == computerMoveIndex)) {
            return ("You win!");
        } else if (computerMove.equals(userMove)) {
            return ("Draw!");
        } else {
            return "error";
        }
    }
}
