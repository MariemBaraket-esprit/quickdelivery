package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QRCodeGenerator {

    /**
     * Génère un QR code à partir des données fournies et l'enregistre dans un fichier
     *
     * @param data Les données à encoder dans le QR code
     * @param filePath Le chemin du fichier où enregistrer le QR code
     * @throws WriterException Si une erreur survient lors de la génération du QR code
     * @throws IOException Si une erreur survient lors de l'écriture du fichier
     */
    public static void generateQRCode(String data, String filePath) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);
        Path path = Paths.get(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    /**
     * Génère un QR code à partir des données fournies et le retourne sous forme de BitMatrix
     *
     * @param data Les données à encoder dans le QR code
     * @param size La taille du QR code en pixels
     * @return La BitMatrix représentant le QR code
     * @throws WriterException Si une erreur survient lors de la génération du QR code
     */
    public static BitMatrix generateQRCodeMatrix(String data, int size) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size);
    }
}