package com.gtfo.snuggle.httpservlet.servlet;

import com.gtfo.snuggle.httpservlet.service.LocalDeviceInetAddrResolverService;
import com.gtfo.snuggle.httpservlet.utils.IPAddressUtil;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.logging.Logger;

public class HttpServerContainer {

    private static final Logger LOGGER = Logger.getLogger(HttpServerContainer.class.getName());
    private static final String HTTP_SERVLET_NAME = "VegaasenSnuggleService/0.1a";

    final private LocalDeviceInetAddrResolverService localAddressResolverService;
    final int listenPort;
    final HttpRequestHandlerRegistry handlerRegistry;
    final HttpParams httpServletParameters;

    ListenerThread listenerThread;

    public HttpServerContainer(LocalDeviceInetAddrResolverService localAddressResolverService) {
        this(localAddressResolverService, 0);
    }

    public HttpServerContainer(LocalDeviceInetAddrResolverService localAddressResolverService, int listenPort) {

        this.localAddressResolverService = localAddressResolverService;
        this.listenPort = listenPort;
        this.handlerRegistry = new HttpRequestHandlerRegistry();

        this.httpServletParameters = new BasicHttpParams();
        this.httpServletParameters.setParameter(CoreProtocolPNames.ORIGIN_SERVER, HTTP_SERVLET_NAME)
                .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
                .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
                .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
                .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);

        if (IPAddressUtil.ANDROID_EMULATOR) {
            startServer();
        }
    }

    public LocalDeviceInetAddrResolverService getLocalAddressResolverService() {
        return localAddressResolverService;
    }

    public int getListenPort() {
        return listenPort;
    }

    public int getLocalPort() {
        return listenerThread != null ? listenerThread.getActualListenPort() : -1;
    }

    public HttpParams getHttpServletParameters() {
        return httpServletParameters;
    }

    public HttpRequestHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    public synchronized void startServer() {
        InetAddress localAddress = getLocalAddressResolverService().getLocalInetAddress();
        if (localAddress == null) {
            LOGGER.severe("Can not start the service. Missing WIFI-coverage?");
            return;
        }

        LOGGER.info("Found local address. Starting web-server.");

        try {
            listenerThread = new ListenerThread(
                    localAddress,
                    getListenPort(),
                    getHttpServletParameters(),
                    getHandlerRegistry());
            listenerThread.start(); // Don't need non-daemon status, we are on Android
        } catch (IOException e) {
            LOGGER.severe("Could not start the service. Error with binding the address? Reason: " + e);
        }
    }

    public synchronized void stopServer() {
        if (listenerThread != null) {
            LOGGER.info("Service stopped, shutting down the web-server.");
            listenerThread.stopListening();
        }
    }

    static class ListenerThread extends Thread {

        private volatile boolean stopped = false; // volatile = variable can/will be changed by other threads
        final HttpParams params;
        final ServerSocket serverSocket;
        final HttpService httpService;

        public ListenerThread(InetAddress address, int port, HttpParams params, HttpRequestHandlerRegistry handlerRegistry)
                throws IOException {

            this.params = params;
            this.serverSocket = new ServerSocket(port, 0, address);

            BasicHttpProcessor httpProcessor = new BasicHttpProcessor();
            httpProcessor.addInterceptor(new ResponseDate());
            httpProcessor.addInterceptor(new ResponseServer());
            httpProcessor.addInterceptor(new ResponseContent());
            httpProcessor.addInterceptor(new ResponseConnControl());

            this.httpService = new HttpService(
                    httpProcessor,
                    new DefaultConnectionReuseStrategy(),
                    new DefaultHttpResponseFactory()
            );
            this.httpService.setParams(params);
            this.httpService.setHandlerResolver(handlerRegistry);
        }

        public int getActualListenPort() {
            return this.serverSocket.getLocalPort();
        }

        public void run() {
            LOGGER.info("Listener is starting on the current port/address. Will be bound " +
                    this.serverSocket.getLocalSocketAddress());
            while (!stopped) {
                try {

                    Socket clientSocket = serverSocket.accept();
                    DefaultHttpServerConnection serverConnection = new DefaultHttpServerConnection();
                    LOGGER.finer("Pairing connection with: " + clientSocket.getInetAddress());
                    serverConnection.bind(clientSocket, params);

                    Thread t = new WorkerThread(httpService, serverConnection);
                    t.setDaemon(true);
                    t.start();


                } catch (InterruptedIOException e) {
                    LOGGER.info("I/O has been interrupted, stopping receiving loop, bytes transfered: "
                            + e.bytesTransferred);
                    break;
                } catch (SocketException e) {
                    if (!stopped) {
                        LOGGER.info("Exception using server socket: " + e.getMessage());
                    }
                    break;
                } catch (IOException e) {
                    LOGGER.severe("I/O error initializing worker thread, aborting: " + e);
                    break;
                }
            }
        }

        public void stopListening() {
            try {
                stopped = true;
                if (!serverSocket.isClosed()) {
                    LOGGER.fine("Closing server socket");
                    serverSocket.close();
                }
            } catch (Exception e) {
                LOGGER.info("Could not close server socket: " + e.getMessage());
            }
        }
    }

    static class WorkerThread extends Thread {

        final HttpService httpservice;
        final HttpServerConnection conn;

        public WorkerThread( final HttpService httpservice, final HttpServerConnection conn) {
            super();
            this.httpservice = httpservice;
            this.conn = conn;
        }

        public void run() {
            HttpContext context = new BasicHttpContext(null);
            try {
                while (!Thread.interrupted() && this.conn.isOpen()) {
                    this.httpservice.handleRequest(this.conn, context);
                }
            } catch (ConnectionClosedException ex) {
                LOGGER.fine("Client closed connection");
            } catch (SocketTimeoutException ex) {
                LOGGER.fine("Server-side closed socket (this is 'normal' behavior of Apache HTTP Core!): " + ex.getMessage());
            } catch (IOException ex) {
                // Could be a peer connection reset, no warning
                LOGGER.fine("I/O exception during HTTP request processing: " + ex.getMessage());
            } catch (HttpException ex) {
                throw new RuntimeException("Request malformed: " + ex.getMessage(), ex);
            } finally {
                try {
                    conn.shutdown();
                } catch (IOException ex) {
                    LOGGER.fine("Error closing connection: " + ex.getMessage());
                }
            }
        }

    }

}
