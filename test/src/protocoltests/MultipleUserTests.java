package src.protocoltests;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.*;
import shared.messages.model.client.*;
import shared.messages.model.server.*;
import shared.utils.JsonUtils;
import src.protocoltests.protocol.utils.Utils;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.*;

class MultipleUserTests {

    private final static Properties PROPS = new Properties();

    private Socket socketUser1, socketUser2, socketUser3;
    private BufferedReader inUser1, inUser2, inUser3;
    private PrintWriter outUser1, outUser2, outUser3;

    private final static int MAX_DELTA_ALLOWED_MS = 500;

    @BeforeAll
    static void setupAll() throws IOException {
        InputStream in = MultipleUserTests.class.getResourceAsStream("testconfig.properties");
        PROPS.load(in);
        in.close();
    }

    @BeforeEach
    void setup() throws IOException {
        socketUser1 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser1 = new BufferedReader(new InputStreamReader(socketUser1.getInputStream()));
        outUser1 = new PrintWriter(socketUser1.getOutputStream(), true);

        socketUser2 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser2 = new BufferedReader(new InputStreamReader(socketUser2.getInputStream()));
        outUser2 = new PrintWriter(socketUser2.getOutputStream(), true);

        socketUser3 = new Socket(PROPS.getProperty("host"), Integer.parseInt(PROPS.getProperty("port")));
        inUser3 = new BufferedReader(new InputStreamReader(socketUser3.getInputStream()));
        outUser3 = new PrintWriter(socketUser3.getOutputStream(), true);
    }

    @AfterEach
    void cleanup() throws IOException {
        socketUser1.close();
        socketUser2.close();
        socketUser3.close();
    }

    @Test
    void tc31JoinedIsReceivedByOtherUserWhenUserConnects() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready msg
        receiveLineWithTimeout(inUser2); //ready msg

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        //JOINED is received by user1 when user2 connects
        /* This test is expected to fail with the given NodeJS server because the JOINED is not implemented.
           Make sure the test works when implementing your own server in Java */
        String resIdent = receiveLineWithTimeout(inUser1);
        Joined joined = Utils.messageToObject(resIdent);

