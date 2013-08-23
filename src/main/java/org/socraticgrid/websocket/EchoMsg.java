/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.socraticgrid.websocket;

/**
 *
 * @author tnguyen
 */
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

//@WebServlet("/websocket")
public class EchoMsg extends WebSocketServlet {

    
//  public void onMessage(String message, Session session) 
//    throws IOException, InterruptedException {
//  
//    // Print the client message for testing purposes
//    System.out.println("Received: " + message);
//  
//    // Send the first message to the client
//    session.getBasicRemote().sendText("This is the first server message");
//  
//    // Send 3 messages to the client every 5 seconds
//    int sentMessages = 0;
//    while(sentMessages < 3){
//      Thread.sleep(5000);
//      session.getBasicRemote().
//        sendText("This is an intermediate server message. Count: " 
//          + sentMessages);
//      sentMessages++;
//    }
//  
//    // Send a final message to the client
//    session.getBasicRemote().sendText("This is the last server message");
//  }
  

    private final Set<EchoMessageInbound> connections =
            new CopyOnWriteArraySet<EchoMessageInbound>();
    
    private static final long serialVersionUID = 1L;
    private volatile int byteBufSize;
    private volatile int charBufSize;
    @Override
    public void init() throws ServletException {
        System.out.println("INIT SOCKET");
        super.init();
        byteBufSize = getInitParameterIntValue("byteBufferMaxSize", 2097152);
        charBufSize = getInitParameterIntValue("charBufferMaxSize", 2097152);
    }

    public int getInitParameterIntValue(String name, int defaultValue) {
        String val = this.getInitParameter(name);
        int result;
        if(null != val) {
            try {
                result = Integer.parseInt(val);
            }catch (Exception x) {
                result = defaultValue;
            }
        } else {
            result = defaultValue;
        }

        return result;
    }
    @Override
    protected StreamInbound createWebSocketInbound(String subProtocol,
            HttpServletRequest request) {
System.out.println("CREATE SOCKET");
        return new EchoMessageInbound(byteBufSize,charBufSize);
    }

    //===========================//

    private final class EchoMessageInbound extends MessageInbound {

        public EchoMessageInbound(int byteBufferMaxSize, int charBufferMaxSize) {
            super();
            setByteBufferMaxSize(byteBufferMaxSize);
            setCharBufferMaxSize(charBufferMaxSize);
        }
        
        @Override
        protected void onOpen(WsOutbound outbound) {
            System.out.println("OPENING SOCKET");
        }

        @Override
        protected void onClose(int status) {
            System.out.println("CLOSING SOCKET with STATUS:"+ status);
        }

        @Override
        protected void onBinaryMessage(ByteBuffer message) throws IOException {
            getWsOutbound().writeBinaryMessage(message);
        }

        @Override
        protected void onTextMessage(CharBuffer message) throws IOException {
            
            CharBuffer reply = CharBuffer.wrap("I got it.  Please wait for three additional alerts.");
            
            getWsOutbound().writeTextMessage(message);
            getWsOutbound().writeTextMessage(reply);
            
            // Send 3 messages to the client every 5 seconds
            int sentMessages = 1;
            while(sentMessages < 4){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(EchoMsg.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
                    reply = CharBuffer.wrap("Incoming Alert # " 
                                    + sentMessages);
                    getWsOutbound().writeTextMessage(reply);
                    
                    sentMessages++;
            }
            reply = CharBuffer.wrap("All alerts sent.");
            getWsOutbound().writeTextMessage(reply);
             
            //broadcast("PUSHING ANOTHER MSG.");
        }
        

        private void broadcast(String message) {
            for (EchoMessageInbound connection : connections) {
                try {
                    CharBuffer buffer = CharBuffer.wrap(message);
                    connection.getWsOutbound().writeTextMessage(buffer);
                } catch (IOException ignore) {
                    // Ignore
                }
            }
        }
        
    }
    
    
}