package com.sekoor.client;

import com.google.common.net.HostAndPort;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Responsible for:
 *
 * 1. sending messages to the server
 * 2. receiving messages from the server
 * 3. getting balance from the server
 *
 */
public class SekoorClient {

    private URI sekoorServer;

    public SekoorClient(URI sekoorServerUri) {
        this.sekoorServer = sekoorServerUri;
    }

    public SekoorClient(HostAndPort sekoorServer) {

        String uriString = "http://" + sekoorServer.getHostText() + ":" + sekoorServer.getPort();
        try {
            URI sekoorServerUri = new URI(uriString);
            this.sekoorServer = sekoorServerUri;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Bad uri: " + uriString);
        }
    }

    public URI getSekoorServer() {
        return sekoorServer;
    }


    public BigInteger getRemainingBalance() {
        return BigInteger.ZERO; // TODO:Olle impl
    }
}
