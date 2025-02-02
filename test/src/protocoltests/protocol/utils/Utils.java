package src.protocoltests.protocol.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import shared.messages.model.client.*;
import shared.messages.model.server.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static Map<Class<?>, String> objToNameMapping = new HashMap<>();
    static {
        objToNameMapping.put(Enter.class, "ENTER");
        objToNameMapping.put(EnterResp.class, "ENTER_RESP");
        objToNameMapping.put(BroadcastReq.class, "BROADCAST_REQ");
        objToNameMapping.put(BroadcastResp.class, "BROADCAST_RESP");
        objToNameMapping.put(Broadcast.class, "BROADCAST");
        objToNameMapping.put(Joined.class, "JOINED");
        objToNameMapping.put(ParseError.class, "PARSE_ERROR");
        objToNameMapping.put(Pong.class, "PONG");
        objToNameMapping.put(PongError.class, "PONG_ERROR");
        objToNameMapping.put(Ready.class, "READY");
        objToNameMapping.put(Ping.class, "PING");
        objToNameMapping.put(ClientsReq.class, "CLIENTS_REQ");
        objToNameMapping.put(ClientsResp.class, "CLIENTS_RESP");
        objToNameMapping.put(Clients.class, "CLIENTS");
        objToNameMapping.put(PrivateReq.class, "PRIVATE_REQ");
        objToNameMapping.put(PrivateResp.class, "PRIVATE_RESP");
        objToNameMapping.put(Private.class, "PRIVATE");
        objToNameMapping.put(RPSStart.class, "RPS_START");
        objToNameMapping.put(RPSStartReq.class, "RPS_START_REQ");
        objToNameMapping.put(RPSStartResp.class, "RPS_START_RESP");
        objToNameMapping.put(RPSChoiceReq.class, "RPS_CHOICE_REQ");
        objToNameMapping.put(RPSChoiceResp.class, "RPS_CHOICE_RESP");
        objToNameMapping.put(RPSEnd.class, "RPS_END");
        objToNameMapping.put(RpsError.class, "RPS_ERROR");
        objToNameMapping.put(TransferReq.class, "TRANSFER_REQ");
        objToNameMapping.put(TransferResp.class, "TRANSFER_RESP");
        objToNameMapping.put(TransferAccept.class, "TRANSFER_ACCEPT");
        objToNameMapping.put(TransferAccepted.class, "TRANSFER_ACCEPTED");
        objToNameMapping.put(TransferAcceptResp.class, "TRANSFER_ACCEPT_RESP");
        objToNameMapping.put(TransferReject.class, "TRANSFER_REJECT");
        objToNameMapping.put(TransferRejected.class, "TRANSFER_REJECTED");
        objToNameMapping.put(TransferRejectResp.class, "TRANSFER_REJECT_RESP");
        objToNameMapping.put(TransferSuccess.class, "TRANSFER_SUCCESS");
        objToNameMapping.put(TransferFailed.class, "TRANSFER_FAILED");
        objToNameMapping.put(TransferChecksum.class, "TRANSFER_CHECKSUM");

    }

    public static String objectToMessage(Object object) throws JsonProcessingException {
        Class<?> clazz = object.getClass();
        String header = objToNameMapping.get(clazz);
        if (header == null) {
            throw new RuntimeException("Cannot convert this class to a message");
        }
        String body = mapper.writeValueAsString(object);
        return header + " " + body;
    }

    public static <T> T messageToObject(String message) throws JsonProcessingException {
        String[] parts = message.split(" ", 2);
        if (parts.length > 2 || parts.length == 0) {
            throw new RuntimeException("Invalid message");
        }
        String header = parts[0];
        String body = "{}";
        if (parts.length == 2) {
            body = parts[1];
        }
        Class<?> clazz = getClass(header);
        Object obj = mapper.readValue(body, clazz);
        return (T) clazz.cast(obj);
    }

    private static Class<?> getClass(String header) {
        return objToNameMapping.entrySet().stream()
                .filter(e -> e.getValue().equals(header))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cannot find class belonging to header " + header));
    }
}