        assertEquals(new Joined("user2"), joined);
    }

    @Test
    void tc32BroadcastMessageIsReceivedByOtherConnectedClients() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready msg
        receiveLineWithTimeout(inUser2); //ready msg

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK
        /* This test is expected to fail with the given NodeJS server because the JOINED is not implemented.
           Make sure the test works when implementing your own server in Java */
        receiveLineWithTimeout(inUser1); //JOINED

        //send BROADCAST from user 1
        outUser1.println(Utils.objectToMessage(new BroadcastReq("messagefromuser1")));

        outUser1.flush();
        String fromUser1 = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp1 = Utils.messageToObject(fromUser1);

        assertEquals("OK", broadcastResp1.status());

        String fromUser2 = receiveLineWithTimeout(inUser2);
        Broadcast broadcast2 = Utils.messageToObject(fromUser2);

        assertEquals(new Broadcast("user1", "messagefromuser1"), broadcast2);

        //send BROADCAST from user 2
        outUser2.println(Utils.objectToMessage(new BroadcastReq("messagefromuser2")));
        outUser2.flush();
        fromUser2 = receiveLineWithTimeout(inUser2);
        BroadcastResp broadcastResp2 = Utils.messageToObject(fromUser2);
        assertEquals("OK", broadcastResp2.status());

        fromUser1 = receiveLineWithTimeout(inUser1);
        Broadcast broadcast1 = Utils.messageToObject(fromUser1);

        assertEquals(new Broadcast("user2", "messagefromuser2"), broadcast1);
    }

    @Test
    void tc33EnterMessageWithAlreadyConnectedUsernameReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user 1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect using same username
        outUser2.println(Utils.objectToMessage(new Enter("user1")));
        outUser2.flush();
        String resUser2 = receiveLineWithTimeout(inUser2);
        EnterResp enterResp = Utils.messageToObject(resUser2);
        assertEquals(new EnterResp("ERROR", 5000), enterResp);
    }

    @Test
    void tc34BroadcastMessageFromUnregisteredUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        outUser1.println(Utils.objectToMessage(new BroadcastReq("messagefromuser1")));
        outUser1.flush();
        String res = receiveLineWithTimeout(inUser1);
        BroadcastResp broadcastResp = Utils.messageToObject(res);
        assertEquals("ERROR", broadcastResp.status());
        assertEquals(6000, broadcastResp.code());
    }

    @Test
    void tc35ClientsRequestFromUnregisteredUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        outUser1.println("CLIENTS_REQ");
        outUser1.flush();
        String res = receiveLineWithTimeout(inUser1);
        ClientsResp clientsResp = Utils.messageToObject(res);
        assertEquals("error", clientsResp.status());
        assertEquals(6000, clientsResp.code());
    }

    @Test
    void tc36ClientsRequestFromRegisteredUserReturnsConnectedClient() throws JsonProcessingException{
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // joined
        receiveLineWithTimeout(inUser1);

        // Request clients from user1
        outUser1.println("CLIENTS_REQ");
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        Clients clients = Utils.messageToObject(res);
        assertEquals(1, clients.clients().size());
        assertEquals("user2", clients.clients().get(0));
    }

    @Test
    void tc37PrivateMessageIsReceivedByRecipient() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // joined
        receiveLineWithTimeout(inUser1);

        // Send private message from user1 to user2
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", "private message")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser2);
        Private privateMsg = Utils.messageToObject(res);
        assertEquals(new Private("user1", "private message"), privateMsg);
    }

    @Test
    void tc38PrivateMessageFromUnregisteredUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Send private message from unregistered user
        outUser1.println(Utils.objectToMessage(new PrivateReq("user2", "private message")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(res);
        assertEquals("ERROR", privateResp.status());
        assertEquals(6000, privateResp.code());
    }

    @Test
    void tc39PrivateMessageToInvalidRecipientReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send private message to invalid recipient
        outUser1.println(Utils.objectToMessage(new PrivateReq("invaliduser", "private message")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(res);
        assertEquals("ERROR", privateResp.status());
        assertEquals(9001, privateResp.code());
    }

    @Test
    void tc40PrivateMessageToSelfReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send private message
        outUser1.println(Utils.objectToMessage(new PrivateReq("user1", "private message")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        PrivateResp privateResp = Utils.messageToObject(res);
        assertEquals("ERROR", privateResp.status());
        assertEquals(9002, privateResp.code());
    }

    @Test
    void tc41RpsStartRequestFromUnregisteredUserReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Send RPS start request from unregistered user
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSStartResp rpsStartResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsStartResp.status());
        assertEquals(6000, rpsStartResp.code());
    }

    @Test
    void tc42RpsStartRequestWithInvalidOpponentReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send RPS start request with invalid opponent
        outUser1.println(Utils.objectToMessage(new RPSStartReq("invaliduser")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSStartResp rpsStartResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsStartResp.status());
        assertEquals(9001, rpsStartResp.code());
    }

    @Test
    void tc43RpsStartRequestWithSelfReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send RPS start request with self
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user1")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSStartResp rpsStartResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsStartResp.status());
        assertEquals(9002, rpsStartResp.code());
    }

    @Test
    void tc44RpsStartRequestIsReceivedByOpponent() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser2);
        RPSStart rpsStart = Utils.messageToObject(res);
        assertEquals(new RPSStart("user1"), rpsStart);
    }

    @Test
    void tc45RpsStartRequestWhenTwoUsersAlreadyPlayingReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //RPS_START

        // Send RPS start request from user2 to user1
        outUser2.println(Utils.objectToMessage(new RPSStartReq("user1")));
        outUser2.flush();

        String res = receiveLineWithTimeout(inUser2);
        RPSStartResp rpsStartResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsStartResp.status());
        assertEquals(10002, rpsStartResp.code());
    }

    @Test
    void tc46RpsGameTimeoutsWhenUsersDontMakeChoice() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //RPS_START
        receiveLineWithTimeout(inUser1); //RPS_START

        // don't send RPS response and wait for timeout (without MAX_DELTA_ALLOWED_MS)
        String res = receiveLineWithTimeout(inUser2, 10_500);
        while (res.contains("PING")) {
            res = receiveLineWithTimeout(inUser2, 10_500);
        }
        String res2 = receiveLineWithTimeout(inUser1, 10_500);
        while (res2.contains("PING")) {
            res2 = receiveLineWithTimeout(inUser1, 10_500);
        }

        RPSChoiceResp rpsChoiceResp = JsonUtils.messageToClass(res);
        assertEquals("ERROR", rpsChoiceResp.status());
        assertEquals(10008, rpsChoiceResp.code());

        RPSChoiceResp rpsChoiceResp2 = Utils.messageToObject(res2);
        assertEquals("ERROR", rpsChoiceResp2.status());
        assertEquals(10008, rpsChoiceResp2.code());
    }

    @Test
    void tc47RpsGameChoiceWhenNoGameAvailableReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Send RPS choice from user1
        outUser1.println(Utils.objectToMessage(new RPSChoiceReq("ROCK")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSChoiceResp rpsChoiceResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsChoiceResp.status());
        assertEquals(10003, rpsChoiceResp.code());
    }

    @Test
    void tc48RpsGameChoiceFromUserNotInMatchReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message
        receiveLineWithTimeout(inUser3); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); //OK

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); //OK

        // Connect user3
        outUser3.println(Utils.objectToMessage(new Enter("user3")));
        outUser3.flush();
        receiveLineWithTimeout(inUser3); //OK

        // joined
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //RPS_START
        receiveLineWithTimeout(inUser1); //RPS_START

        // Send RPS choice from user3
        outUser3.println(Utils.objectToMessage(new RPSChoiceReq("ROCK")));
        outUser3.flush();

        String res = receiveLineWithTimeout(inUser3);
        RPSChoiceResp rpsChoiceResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsChoiceResp.status());
        assertEquals(12001, rpsChoiceResp.code());
    }

    @Test
    void tc49RpsGameSecondChoiceReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //RPS_START
        receiveLineWithTimeout(inUser1); //RPS_START

        // Send RPS choice from user1
        outUser1.println(Utils.objectToMessage(new RPSChoiceReq("rock")));
        outUser1.flush();

        receiveLineWithTimeout(inUser1); //RPS_CHOICE_RESP

        // Send RPS choice from user1 again
        outUser1.println(Utils.objectToMessage(new RPSChoiceReq("rock")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSChoiceResp rpsChoiceResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsChoiceResp.status());
        assertEquals(10007, rpsChoiceResp.code());
    }

    @Test
    void tc50RpsInvalidChoiceReturnsError() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        receiveLineWithTimeout(inUser1);
        outUser1.flush();

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser1); //RPS_START

        // Send RPS choice from user1
        outUser1.println(Utils.objectToMessage(new RPSChoiceReq("invalidchoice")));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        RPSChoiceResp rpsChoiceResp = Utils.messageToObject(res);
        assertEquals("ERROR", rpsChoiceResp.status());
        assertEquals(10005, rpsChoiceResp.code());
    }

    @Test
    void tc51RpsGameUnexpectedOpponentDisconnectReturnsError() throws IOException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();

        // joined
        receiveLineWithTimeout(inUser1);

        // Send RPS start request from user1 to user2
        outUser1.println(Utils.objectToMessage(new RPSStartReq("user2")));
        outUser1.flush();

        receiveLineWithTimeout(inUser2); //RPS_START
        receiveLineWithTimeout(inUser1); //RPS_START

        // Disconnect user2
        socketUser2.close();

        String res = receiveLineWithTimeout(inUser1);
        RpsError rpsError = Utils.messageToObject(res);
        assertEquals(10009, rpsError.code());
    }

    @Test
    void tc60SuccessfulTransferRequestIsReceivedByRecipient() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("user2", "test.txt", 100, "test", null)));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser2);
        TransferReq transfer = Utils.messageToObject(res);
        assertEquals("user1", transfer.username());
        assertEquals("test.txt", transfer.filename());
        assertEquals(100, transfer.filesize());
    }

    @Test
    void tc61TransferRequestAcceptedByIdSuccessfullyAndRolesIdentified() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("user2", "test.txt", 100, "test", null)));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);

        String res = receiveLineWithTimeout(inUser2);
        TransferReq transfer = Utils.messageToObject(res);
        String id = transfer.sessionId();

        // Accept transfer request from user2
        outUser2.println(Utils.objectToMessage(new TransferAccept(id)));
        outUser2.flush();

        // Confirm transfer request accept resp
        String res2 = receiveLineWithTimeout(inUser2);
        TransferAcceptResp transferAcceptResp = Utils.messageToObject(res2);
        assertEquals("OK", transferAcceptResp.status());

        // CONFIRM TRASNFER_ACCEPTED with correct roles
        String res3 = receiveLineWithTimeout(inUser1);
        TransferAccepted transferAcceptedUserSender = Utils.messageToObject(res3);

        String res4 = receiveLineWithTimeout(inUser2);
        TransferAccepted transferAcceptedUserReceiver = Utils.messageToObject(res4);

        // ensure sender and receiver roles are correct
        assertEquals('s', transferAcceptedUserSender.uuid().charAt(transferAcceptedUserSender.uuid().length() - 1));
        assertEquals('r', transferAcceptedUserReceiver.uuid().charAt(transferAcceptedUserReceiver.uuid().length() - 1));

        // ensure session id is the same
        assertEquals(transferAcceptedUserSender.uuid().substring(0, transferAcceptedUserSender.uuid().length() - 1),
                transferAcceptedUserReceiver.uuid().substring(0, transferAcceptedUserReceiver.uuid().length() - 1));


        // ensure the file name is correct
        assertEquals("test.txt", transferAcceptedUserSender.filename());
        assertEquals("test.txt", transferAcceptedUserReceiver.filename());
    }

    @Test
    void tc62ConfirmErrorWhenEvilUserTriesToAcceptOtherTransfer() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("user2", "test.txt", 100, "test", null)));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);

        String res = receiveLineWithTimeout(inUser2);
        TransferReq transfer = Utils.messageToObject(res);
        String id = transfer.sessionId();

        // Evil user3 tries to accept transfer request
        outUser3.println(Utils.objectToMessage(new TransferAccept(id)));
        outUser3.flush();

        // Confirm transfer request accept resp
        String res2 = receiveLineWithTimeout(inUser3);
        TransferAcceptResp transferAcceptResp = Utils.messageToObject(res2);
        assertEquals("ERROR", transferAcceptResp.status());
        assertEquals(12001, transferAcceptResp.code()); // 12001 - didn't expect from user
    }

    @Test
    void tc63ConfirmErrorWhenEvilUserTriesToRejectTransfer() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("user2", "test.txt", 100, "test", null)));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);

        String res = receiveLineWithTimeout(inUser2);
        TransferReq transfer = Utils.messageToObject(res);
        String id = transfer.sessionId();

        // Evil user3 tries to reject transfer request
        outUser3.println(Utils.objectToMessage(new TransferReject(id)));
        outUser3.flush();

        // Confirm transfer request reject resp
        String res2 = receiveLineWithTimeout(inUser3);
        TransferRejectResp transferRejectResp = Utils.messageToObject(res2);
        assertEquals("ERROR", transferRejectResp.status());
        assertEquals(12001, transferRejectResp.code()); // 12001 - didn't expect from user
    }

    @Test
    void tc64ConfirmErrorResponseWhenTryingToSendFileTransferToNonExistentUser() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("nonexistentuser", "test.txt", 100, "test", null)));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        TransferResp transferResp = Utils.messageToObject(res);
        assertEquals("ERROR", transferResp.status());
        assertEquals(9001, transferResp.code()); // 9001 - user not found
    }

    @Test
    void tc65ConfirmErrorResponseWhenTransferRequestTimeout() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user2
        outUser1.println(Utils.objectToMessage(new TransferReq("user2", "test.txt", 100, "test", null)));
        outUser1.flush();
        receiveLineWithTimeout(inUser1);

        // Wait for timeout
        String res = receiveLineWithTimeout(inUser1, 10_500);
        while (res.contains("PING")) {
            res = receiveLineWithTimeout(inUser1, 10_500);
        }
        TransferResp transferResp = Utils.messageToObject(res);
        assertEquals("ERROR", transferResp.status());
        assertEquals(10008, transferResp.code()); // 10008 - timeout
    }

    @Test
    void tc66ConfirmErrorResponseWhenSendingTransferToSelf() throws JsonProcessingException {
        loginAndReceiveReady();

        // Send transfer request from user1 to user1
        outUser1.println(Utils.objectToMessage(new TransferReq("user1", "test.txt", 100, "test", null)));
        outUser1.flush();

        String res = receiveLineWithTimeout(inUser1);
        TransferResp transferResp = Utils.messageToObject(res);
        assertEquals("ERROR", transferResp.status());
        assertEquals(9002, transferResp.code()); // 9002 - cannot send to self
    }

    @Test
    public void loginAndReceiveReady() throws JsonProcessingException {
        receiveLineWithTimeout(inUser1); //ready message
        receiveLineWithTimeout(inUser2); //ready message
        receiveLineWithTimeout(inUser3); //ready message

        // Connect user1
        outUser1.println(Utils.objectToMessage(new Enter("user1")));
        outUser1.flush();
        receiveLineWithTimeout(inUser1); // ready

        // Connect user2
        outUser2.println(Utils.objectToMessage(new Enter("user2")));
        outUser2.flush();
        receiveLineWithTimeout(inUser2); // ready

        // Connect user3
        outUser3.println(Utils.objectToMessage(new Enter("user3")));
        outUser3.flush();
        receiveLineWithTimeout(inUser3); // ready

        // joined
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser1);
        receiveLineWithTimeout(inUser2);
    }

    private String receiveLineWithTimeout(BufferedReader reader) {
        return assertTimeoutPreemptively(ofMillis(MAX_DELTA_ALLOWED_MS), reader::readLine);
    }

    private String receiveLineWithTimeout(BufferedReader reader, int timeout) {
        return assertTimeoutPreemptively(ofMillis(timeout), reader::readLine);
    }
}