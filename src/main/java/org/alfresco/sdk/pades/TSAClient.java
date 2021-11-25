package org.alfresco.sdk.pades;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Time Stamping Authority (TSA) Client [RFC 3161]
 */
@Service
public class TSAClient {

    @Value("${tsa.url}")
    private URL url;
    @Value("${tsa.username}")
    private String username;
    @Value("${tsa.password}")
    private String password;
    @Value("${tsa.hash.algorithm}")
    private String hashAlgorithm;

    public byte[] getTSToken(byte[] message) throws Exception {

        MessageDigest digest = MessageDigest.getInstance(hashAlgorithm);
        SecureRandom random = new SecureRandom();

        TimeStampRequestGenerator tsaGenerator = new TimeStampRequestGenerator();
        tsaGenerator.setCertReq(true);
        TimeStampRequest request =
            tsaGenerator.generate(getHashObjectIdentifier(digest.getAlgorithm()),
                digest.digest(message),
                BigInteger.valueOf(random.nextInt()));

        TimeStampResponse response = new TimeStampResponse(getTSAResponse(request.getEncoded()));
        response.validate(request);

        return response.getTimeStampToken().getEncoded();

    }

    private byte[] getTSAResponse(byte[] request) throws IOException {

        URLConnection connection = url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Content-Type", "application/timestamp-query");
        if (username != null && password != null) {
            connection.setRequestProperty(username, password);
        }

        // Write request to connection
        try (OutputStream out = connection.getOutputStream()) {
            out.write(request);
        }

        // Read response from connection
        byte[] response;
        try (InputStream in = connection.getInputStream()) {
            response = IOUtils.toByteArray(in);
        }
        return response;

    }

    private ASN1ObjectIdentifier getHashObjectIdentifier(String algorithm) {
        switch (algorithm) {
            case "MD2":
                return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md2.getId());
            case "MD5":
                return new ASN1ObjectIdentifier(PKCSObjectIdentifiers.md5.getId());
            case "SHA-1":
                return new ASN1ObjectIdentifier(OIWObjectIdentifiers.idSHA1.getId());
            case "SHA-224":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha224.getId());
            case "SHA-256":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha256.getId());
            case "SHA-384":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha384.getId());
            case "SHA-512":
                return new ASN1ObjectIdentifier(NISTObjectIdentifiers.id_sha512.getId());
            default:
                return new ASN1ObjectIdentifier(algorithm);
        }
    }
}
