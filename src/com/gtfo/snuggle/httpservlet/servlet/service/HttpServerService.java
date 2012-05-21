package com.gtfo.snuggle.httpservlet.servlet.service;

import org.apache.http.protocol.HttpRequestHandler;

public interface HttpServerService {

    public int getLocalPort();

    public void addHandler(String pattern, HttpRequestHandler handler);

    public void removeHandler(String pattern);
    
}
