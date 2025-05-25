package protocol;

import protocol.utils.Symmetric;

import java.security.SecureRandom;

// SOURCE: https://github.com/bcgit/bc-java/blob/main/core/src/main/java/org/bouncycastle/pqc/crypto/mlkem/MLKEMEngine.java
// A lot of things deleted, a few changed.

class Engine {

    private SecureRandom random;
    //private MLKEMIndCpa indCpa;

    // constant parameters
    public final static int KyberN = 256;  // !! Changed from 256 to 1024
    public final static int KyberQ = 3329;  // !! Changed from 3329 to 1073479681
    public final static int KyberQinv = 62209;

    public final static int KyberSymBytes = 32; // Number of bytes for Hashes and Seeds
    private final static int KyberSharedSecretBytes = 32; // Number of Bytes for Shared Secret

    public final static int KyberPolyBytes = 384;

    private final static int KyberEta2 = 2;

    private final static int KyberIndCpaMsgBytes = KyberSymBytes;


    // parameters for Kyber{k}
    private final int KyberK;
    private final int KyberPolyVecBytes;
    private final int KyberPolyCompressedBytes;
    private final int KyberPolyVecCompressedBytes;
    private final int KyberEta1;
    private final int KyberIndCpaPublicKeyBytes;
    private final int KyberIndCpaSecretKeyBytes;
    private final int KyberIndCpaBytes;
    private final int KyberPublicKeyBytes;
    private final int KyberSecretKeyBytes;
    private final int KyberCipherTextBytes;

    // Crypto
    private final int CryptoBytes;
    private final int CryptoSecretKeyBytes;
    private final int CryptoPublicKeyBytes;
    private final int CryptoCipherTextBytes;

    private final int sessionKeyLength;
    private final Symmetric symmetric;

    Engine()  // !! SHORTENED since k will always be 4
    {
        this.KyberK = 4;
        this.random = new SecureRandom();
        int k = this.KyberK;

        KyberEta1 = 2;
        KyberPolyCompressedBytes = 160;
        KyberPolyVecCompressedBytes = k * 352;
        sessionKeyLength = 32;

        this.KyberPolyVecBytes = k * KyberPolyBytes;
        this.KyberIndCpaPublicKeyBytes = KyberPolyVecBytes + KyberSymBytes;
        this.KyberIndCpaSecretKeyBytes = KyberPolyVecBytes;
        this.KyberIndCpaBytes = KyberPolyVecCompressedBytes + KyberPolyCompressedBytes;
        this.KyberPublicKeyBytes = KyberIndCpaPublicKeyBytes;
        this.KyberSecretKeyBytes = KyberIndCpaSecretKeyBytes + KyberIndCpaPublicKeyBytes + 2 * KyberSymBytes;
        this.KyberCipherTextBytes = KyberIndCpaBytes;

        // Define Crypto Params
        this.CryptoBytes = KyberSharedSecretBytes;
        this.CryptoSecretKeyBytes = KyberSecretKeyBytes;
        this.CryptoPublicKeyBytes = KyberPublicKeyBytes;
        this.CryptoCipherTextBytes = KyberCipherTextBytes;

        this.symmetric = new Symmetric.ShakeSymmetric();

        //this.indCpa = new MLKEMIndCpa(this);
    }

    public Symmetric getSymmetric()
    {
        return symmetric;
    }
    public static int getKyberEta2()
    {
        return KyberEta2;
    }

    public static int getKyberIndCpaMsgBytes()
    {
        return KyberIndCpaMsgBytes;
    }

    public int getCryptoCipherTextBytes()
    {
        return CryptoCipherTextBytes;
    }

    public int getCryptoPublicKeyBytes()
    {
        return CryptoPublicKeyBytes;
    }

    public int getCryptoSecretKeyBytes()
    {
        return CryptoSecretKeyBytes;
    }

    public int getCryptoBytes()
    {
        return CryptoBytes;
    }

    public int getKyberCipherTextBytes()
    {
        return KyberCipherTextBytes;
    }

    public int getKyberSecretKeyBytes()
    {
        return KyberSecretKeyBytes;
    }

    public int getKyberIndCpaPublicKeyBytes()
    {
        return KyberIndCpaPublicKeyBytes;
    }

    public int getKyberIndCpaSecretKeyBytes()
    {
        return KyberIndCpaSecretKeyBytes;
    }

    public int getKyberIndCpaBytes()
    {
        return KyberIndCpaBytes;
    }

    public int getKyberPublicKeyBytes()
    {
        return KyberPublicKeyBytes;
    }

    public int getKyberPolyCompressedBytes()
    {
        return KyberPolyCompressedBytes;
    }

    public int getKyberK()
    {
        return KyberK;
    }

    public int getKyberPolyVecBytes()
    {
        return KyberPolyVecBytes;
    }

    public int getKyberPolyVecCompressedBytes()
    {
        return KyberPolyVecCompressedBytes;
    }

    public int getKyberEta1()
    {
        return KyberEta1;
    }

    public void getRandomBytes(byte[] buf)
    {
        this.random.nextBytes(buf);
    }
}
