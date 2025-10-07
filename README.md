# Post-Quantum Secure Remote Password Protocol

This repository contains a **proof-of-concept (PoC)** implementation of the **Post-quantum Secure Remote Password Protocol** presented in [1] (2017), written in Java.  
It is the **practical part of Martin Zimka’s master’s thesis** [2].

> ⚠️ **DISCLAIMER:**  
> This code is a proof-of-concept and **must not be used in production**.  
> Constant-time operation guarantees, side-channel resistance, and known attacks on the protocol require further study.
> The author is not responsible for any consequences arising from the use of this code in practice.

The protocol is implemented in a way that parameters:
- **n** – polynomial degree (modulus for polynomial terms),
- **q** – prime modulus for polynomial coefficients, and
- **η (eta)** – centered binomial distribution parameter  
  can be configured via the [`ProtocolConfiguration`](main/protocol/ProtocolConfiguration.java) class.  
  All protocol components automatically adapt to the chosen configuration.

---

## Repository structure

The repository consists of:
- **`main/`** – source code
- **`test/`** – corresponding unit and integration tests

Everything (packages, classes, methods, and variables) is clearly named and documented.  
Public APIs and complex logic include Javadoc for clarity.

---

### `protocol` package (main)

#### `client`
Implements the **client-side** of the protocol.
- [`ClientImple`](main/protocol/client/ClientImple.java) defines public seed and salt sizes, and implements enrollment and login.
- [`ClientsKnowledge`](main/protocol/client/ClientsKnowledge.java) stores client's identity and password.
- [`LoginResponse`](main/protocol/client/LoginResponse.java) represents protocol’s response to a login attempt.
- [`SessionConfigurationClient`](main/protocol/client/SessionConfigurationClient.java) represents the client’s view of a client–server session.

#### `polynomial`
Implements core **polynomial arithmetic** and NTT (Number Theoretic Transform) logic.
- [`ClassicalPolynomial`](main/protocol/polynomial/ClassicalPolynomial.java) – polynomial in coefficient form.
- [`NttPolynomial`](main/protocol/polynomial/NttPolynomial.java) – polynomial in NTT domain with mathematical operations (add, subtract, multiply).
- [`PolynomialConfig`](main/protocol/polynomial/PolynomialConfig.java) – holds parameters (*n*, *q*, precomputed roots of unity for NTT transformations).
- [`ModuloPoly`](main/protocol/polynomial/ModuloPoly.java) – internal structure used to compute roots of unity.
- [`Utils`](main/protocol/polynomial/Utils.java) – helper methods for polynomial operations.

#### `random`
Implements **custom random generation** used in protocol.
- [`RandomCustom`](main/protocol/random/RandomCustom.java) – interface defining protocol-level randomness operations.
- [`RandomCustomImple`](main/protocol/random/RandomCustomImple.java) – concrete RandomCustom implementation.
- [`BitCursor`](main/protocol/random/BitCursor.java) – helper class for bit-level navigation used in the CBD implementation.

#### `server`
Implements the **server-side** of the protocol.
- [`Server`](main/protocol/server/Server.java) – interface defining enrollment, shared secret derivation, and verification methods.
- [`ServerImple`](main/protocol/server/ServerImple.java) – concrete Server implementation.
- [`ServersDatabase`](main/protocol/server/ServersDatabase.java) – server's database containing `ClientRecord` entries.
- [`SessionConfigurationServer`](main/protocol/server/SessionConfigurationServer.java) – server-side session representation.

#### Other top-level classes
- [`ByteArrayWrapper`](main/protocol/ByteArrayWrapper.java) – immutable wrapper for `byte[]`.
- [`Ding12Imple`](main/protocol/Ding12Imple.java) – implements all functions needed to secretly transform information to other party.
- [`Main`](main/protocol/Main.java) – sets protocol parameters, secrets, and runs a demo client interaction.
- [`ProtocolConfiguration`](main/protocol/ProtocolConfiguration.java) – encapsulates parameters (*n*, *q*, *eta*).
- [`ServersResponseScs`](main/protocol/ServersResponseScs.java) – server’s phase-1 response and session configuration.

---

###  `test` folder

Contains the same package hierarchy as `main`, with:
- **Unit tests** for individual classes (`{Xyz}Test`),
- **Integration test** [`ProtocolTest`](test/protocol/ProtocolTest.java) – validates the entire protocol workflow.

Additional helper classes:
- [`TestPreSeededRandom`](test/protocol/random/TestPreSeededRandom.java) – deterministic variant of `RandomCustomImple` (uses pre-seeded `Random` instead of `SecureRandom`).
- [`TestServerWrapper`](test/protocol/server/TestServerWrapper.java) – simple wrapper around a `Server` implementation providing access to the computed shared secret.

---

## References

1. [Post-Quantum Secure Remote Password Protocol from RLWE Problem](https://eprint.iacr.org/2017/1196.pdf)
2. [Post-Quantum Secure Remote Password Protocol (Master’s Thesis, 2025)](https://is.muni.cz/th/tbd/)

---

**Author:** Martin Zimka (2025)
