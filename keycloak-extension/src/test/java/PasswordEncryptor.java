import lombok.SneakyThrows;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.security.SecureRandom;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PasswordEncryptor {
    private static final String PASSWORD = "secret";

    private static final int ITERATIONS = 27500;
    private static final int KEY_SIZE = 64 * 8;

    @SneakyThrows
    public static void main(String[] args) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);

        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(PASSWORD.getBytes(UTF_8), salt, ITERATIONS);
        byte[] key = ((KeyParameter) gen.generateDerivedParameters(KEY_SIZE)).getKey();

        System.out.println("PASSWORD_SALT=" + Base64.getEncoder().encodeToString(salt));
        System.out.println("PASSWORD_VALUE=" + Base64.getEncoder().encodeToString(key));
    }
}
