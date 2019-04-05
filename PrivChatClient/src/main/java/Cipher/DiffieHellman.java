package Cipher;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class DiffieHellman {
    public static final int PRIME_LENGTH = 1024; //bits
    public static final int SK_LENGTH = 512; //bits
    
    private static final int HMAC_LENGTH = 32; //bits
    private static final int KEY_SIZE = 256; //bits
    private static final int ITERATION_COUNT = 100000;
    private static final int KEY_BYTE_LENGTH = 64; //64bytes = 512 bits
    private static final int IV_LENGTH = 16; //bits

    private BigInteger generator;
    private BigInteger primeModules;
    private BigInteger secretKey;
    private BigInteger publicKey;
    private BigInteger sharedKey;

    public byte[] iv;
    private Random random;
    
    public DiffieHellman(){
        this.random = new Random();
        this.iv = new byte[IV_LENGTH];
    }

    public void startDHagreement(BufferedReader in, PrintWriter out) throws IOException {
        this.primeModules = this.generateBigPrime(PRIME_LENGTH); //médo não é o melhor...
        this.generator = this.generateBigPrime(PRIME_LENGTH); //deveria ser baseado no primeModules...
        this.secretKey = this.generateBigNumber(SK_LENGTH);
        this.publicKey = this.generator.modPow(this.secretKey, this.primeModules);
        this.random.nextBytes(this.iv);

        //send @generator, @primeModules, @publicKey and @iv to Client
        out.println(this.generator);
        out.println(this.primeModules);
        out.println(this.publicKey);
        out.println(Base64.getEncoder().encodeToString(this.iv));
        out.flush();

        //receive @publicKey from Client
        BigInteger pkClient = new BigInteger(String.valueOf(in.readLine()));

        //compute @sharedKey
        this.sharedKey = pkClient.modPow(this.secretKey, this.primeModules);
    }

    public void proceedDHagreement(BufferedReader in, PrintWriter out) throws IOException {

        //receive @generator, @primeModules, @publicKey and @iv to Client
        this.generator = new BigInteger(String.valueOf(in.readLine()));
        this.primeModules = new BigInteger(String.valueOf(in.readLine()));
        BigInteger pkServer = new BigInteger(String.valueOf(in.readLine()));
        this.iv = Base64.getDecoder().decode(in.readLine());

        this.secretKey = this.generateBigNumber(SK_LENGTH);
        this.publicKey = this.generator.modPow(this.secretKey, this.primeModules);

        //send @publicKey to Client

        out.println(this.publicKey);
        out.flush();

        //compute @sharedKey
        this.sharedKey = pkServer.modPow(this.secretKey, this.primeModules);

    }

    public String encrypt(String mensage) {

        try {
            byte[] cipherKey = new byte[KEY_BYTE_LENGTH];
            System.arraycopy(this.sharedKey.toByteArray(), 0, cipherKey, 0, KEY_BYTE_LENGTH);
            byte[] deriveCipherKey = deriveKey( Base64.getEncoder().encodeToString(cipherKey), this.iv, ITERATION_COUNT, KEY_SIZE);

            Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
            Key sk = new SecretKeySpec(deriveCipherKey, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, sk , new IvParameterSpec(iv));

            byte[] hmacKey = new byte[KEY_BYTE_LENGTH];
            System.arraycopy(this.sharedKey.toByteArray(), KEY_BYTE_LENGTH, hmacKey, 0, KEY_BYTE_LENGTH);
            byte[] deriveHmacKey = deriveKey( Base64.getEncoder().encodeToString(hmacKey), this.iv, ITERATION_COUNT, KEY_SIZE);

            Mac hMac = Mac.getInstance("HmacSHA256");
            Key hMacSK = new SecretKeySpec(deriveHmacKey, "HmacSHA256");

            byte[] cipherText = cipher.doFinal(mensage.getBytes());
            hMac.init(hMacSK);
            byte[] hmac = hMac.doFinal(cipherText);

            byte[] cryptogram = new byte[cipherText.length + HMAC_LENGTH];
            System.arraycopy(hmac, 0, cryptogram, 0, HMAC_LENGTH);
            System.arraycopy(cipherText, 0, cryptogram, HMAC_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(cryptogram);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String decrypt(String message) {
        try {
            byte[] messageBytes = Base64.getDecoder().decode(message);
            byte[] hmac = Arrays.copyOfRange(messageBytes, 0, HMAC_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(messageBytes, HMAC_LENGTH, messageBytes.length);

            byte[] hmacKey = new byte[KEY_BYTE_LENGTH];
            System.arraycopy(this.sharedKey.toByteArray(), KEY_BYTE_LENGTH, hmacKey, 0, KEY_BYTE_LENGTH);
            byte[] deriveHmacKey = deriveKey( Base64.getEncoder().encodeToString(hmacKey), this.iv, ITERATION_COUNT, KEY_SIZE);

            Mac hMac = Mac.getInstance("HmacSHA256");
            Key hMacSK = new SecretKeySpec(deriveHmacKey, "HmacSHA256");
            hMac.init(hMacSK);
            byte[] shmac = hMac.doFinal(cipherText);

            if (MessageDigest.isEqual(hmac, shmac)) {
                byte[] cipherKey = new byte[KEY_BYTE_LENGTH];
                System.arraycopy(this.sharedKey.toByteArray(), 0, cipherKey, 0, KEY_BYTE_LENGTH);
                byte[] deriveCipherKey = deriveKey( Base64.getEncoder().encodeToString(cipherKey), this.iv, ITERATION_COUNT, KEY_SIZE);

                Cipher cipher = Cipher.getInstance("AES/CTR/PKCS5PADDING");
                Key sk = new SecretKeySpec(deriveCipherKey,"AES");
                cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(iv));

                byte[] plainText = cipher.doFinal(cipherText);
                return new String(plainText);
            }
            else
                log("Problem!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public byte[] deriveKey(String p, byte[] s, int i, int l) throws Exception {
        PBEKeySpec ks = new PBEKeySpec(p.toCharArray(), s, i, l);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return skf.generateSecret(ks).getEncoded();
    }

    private void log(String s){
        System.out.println(s);
    }
    
    private BigInteger generateBigPrime(int bits) {
        return BigInteger.probablePrime(bits, random);
    }

    private BigInteger generateBigNumber(int bits) {
        return new BigInteger(bits, random);
    }
}