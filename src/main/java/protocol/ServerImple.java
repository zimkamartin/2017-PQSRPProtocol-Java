package protocol;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.IntStream;

import static protocol.Utils.computeUNtt;

public class ServerImple implements Server {

    private final int n;
    private final BigInteger q;
    private final int eta;
    private final PublicParams publicParams;
    private final Engine engine = new EngineImple();
    private final Mlkem mlkem;
    private final Ntt ntt;
    private final Magic magic;
    private final Map<ByteArrayWrapper, ClientsPublics> database = new HashMap<>();
    // TODO figure out what to do with these 3 vars //
    private byte[] skj = null;
    private List<BigInteger> piNtt = null;
    private List<BigInteger> pjNtt = null;

    public ServerImple(int n, BigInteger q, int eta) {
        this.n = n;
        this.q = q;
        this.eta = eta;
        this.publicParams = new PublicParams(this.n, this.q, this.eta);
        this.mlkem = new MlkemImple(this.n, this.q);
        this.ntt = new NttImple(this.n, this.q);
        this.magic = new MagicImple(this.q);
    }

    @Override
    public PublicParams getPublicParams() {
        return publicParams;
    }

    @Override
    public void enrollClient(byte[] publicSeedForA, byte[] I, byte[] salt, List<BigInteger> vNtt) {
        database.put(new ByteArrayWrapper(I.clone()), new ClientsPublics(publicSeedForA.clone(), salt.clone(), List.copyOf(vNtt)));
    }

    @Override
    public SaltEphPublicSignal computeSharedSecret(byte[] I, List<BigInteger> piNtt) {
        ByteArrayWrapper wrappedIdentity = new ByteArrayWrapper(I.clone());
        this.piNtt = List.copyOf(piNtt);
        List<BigInteger> constantTwoPolyNtt = ntt.generateConstantTwoPolynomialNtt();
        // Extract database. //  // TODO: handle when key is not there
        byte[] publicSeedForA = database.get(wrappedIdentity).getPublicSeedForA();
        List<BigInteger> vNtt = database.get(wrappedIdentity).getVerifierNtt();
        byte[] salt = database.get(wrappedIdentity).getSalt();
        // pj = as1' + 2e1' + v //
        // Create polynomial a from public seed.
        List<BigInteger> aNtt = new ArrayList<>(Collections.nCopies(n, null));
        mlkem.generateUniformPolynomialNtt(engine, aNtt, publicSeedForA);
        // Compute s1'.
        List<BigInteger> s1PrimeNtt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Compute e1'.
        List<BigInteger> e1PrimeNtt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Do all the math.
        this.pjNtt = ntt.addPolys(Utils.multiply2NttTuplesAndAddThemTogetherNtt(ntt, aNtt, s1PrimeNtt, constantTwoPolyNtt, e1PrimeNtt), vNtt);
        // u = XOF(H(pi || pj)) //
        List<BigInteger> uNtt = computeUNtt(engine, mlkem, n, piNtt, pjNtt);
        // kj = (v + pi)s1' + uv + 2e1''' //
        // Compute e1'''.
        List<BigInteger> e1TriplePrimeNtt = Utils.generateRandomErrorPolyNtt(publicParams, mlkem, engine, ntt);
        // Do all the math.
        List<BigInteger> fstBracket = ntt.addPolys(vNtt, piNtt);
        List<BigInteger> kj = Utils.multiply3NttTuplesAndAddThemTogether(ntt, fstBracket, s1PrimeNtt, uNtt, vNtt, constantTwoPolyNtt, e1TriplePrimeNtt);
        // wj = Cha(kj) //
        List<Integer> wj = IntStream.range(0, n).mapToObj(i -> magic.signalFunction(engine, kj.get(i))).toList();
        // sigmaj = Mod_2(kj, wj) //
        List<Integer> sigmaj = IntStream.range(0, n).mapToObj(i -> magic.robustExtractor(kj.get(i), wj.get(i))).toList();
        // skj = SHA3-256(sigmaj) //
        this.skj = Utils.hashConvertIntegerListToByteArray(n, engine, sigmaj);

        return new SaltEphPublicSignal(salt.clone(), List.copyOf(pjNtt), List.copyOf(wj));
    }

    @Override
    public byte[] verifyEntities(byte[] m1) {
        // M1' = SHA3-256(pi || pj || skj) //
        byte[] m1Prime = Utils.concatenateTwoByteArraysAndHash(engine, Utils.concatBigIntegerListsToByteArray(this.piNtt, this.pjNtt), this.skj);
        // VERIFY that M1 == M1'. If true, return M2', else return empty byte array.
        ByteArrayWrapper m1Wrapped = new ByteArrayWrapper(m1);
        ByteArrayWrapper m1PrimeWrapped = new ByteArrayWrapper(m1Prime);
        if (!m1Wrapped.equals(m1PrimeWrapped)) {
            return new byte[0];
        }
        // M2' = SHA3-256(pi || M1' || skj) //
        return Utils.concatenateThreeByteArraysAndHash(engine, Utils.convertBigIntegerListToByteArray(piNtt), m1Prime, this.skj);
    }
}
