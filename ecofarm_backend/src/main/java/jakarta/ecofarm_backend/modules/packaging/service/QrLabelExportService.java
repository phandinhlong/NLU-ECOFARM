package jakarta.ecofarm_backend.modules.packaging.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.Batch;
import jakarta.ecofarm_backend.infrastructure.persistence.entity.QrCode;
import jakarta.enterprise.context.ApplicationScoped;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Sinh mã QR ngẫu nhiên và xuất tệp tem PDF / ZPL / CSV (FR-PCK-003, FR-PCK-004).
 */
@ApplicationScoped
public class QrLabelExportService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int CODE_LENGTH = 24;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(BASE62.charAt(RANDOM.nextInt(BASE62.length())));
        }
        return sb.toString();
    }

    public String buildTraceUrl(String frontendBaseUrl, String code) {
        String base = (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                ? ""
                : frontendBaseUrl.replaceAll("/+$", "");
        return base + "/t/" + code;
    }

    public byte[] exportPdf(Batch batch, List<QrCode> codes, boolean reprint, String frontendBaseUrl) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 24, 24, 24, 24);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.DARK_GRAY);
            Font reprintFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.RED);

            String titleText = "ECOFARM QR LABELS - " + batch.getBatchCode() + (reprint ? " [REPRINT]" : "");
            Paragraph title = new Paragraph(titleText, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph meta = new Paragraph("Batch ID: " + batch.getId() + " | Total labels: " + codes.size(), subFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(12f);
            document.add(meta);

            int cols = 3;
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            for (QrCode qr : codes) {
                String traceUrl = buildTraceUrl(frontendBaseUrl, qr.getCode());
                byte[] qrPng = renderQrPng(traceUrl, 160);
                Image img = Image.getInstance(qrPng);
                img.scaleToFit(95, 95);
                img.setAlignment(Element.ALIGN_CENTER);

                PdfPCell cell = new PdfPCell();
                cell.setPadding(8f);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.addElement(img);

                Paragraph pBatch = new Paragraph(batch.getBatchCode() + " | #" + String.format("%05d", qr.getSerial()), labelFont);
                pBatch.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(pBatch);

                Paragraph pCode = new Paragraph("/t/" + qr.getCode(), codeFont);
                pCode.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(pCode);

                if (reprint) {
                    Paragraph pReprint = new Paragraph("REPRINT (#" + qr.getReprintCount() + ")", reprintFont);
                    pReprint.setAlignment(Element.ALIGN_CENTER);
                    cell.addElement(pReprint);
                }

                table.addCell(cell);
            }

            int remainder = codes.size() % cols;
            if (remainder != 0) {
                for (int i = 0; i < cols - remainder; i++) {
                    PdfPCell empty = new PdfPCell(new Phrase(""));
                    empty.setBorder(PdfPCell.NO_BORDER);
                    table.addCell(empty);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate QR PDF", e);
        }
    }

    public byte[] exportZpl(Batch batch, List<QrCode> codes, boolean reprint, String frontendBaseUrl) {
        StringBuilder zpl = new StringBuilder();
        for (QrCode qr : codes) {
            String url = buildTraceUrl(frontendBaseUrl, qr.getCode());
            zpl.append("^XA\n")
                    .append("^FO30,20^BQN,2,5^FDQA,").append(url).append("^FS\n")
                    .append("^FO200,30^A0N,28,28^FD").append(batch.getBatchCode()).append("^FS\n")
                    .append("^FO200,65^A0N,24,24^FDSerial: #").append(qr.getSerial()).append("^FS\n")
                    .append("^FO200,98^A0N,20,20^FD/t/").append(qr.getCode()).append("^FS\n");
            if (reprint) {
                zpl.append("^FO200,128^A0N,22,22^FDREPRINT^FS\n");
            }
            zpl.append("^XZ\n");
        }
        return zpl.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportCsv(Batch batch, List<QrCode> codes, boolean reprint, String frontendBaseUrl) {
        StringBuilder csv = new StringBuilder("serial,code,url,batchCode,status,reprint,reprintCount\n");
        for (QrCode qr : codes) {
            String url = buildTraceUrl(frontendBaseUrl, qr.getCode());
            csv.append(qr.getSerial()).append(',')
                    .append(qr.getCode()).append(',')
                    .append(url).append(',')
                    .append(batch.getBatchCode()).append(',')
                    .append(qr.getStatus()).append(',')
                    .append(reprint).append(',')
                    .append(qr.getReprintCount() != null ? qr.getReprintCount() : 0)
                    .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] renderQrPng(String content, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                content,
                BarcodeFormat.QR_CODE,
                size,
                size,
                Map.of(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M, EncodeHintType.MARGIN, 1)
        );
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
