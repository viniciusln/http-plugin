package org.unbiquitous.network.http.connection;

import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.eclipse.jetty.websocket.common.WebSocketSession;
import org.unbiquitous.uos.core.UOSLogging;

@ClientEndpoint
@ServerEndpoint(value = "/" + ClientMode.END_POINT + "/")
public class WebSocketEndpoint {
	private static final Logger LOGGER = UOSLogging.getLogger();

	private WebSocketChannelManager channel;

	public WebSocketEndpoint() {
		channel = channelStorage.get()[0];
	}

	public static void setChannel(WebSocketChannelManager channel) {
		channelStorage.get()[0] = channel;
	}

	private static final InheritableThreadLocal<WebSocketChannelManager[]> channelStorage = new InheritableThreadLocal<WebSocketChannelManager[]>() {
		protected WebSocketChannelManager[] initialValue() {
			return new WebSocketChannelManager[] { null};
		}
	};
	
	@OnOpen
	public void onWebSocketConnect(Session sess) {
		InetSocketAddress remote = ((WebSocketSession) sess).getRemoteAddress();
		String hostName = remote.getHostName();
		LOGGER.finest("Socket Connected: "+ hostName);
	}


	@OnMessage
	public void onWebSocketText(String message, Session session) {
		try {
			LOGGER.finest(String.format("Handling '%s'", message));
			channel.handleIncommingMessage(message, session);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "", e);
		}
	}

	@OnClose
	public void onWebSocketClose(CloseReason reason, Session session) {
		LOGGER.finest("Session " + session.getId() + " Closed: "+ reason.getCloseCode());
		channel.deviceLeft(session);
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		LOGGER.severe(cause.getMessage());
	}

}
