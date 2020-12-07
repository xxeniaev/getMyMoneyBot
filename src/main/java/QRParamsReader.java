import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class QRParamsReader implements IParamable {
    HashMap<String, String> params;

    public QRParamsReader(String linkPhoto) throws FileNotFoundException, UnsupportedEncodingException {
        this.params = this.getQRCode(linkPhoto);
    }

    @Override
    public HashMap<String, String> getParams() {
        return this.params;
    }

    private HashMap<String, String> getQRCode(String linkPhoto) throws FileNotFoundException, UnsupportedEncodingException {
        String decodeText;
        BarCodeDecode dec = new BarCodeDecode();
        URL url = null;
        try {
            url = new URL(linkPhoto);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        decodeText = dec.getQRString(url);
        if (decodeText == null) {
            throw new FileNotFoundException("Не смогли получить QR");
        }
        return splitQuery(decodeText);
    }

    private static HashMap<String, String> splitQuery(String query) throws UnsupportedEncodingException {
        // сплитит query полученный из QR кода и получает на выходе HashMap
        HashMap<String, String> query_pairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.toString()),
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.toString()));
        }
        return query_pairs;
    }
}
