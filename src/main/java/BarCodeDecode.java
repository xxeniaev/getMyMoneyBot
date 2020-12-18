import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public class BarCodeDecode {
    public String getQRString(URL file) {
        String result = null;
        Map<DecodeHintType, Object> hintsMap = new EnumMap<>(DecodeHintType.class);
        hintsMap.put(DecodeHintType.TRY_HARDER, Boolean.FALSE);
        try {
            result = BarCodeUtil.decode(file, hintsMap);
            if (result == null)
            {
                hintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                result = BarCodeUtil.decode(file, hintsMap);
            }
        } catch (FileNotFoundException tmpExpt) {
            System.out.println("main: " + "Excpt err! (" + tmpExpt.getMessage() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}

class BarCodeUtil
{
    public static String decode(URL file, Map<DecodeHintType, Object> hints) throws Exception
    {
        BufferedImage tmpBfrImage;
        try
        {
            tmpBfrImage = ImageIO.read(file);
        }
        catch (IOException tmpIoe)
        {
            throw new Exception(tmpIoe.getMessage());
        }
        if (tmpBfrImage == null)
            throw new IllegalArgumentException("Could not decode image.");
        LuminanceSource tmpSource = new BufferedImageLuminanceSource(tmpBfrImage);
        BinaryBitmap tmpBitmap = new BinaryBitmap(new HybridBinarizer(tmpSource));
        MultiFormatReader tmpBarcodeReader = new MultiFormatReader();
        Result tmpResult;
        String tmpFinalResult;
        try
        {
            if (hints != null && ! hints.isEmpty())
                tmpResult = tmpBarcodeReader.decode(tmpBitmap, hints);
            else
                tmpResult = tmpBarcodeReader.decode(tmpBitmap);
            tmpFinalResult = String.valueOf(tmpResult.getText());
        }
        catch (Exception tmpExcpt)
        {
            throw new Exception("BarCodeUtil.decode Excpt err - " + tmpExcpt.toString() + " - " + tmpExcpt.getMessage());
        }
        return tmpFinalResult;
    }
}
