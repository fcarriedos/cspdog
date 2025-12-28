package com.cspdog.rewriter;

public interface Rewriter {

    String getCSPedResponseBody(String  regularHTMLResponse, String nonce);
    String injectNonce(String preNonceResponse, String nonce);

}
