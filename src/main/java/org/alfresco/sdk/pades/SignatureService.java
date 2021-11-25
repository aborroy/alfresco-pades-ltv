package org.alfresco.sdk.pades;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * Timestamping signature services
 */
@Service
public class SignatureService implements SignatureInterface  {

    @Autowired
    TSAClient tsaClient;

    /**
     * Get timestamp TS from TSA
     */
    @Override
    public byte[] sign(InputStream content) throws IOException {
        try
        {
            return tsaClient.getTSToken(IOUtils.toByteArray(content));
        }
        catch (Exception e)
        {
            throw new IOException("Hashing-Algorithm not found for TimeStamping", e);
        }
    }

    /**
     * Prepare document for LTV with a TS signature
     */
    public void signDetached(InputStream input, File output) throws Exception {

        try (PDDocument doc = PDDocument.load(input);
             FileOutputStream fos = new FileOutputStream(output))
        {
            PDSignature signature = new PDSignature();
            signature.setType(COSName.DOC_TIME_STAMP);
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(COSName.getPDFName("ETSI.RFC3161"));
            signature.setSignDate(Calendar.getInstance());

            // No certification allowed because /Reference not allowed in signature directory
            // see ETSI EN 319 142-1 Part 1 and ETSI TS 102 778-4

            doc.addSignature(signature, this);
            doc.saveIncremental(fos);
        }

    }

}
