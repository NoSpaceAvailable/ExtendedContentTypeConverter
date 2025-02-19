package burp;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.*;

/**
 * Modified by NoSpaceAvailable
 * */

public class Utilities {

    private byte[] original = null;

    private final FormURLUtilities externalUtilities = new FormURLUtilities();


    public byte[] convertToXML(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) throws Exception {
        byte[] request = requestResponse.getRequest();

        if (this.original == null) {
            this.original = request;
        }

        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")){
            request = helpers.toggleRequestMethod(request);
        }

        IRequestInfo requestInfo = helpers.analyzeRequest(request);

        int bodyOffset = requestInfo.getBodyOffset();

        byte content_type = requestInfo.getContentType();

        String body = new String(request, bodyOffset, request.length - bodyOffset, "UTF-8");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = XMLUtilities.createDocument();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        if (xml.indexOf("<root>") == -1) {
            xml.append("<root>");
        }

        if (content_type == 0 || content_type == 1) {
            JSONObject json = FormURLParser.parse(body);
            body = json.toString(2);
        } else if (content_type == 2) {
            // multipart/form-data
            String formDataToFormURL = new String(externalUtilities.formDataToFormUrl(body));
            body = FormURLParser.parse(formDataToFormURL).toString();
        }

        boolean success = true;

        try {
            Object item = new JSONTokener(body).nextValue();
            Object json = item;

            xml.append(XML.toString(json));
            if (xml.indexOf("</root>") == -1) {
                xml.append("</root>");
            }

            DocumentBuilder builder = factory.newDocumentBuilder();

            ByteArrayInputStream input =  new ByteArrayInputStream(
                    xml.toString().getBytes("UTF-8"));
            doc = builder.parse(input);

        } catch (Exception e){
            success = false;

        }

        if (!success) {
            return null;
        } else {

            List<String> headers;

            headers = helpers.analyzeRequest(request).getHeaders();

            headers.removeIf(s -> s.contains("Content-Type"));

            headers.add("Content-Type: application/xml;charset=UTF-8");

            return helpers.buildHttpMessage(headers,prettyPrint(doc).getBytes());

        }

    }

    public byte[] convertToJSON(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) {
        byte[] request = requestResponse.getRequest();

        if (this.original == null) {
            this.original = request;
        }

        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")){
            request = helpers.toggleRequestMethod(request);
        }

        IRequestInfo requestInfo = helpers.analyzeRequest(request);

        int bodyOffset = requestInfo.getBodyOffset();

        byte content_type = requestInfo.getContentType();

        String body = new String(request, bodyOffset, request.length - bodyOffset);

        String json = "";

        boolean success = true;

        try {
            if (content_type == 3) {
                // My trick to fix nested root growth bug of original code
                JSONObject _xmlJSONObject = XML.toJSONObject(body);
                JSONObject xmlJSONObject = (JSONObject) _xmlJSONObject.get("root");
                json = xmlJSONObject.toString(2);
            } else if (content_type == 2) {
                // multipart/form-data
                String formDataToFormURL = new String(externalUtilities.formDataToFormUrl(body));
                json = FormURLParser.parse(formDataToFormURL).toString();
            } else if (content_type == 0 || content_type == 1) {
                json = FormURLParser.parse(body).toString(2);
            } else {
                json = body;
            }
        } catch (Exception e){
            success = false;

        }

        if (!success) {
            return request;
        } else {

            List<String> headers;

            headers = helpers.analyzeRequest(request).getHeaders();

            headers.removeIf(s -> s.contains("Content-Type"));

            headers.add("Content-Type: application/json;charset=UTF-8");

            return helpers.buildHttpMessage(headers, json.getBytes());

        }


    }

    public byte[] convertToFormURLEncoded(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) {
        byte[] request = requestResponse.getRequest();

        if (this.original == null) {
            this.original = request;
        }

        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")){
            request = helpers.toggleRequestMethod(request);
        }

        IRequestInfo requestInfo = helpers.analyzeRequest(request);

        int bodyOffset = requestInfo.getBodyOffset();

        byte content_type = requestInfo.getContentType();

        String body = new String(request, bodyOffset, request.length - bodyOffset);

        byte[] result = null;

        boolean success = false;

        try {
            if (content_type == 3) {
                JSONObject _xmlJSONObject = XML.toJSONObject(body);
                JSONObject xmlJSONObject = (JSONObject) _xmlJSONObject.get("root");
                result = externalUtilities.jsonToFormURL(xmlJSONObject);
            } else if (content_type == 2) {
                // multipart/form-data
                result = externalUtilities.formDataToFormUrl(body);
            } else if (content_type == 4) {
                // application/json
                result = externalUtilities.jsonToFormURL(new JSONObject(body));
            } else if (content_type == 0 || content_type == 1) {
                return request;
            }
            success = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        };

        if (!success) {
            return request;
        } else {
            List<String> headers;

            headers = helpers.analyzeRequest(request).getHeaders();

            headers.removeIf(s -> s.contains("Content-Type"));

            headers.add("Content-Type: application/x-www-form-urlencoded;charset=UTF-8");

            return helpers.buildHttpMessage(headers, result);
        }
    }

    public byte[] convertToFormData(IExtensionHelpers helpers, IHttpRequestResponse requestResponse) {
        byte[] request = requestResponse.getRequest();

        if (this.original == null) {
            this.original = request;
        }

        if (Objects.equals(helpers.analyzeRequest(request).getMethod(), "GET")){
            request = helpers.toggleRequestMethod(request);
        }

        IRequestInfo requestInfo = helpers.analyzeRequest(request);

        int bodyOffset = requestInfo.getBodyOffset();

        byte content_type = requestInfo.getContentType();

        String body = new String(request, bodyOffset, request.length - bodyOffset);

        String result = new String(request);

        String salt = FormDataUtilities.getSaltString(16);

        String randomBoundary = "----WebKitFormBoundary" + salt;

        boolean success = false;

        try {
            if (content_type == 2) return request;
            if (content_type == 3) {
                // XML
                JSONObject _xmlJSONObject = XML.toJSONObject(body);
                JSONObject xmlJSONObject = (JSONObject) _xmlJSONObject.get("root");
                body = new String(externalUtilities.jsonToFormURL(xmlJSONObject));
            } else if (content_type == 4) {
                // application/json
                body = new String(externalUtilities.jsonToFormURL(new JSONObject(body)));
            }
            result = FormDataUtilities.buildFormData(body, salt);
            success = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        };

        if (!success) {
            return request;
        } else {
            List<String> headers;

            headers = helpers.analyzeRequest(request).getHeaders();

            headers.removeIf(s -> s.contains("Content-Type"));

            headers.add("Content-Type: multipart/form-data; boundary=" + randomBoundary);

            return helpers.buildHttpMessage(headers, result.toString().getBytes());
        }
    }

    public byte[] rollBack() {
        return this.original;
    }

    public static Map<String,String> splitQuery(String body) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<>();
        List<String> pairs = Arrays.asList(body.split("&"));

        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, "");
            }
            final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
            query_pairs.put(key,value.trim());
        }
        return query_pairs;
    }

    public static String prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
       return(out.toString());
    }
}
