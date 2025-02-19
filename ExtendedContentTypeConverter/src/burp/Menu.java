package burp;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Menu implements IContextMenuFactory {
    private final IExtensionHelpers m_helpers;

    public Menu(IExtensionHelpers helpers) {
        m_helpers = helpers;
    }

    public Utilities utilities = new Utilities();

    public List<JMenuItem> createMenuItems(final IContextMenuInvocation invocation) {
        List<JMenuItem> menus = new ArrayList<>();

        if (invocation.getToolFlag() != IBurpExtenderCallbacks.TOOL_INTRUDER && invocation.getInvocationContext() != IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST){
            return menus;
        }

        JMenuItem sendXMLToRepeater = new JMenuItem("Convert to XML");
        JMenuItem sendJSONToRepeater = new JMenuItem("Convert to JSON");
        JMenuItem sendFormURLEncodedToRepeater = new JMenuItem("Convert to x-www-form-urlencoded");
        JMenuItem sendFormDataToRepeater = new JMenuItem("Convert to multipart/form-data");
        JMenuItem restoreOriginal = new JMenuItem("Restore to original request");

        sendXMLToRepeater.addActionListener(e -> {
            IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
            try {
                byte[] request = utilities.convertToXML(m_helpers, iReqResp);
                if (request != null) {
                    iReqResp.setRequest(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sendJSONToRepeater.addActionListener(e -> {
            IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
            try {
                byte[] request = utilities.convertToJSON(m_helpers, iReqResp);
                if (request != null) {
                    iReqResp.setRequest(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sendFormURLEncodedToRepeater.addActionListener(e -> {
            IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
            try {
                byte[] request = utilities.convertToFormURLEncoded(m_helpers, iReqResp);
                if (request != null) {
                    iReqResp.setRequest(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sendFormDataToRepeater.addActionListener(e -> {
            IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
            try {
                byte[] request = utilities.convertToFormData(m_helpers, iReqResp);
                if (request != null) {
                    iReqResp.setRequest(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        restoreOriginal.addActionListener(e -> {
            IHttpRequestResponse iReqResp = invocation.getSelectedMessages()[0];
            try {
                byte[] request = utilities.rollBack();
                if (request != null) {
                    iReqResp.setRequest(request);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        menus.add(sendXMLToRepeater);
        menus.add(sendJSONToRepeater);
        menus.add(sendFormURLEncodedToRepeater);
        menus.add(sendFormDataToRepeater);
        menus.add(restoreOriginal);

        return menus;
    }

}
