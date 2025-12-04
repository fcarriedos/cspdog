package com.cspdog.rewriter;

import jakarta.servlet.http.HttpServletResponse;

public interface Rewriter {

    String getCSPedResponse(String  regularHTMLResponse, String nonce);
    String injectNonce(String preNonceResponse, String nonce);
    String getHashesForEventHandlers(String regularHTMLResponse);
    String getCSPHeaders(String nonce);

}
