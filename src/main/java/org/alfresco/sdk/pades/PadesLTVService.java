package org.alfresco.sdk.pades;

import org.alfresco.sdk.pdfbox.examples.AddValidationInformation;
import org.apache.pdfbox.pdmodel.encryption.SecurityProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Security;

/**
 * Creates Pades-LTV file from plain PDF using an external TSA
 */
@Component
public class PadesLTVService {

    @Autowired
    SignatureService signatureService;

    public byte[] getPadesLTV(InputStream input) throws Exception {

        Security.addProvider(SecurityProvider.getProvider());

        File temp = Files.createTempFile("pades", "ltv").toFile();
        File output = Files.createTempFile("pades", "ltv").toFile();

        signatureService.signDetached(input, temp);
        new AddValidationInformation().validateSignature(temp, output);

        byte[] padesLTV = Files.readAllBytes(output.toPath());

        Files.deleteIfExists(temp.toPath());
        Files.deleteIfExists(output.toPath());

        return padesLTV;

    }

}
