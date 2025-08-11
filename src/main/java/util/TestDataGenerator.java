package util;

import api.model.request.Player;

import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static common.PropertiesReader.getProperty;

public class TestDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    private static final Faker faker = new Faker(new Locale("en"));

    public static Player generateValidPlayer() {
        return generateValidPlayer("user");
    }

    public static Player generateValidPlayer(String role) {
        Player player = new Player();
        player.setAge(generateRandomAge());
        player.setGender(generateRandomGender());
        player.setLogin(faker.internet().username());
        player.setPassword(generateValidPassword());
        player.setRole(role);
        player.setScreenName(faker.internet().uuid().substring(0, 8));

        logger.debug("Generated valid player: {}", player);
        return player;
    }

    public static Player generatePlayerWithInvalidAgeYoung() {
        Player player = generateValidPlayer();
        player.setAge(Integer.parseInt(getProperty("test.user.min.age", "16")) - 1);
        return player;
    }

    public static Player generatePlayerWithInvalidAgeOld() {
        Player player = generateValidPlayer();
        player.setAge(Integer.parseInt(getProperty("test.user.max.age", "60")) + 1);
        return player;
    }

    public static Player generatePlayerWithInvalidGender() {
        Player player = generateValidPlayer();
        player.setGender("invalid_gender");
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordShort() {
        Player player = generateValidPlayer();
        int minLen = Integer.parseInt(getProperty("test.password.min.length", "7"));
        int length = Math.max(1, minLen - 1);
        player.setPassword(generateAlphaNumericWithLettersAndDigits(length));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordLong() {
        Player player = generateValidPlayer();
        int maxLen = Integer.parseInt(getProperty("test.password.max.length", "15"));
        int extra = faker.number().numberBetween(1, 6);
        int length = maxLen + extra;
        player.setPassword(generateAlphaNumericWithLettersAndDigits(length));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordNoNumbers() {
        Player player = generateValidPlayer();
        int maxLen = Integer.parseInt(getProperty("test.password.max.length", "15"));
        int minLen = Integer.parseInt(getProperty("test.password.min.length", "7"));
        int length = faker.number().numberBetween(minLen, maxLen);
        player.setPassword(generateLettersOnly(length));
        return player;
    }

    public static Player generatePlayerWithInvalidPasswordNoLetters() {
        Player player = generateValidPlayer();
        int maxLen = Integer.parseInt(getProperty("test.password.max.length", "15"));
        int minLen = Integer.parseInt(getProperty("test.password.min.length", "7"));
        int length = faker.number().numberBetween(minLen, maxLen);
        player.setPassword(faker.number().digits(length));
        return player;
    }

    public static Player generatePlayerWithInvalidRole() {
        Player player = generateValidPlayer();
        player.setRole("invalid_role");
        return player;
    }

    public static Player generatePlayerWithDuplicateLogin(String existingLogin) {
        Player player = generateValidPlayer();
        player.setLogin(existingLogin);
        return player;
    }

    public static Player generatePlayerWithNullFields() {
        Player player = new Player();
        player.setAge(25);
        player.setGender("male");
        return player;
    }

    public static Player generatePlayerWithDuplicateScreenName(String existingScreenName) {
        Player player = generateValidPlayer();
        player.setScreenName(existingScreenName);
        return player;
    }

    public static Player generatePlayerWithMissingAge() {
        Player player = generateValidPlayer();
        player.setAge(null);
        return player;
    }

    public static Player generatePlayerWithMissingGender() {
        Player player = generateValidPlayer();
        player.setGender(null);
        return player;
    }

    public static Player generatePlayerWithMissingLogin() {
        Player player = generateValidPlayer();
        player.setLogin(null);
        return player;
    }

    public static Player generatePlayerWithMissingPassword() {
        Player player = generateValidPlayer();
        player.setPassword(null);
        return player;
    }

    public static Player generatePlayerWithMissingRole() {
        Player player = generateValidPlayer();
        player.setRole(null);
        return player;
    }

    public static Player generatePlayerWithMissingScreenName() {
        Player player = generateValidPlayer();
        player.setScreenName(null);
        return player;
    }

    public static Player generatePartialUpdatePlayer() {
        Player player = new Player();
        // Only set some fields for partial updates
        player.setAge(generateRandomAge());
        player.setGender(generateRandomGender());
        return player;
    }

    public static Player generateUpdatePlayerWithNewLogin() {
        Player player = new Player();
        player.setLogin(faker.internet().username());
        return player;
    }

    public static Player generateUpdatePlayerWithNewScreenName() {
        Player player = new Player();
        player.setScreenName(faker.internet().uuid().substring(0, 8));
        return player;
    }

    public static Player generateUpdatePlayerWithNewPassword() {
        Player player = new Player();
        player.setPassword(generateValidPassword());
        return player;
    }

    private static int generateRandomAge() {
        int maxAge = Integer.parseInt(getProperty("test.user.max.age", "60"));
        int minAge = Integer.parseInt(getProperty("test.user.min.age", "16"));
        return faker.number().numberBetween(minAge, maxAge);
    }

    private static String generateRandomGender() {
        return faker.options().option("male", "female");
    }

    private static String generateValidPassword() {
        int maxPasswordLength = Integer.parseInt(getProperty("test.password.max.length", "15"));
        int minPasswordLength = Integer.parseInt(getProperty("test.password.min.length", "7"));
        // includeUppercase=true, includeSpecial=false, includeDigit=true
        return faker.internet().password(minPasswordLength, maxPasswordLength, true, false, true);
    }

    private static String generateAlphaNumericWithLettersAndDigits(int length) {
        if (length <= 1) {
            return "a1".substring(0, Math.max(1, length));
        }
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String digits = "0123456789";
        String allChars = letters + digits;
        StringBuilder password = new StringBuilder();
        // ensure at least one letter and one digit
        password.append(letters.charAt(faker.random().nextInt(letters.length())));
        password.append(digits.charAt(faker.random().nextInt(digits.length())));
        for (int i = 2; i < length; i++) {
            password.append(allChars.charAt(faker.random().nextInt(allChars.length())));
        }
        return password.toString();
    }

    private static String generateLettersOnly(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(letters.charAt(faker.random().nextInt(letters.length())));
        }
        return builder.toString();
    }
}


