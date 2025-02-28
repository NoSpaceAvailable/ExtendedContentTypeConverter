package burp;/*
 * @(#)burp.IScanIssue.java
 *
 * Copyright PortSwigger Ltd. All rights reserved.
 *
 * This code may be used to extend the functionality of Burp Suite Free Edition
 * and Burp Suite Professional, provided that this usage does not violate the
 * license terms for those products.
 */
/**
 * This interface is used to retrieve details of Scanner issues. Extensions can
 * obtain details of issues by registering an
 * <code>burp.IScannerListener</code> or by calling
 * <code>burp.IBurpExtenderCallbacks.getScanIssues()</code>. Extensions can also add
 * custom Scanner issues by registering an
 * <code>burp.IScannerCheck</code> or calling
 * <code>burp.IBurpExtenderCallbacks.addScanIssue()</code>, and providing their own
 * implementations of this interface
 */
public interface IScanIssue
{
    /**
     * This method returns the URL for which the issue was generated.
     *
     * @return The URL for which the issue was generated.
     */
    java.net.URL getUrl();

    /**
     * This method returns the name of the issue type.
     *
     * @return The name of the issue type (e.g. "SQL injection").
     */
    String getIssueName();

    /**
     * This method returns a numeric identifier of the issue type. See the Burp
     * Scanner help documentation for a listing of all the issue types.
     *
     * @return A numeric identifier of the issue type.
     */
    int getIssueType();

    /**
     * This method returns the issue severity level.
     *
     * @return The issue severity level. Expected values are "High", "Medium",
     * "Low", "Information" or "False positive".
     *
     */
    String getSeverity();

    /**
     * This method returns the issue confidence level.
     *
     * @return The issue confidence level. Expected values are "Certain", "Firm"
     * or "Tentative".
     */
    String getConfidence();

    /**
     * This method returns a background description for this type of issue.
     *
     * @return A background description for this type of issue, or
     * <code>null</code> if none applies.
     */
    String getIssueBackground();

    /**
     * This method returns a background description of the remediation for this
     * type of issue.
     *
     * @return A background description of the remediation for this type of
     * issue, or
     * <code>null</code> if none applies.
     */
    String getRemediationBackground();

    /**
     * This method returns detailed information about this specific instance of
     * the issue.
     *
     * @return Detailed information about this specific instance of the issue,
     * or
     * <code>null</code> if none applies.
     */
    String getIssueDetail();

    /**
     * This method returns detailed information about the remediation for this
     * specific instance of the issue.
     *
     * @return Detailed information about the remediation for this specific
     * instance of the issue, or
     * <code>null</code> if none applies.
     */
    String getRemediationDetail();

    /**
     * This method returns the HTTP messages on the basis of which the issue was
     * generated.
     *
     * @return The HTTP messages on the basis of which the issue was generated.
     * <b>Note:</b> The items in this array should be instances of
     * <code>burp.IHttpRequestResponseWithMarkers</code> if applicable, so that
     * details of the relevant portions of the request and response messages are
     * available.
     */
    IHttpRequestResponse[] getHttpMessages();

    /**
     * This method returns the HTTP service for which the issue was generated.
     *
     * @return The HTTP service for which the issue was generated.
     */
    IHttpService getHttpService();

}
