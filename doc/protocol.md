# Protocol description

This client-server protocol describes the following scenarios:
- Setting up a connection between client and server.
- Broadcasting a message to all connected clients.
- Periodically sending heartbeat to connected clients.
- Disconnection from the server.
- Handling invalid messages.

In the description below, `C -> S` represents a message from the client `C` is send to server `S`. When applicable, `C` is extended with a number to indicate a specific client, e.g., `C1`, `C2`, etc. The keyword `others` is used to indicate all other clients except for the client who made the request. Messages can contain a JSON body. Text shown between `<` and `>` are placeholders.

The protocol follows the formal JSON specification, RFC 8259, available on https://www.rfc-editor.org/rfc/rfc8259.html

# 1. Establishing a connection

The client first sets up a socket connection to which the server responds with a welcome message. The client supplies a username on which the server responds with an OK if the username is accepted or an ERROR with a number in case of an error.
_Note:_ A username may only consist of characters, numbers, and underscores ('_') and has a length between 3 and 14 characters.

## 1.1 Happy flow

Client sets up the connection with server.
```
S -> C: READY {"version": "<server version number>"}
```
- `<server version number>`: the semantic version number of the server.

After a while when the client logs the user in:
```
C -> S: ENTER {"username":"<username>"}
S -> C: ENTER_RESP {"status":"OK"}
```

- `<username>`: the username of the user that needs to be logged in.
      To other clients (Only applicable when working on Level 2):
```
S -> others: JOINED {"username":"<username>"}
```

## 1.2 Unhappy flow
```
S -> C: ENTER_RESP {"status":"ERROR", "code":<error code>}
```      
Possible `<error code>`:

| Error code | Description                              |
|------------|------------------------------------------|
| 5000       | User with this name already exists       |
| 5001       | Username has an invalid format or length |      
| 5002       | Already logged in                        |

# 2. Broadcast message

Sends a message from a client to all other clients. The sending client does not receive the message itself but gets a confirmation that the message has been sent.

## 2.1 Happy flow

```
C -> S: BROADCAST_REQ {"message":"<message>"}
S -> C: BROADCAST_RESP {"status":"OK"}
```
- `<message>`: the message that must be sent.

Other clients receive the message as follows:
```
S -> others: BROADCAST {"username":"<username>","message":"<message>"}   
```   
- `<username>`: the username of the user that is sending the message.

## 2.2 Unhappy flow

```
S -> C: BROADCAST_RESP {"status": "ERROR", "code": <error code>}
```
Possible `<error code>`:

| Error code | Description            |
|------------|------------------------|
| 6000       | User is not logged in  |

# 3. Heartbeat message

Sends a ping message to the client to check whether the client is still active. The receiving client should respond with a pong message to confirm it is still active. If after 3 seconds no pong message has been received by the server, the connection to the client is closed. Before closing, the client is notified with a HANGUP message, with reason code 7000.

The server sends a ping message to a client every 10 seconds. The first ping message is send to the client 10 seconds after the client is logged in.

When the server receives a PONG message while it is not expecting one, a PONG_ERROR message will be returned.

## 3.1 Happy flow

```
S -> C: PING
C -> S: PONG
```     

## 3.2 Unhappy flow

```
S -> C: HANGUP {"reason": <reason code>}
[Server disconnects the client]
```      
Possible `<reason code>`:

| Reason code | Description      |
|-------------|------------------|
| 7000        | No pong received |    

```
S -> C: PONG_ERROR {"code": <error code>}
```
Possible `<error code>`:

| Error code | Description         |
|------------|---------------------|
| 8000       | Pong without ping   |    

# 4. Termination of the connection

When the connection needs to be terminated, the client sends a bye message. This will be answered (with a BYE_RESP message) after which the server will close the socket connection.

## 4.1 Happy flow
```
C -> S: BYE
S -> C: BYE_RESP {"status":"OK"}
[Server closes the socket connection]
```

Other, still connected clients, clients receive:
```
S -> others: LEFT {"username":"<username>"}
```

## 4.2 Unhappy flow

- None

# 5. Invalid message header

If the client sends an invalid message header (not defined above), the server replies with an unknown command message. The client remains connected.

Example:
```
C -> S: MSG This is an invalid message
S -> C: UNKNOWN_COMMAND
```

# 6. Invalid message body

If the client sends a valid message, but the body is not valid JSON, the server replies with a pars error message. The client remains connected.

Example:
```
C -> S: BROADCAST_REQ {"aaaa}
S -> C: PARSE_ERROR
```

# 7. Number of connected clients
The server keeps track of the number of connected clients. The server can send a list of all connected, logged in clients as per request. The server responds with a list of all connected clients.

## 7.1 Happy flow
Only applicable, if client that sends the request is logged in.
```
C -> S: CLIENTS_REQ
S -> C: CLIENTS {"clients":["<username1>", "<username2>", ...]}
```

- `<username1>`, `<username2>`, ...: usernames of all connected clients.

## 7.2 Unhappy flow
If the client that sends the request is not logged in, the server responds with an error message.
```
S -> C: CLIENTS_RESP {"status": "ERROR", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description            |
|------------|------------------------|
| 6000       | User is not logged in  |

# 8. Private messaging

A client sends a private message to another client. The server, then, forwards the message to the receiving client. A client receives a confirmation message when the message is sent successfully, or an error message, containing respective error code, when the message cannot be sent.

## 8.1 Happy flow

```
C1 -> S: PRIVATE_REQ {"to":"<username>", "message":"<message>"}
S -> C1: PRIVATE_RESP {"status":"OK"}
```
- `<username>`: username of the receiver
- `<message>`: message to be sent

The receiving client receives the message as follows:
```
S -> C2: PRIVATE {"from":"<username>", "message":"<message>"}
```
- `<username>`: username of the sender
- `<message>`: received message

## 8.2 Unhappy flow
A client sends a private message to another client that is not connected. The server responds with an error message.
```
S -> C: PRIVATE_RESP {"status": "ERROR", "code": <error code>}
```
Possible `<error code>`:

| Error code | Description                         |
|------------|-------------------------------------|
| 6000       | Sender is not logged in             |
| 9001       | Receiver does not exist             |
| 9002       | You cannot send message to yourself |


# 9. Rock Paper Scissors
A user selects an opponent and starts the game. The server responds with an OK message if the game is started successfully. If the game start fails, the server responds with an error message, containing specific error code.
When the game is started, each user makes a choice (rock, paper, or scissors). The server responds with an OK message if the choice is made successfully. If the choice fails, the server responds with an error message, containing specific error code.
When both users made a choice, the server determines the winner and sends the outcome of the game to both users.

## 9.1 Start game

### 9.1.1 Start game happy flow

User `A` starts the game 
```
C -> S: RPS_START_REQ {"username":"<username>"}
S -> C: RPS_START_RESP {"status":"OK"}
```

User `B` receives the message that the game will start
```
S -> C: RPS_START {"username":"<username>"}
```

- `<username>`: username of the opponent;

### 9.1.1 Start game unhappy flow

User `A` starts the game with an invalid username
```
C -> S: RPS_START_REQ {"username":"<username>"}
S -> C: RPS_START_RESP {"status":"ERROR", "code": <error code>}
```

User `A` starts the game when the game is already started
```
C -> S: RPS_START_REQ {"username":"<username>"}
S -> C: RPS_START_RESP {"status":"ERROR", "code": <error code>, "players": ["<username1>", "<username2>"]}
```

- `<username>`: username of the opponent;
- `<error code>`: error code;
- `<username1>`, `<username2>`: usernames of the players;

Possible `<error code>`:

| Error code | Description                      |
|------------|----------------------------------|
| 6000       | You are not logged in            |
| 9001       | Invalid user                     |
| 10001      | You cannot play against yourself |
| 10002      | Two users already playing        |

## 9.2 Make a choice

### 9.2.1 Make a choice happy flow

User `A` makes a choice (rock, paper or scissors)
```
C -> S: RPS_CHOICE_REQ {"choice":"<choice>"}
S -> C: RPS_CHOICE_RESP {"status":"OK"}
```
- `<choice>`: choice of the user;

### 9.2.2 Make a choice unhappy flow

User `A` makes a choice (rock, paper or scissors) with an invalid choice, when the game is not started, when the client is not playing a match, or when the client already made a choice.
```
C -> S: RPS_CHOICE_REQ {"choice":"<choice>"}
S -> C: RPS_CHOICE_RESP {"status":"ERROR", "code": <error code>}
```

- `<choice>`: choice of the user;
- `<error code>`: error code;

Possible `<error code>`:

| Error code | Description                              |
|------------|------------------------------------------|
| 10003      | Game not started                         |
| 10005      | Invalid message                          |
| 10007      | You already made a choice                |
| 10008      | Response timeout                         |
| 12001      | Unexpectedly sent message (not in match) |

## 9.3 End game

### 9.3.1 End game happy flow

The server sends the outcome of the game to both users. Winner = null if tie
```
S -> C: RPS_END {"winner":"<winner>", "opponent_choice":"<opponent_choice>"}
```

- `<winner>`: username of the winner; (or null if tie)
- `<opponent_choice>`: choice of the opponent;

### 9.3.2 End game unhappy flow
- none

### 9.3.3 End game tie 
If the rps ends in a tie, the server sends the outcome of the game to both users. Winner = null if tie
```
S -> C: RPS_END {"winner":null, "opponent_choice":"<opponent_choice>"}
```

## 9.4 Game error
If one of the users disconnects during the game, the server sends a game error message to the other user.
```
S -> C: RPS_ERROR {"code":"<error>"}
```
  Possible `<error code>`:

| Error code | Description                 |
|------------|-----------------------------|
| 10009      | Lost connection to opponent |


# 10. File transfer
A logged in client requests to send a file to another client. The receiver needs to accept the transfer first. 
After receiver accepts the transfer and file is sent, the integrity of the file is checked by comparing the checksums. If the checksums are equal, the transfer is successful.

## 10.1 File transfer request

The sender requests to send a file to the receiver.
```
C1 -> S: TRANSFER_REQ {"username":"<receiver>", "filename":"<filename>", "size":<size>, "checksum":"<checksum>"}
S -> C2: TRANSFER_REQ {"username":"<sender>", "filename":"<filename>", "size":<size>, "checksum":"<checksum>", "sessionId":"<uuid>"}
```
- `<username>`: username of the receiver or sender;
- `<filename>`: name of the file;
- `<size>`: size of the file in bytes;
- `<checksum>`: checksum of the file;
- `<uuid>`: UUID of the pending file transfer;

The receiver can either accept or reject the transfer request.

### 10.1.1 Happy flow
Client `C1` receives the response from the server with the status of the request.
```
S -> C1: TRANSFER_RESP {"status":"OK"}
```

### 10.1.2 Unhappy flow
Client `C1` receives the response from the server with the status of the request
```
S -> C1: TRANSFER_RESP {"status":"ERROR", "code": <error code>}
```
Possible `<error code>`:

| Error code | Description                 |
|------------|-----------------------------|
| 6000       | You are not logged in       |
| 9001       | Invalid user                |
| 9002       | Cannot send request to self |
| 10008      | Response timeout            |


## 10.2 File transfer response (acceptance / rejection)
When Client `C2` receives the transfer request, it can either accept or reject the transfer.

### 10.2.1 Accept transfer happy flow
Client `C2` accepts the transfer request. 
```
C2 -> S: TRANSFER_ACCEPT {"id": "<uuid>"}
S -> C2: TRANSFER_ACCEPT_RESP {"status":"OK"}
```
- `<uuid>`: UUID of the file transfer;

Server informs both clients about file transfer start.
```
S -> C1: TRANSFER_ACCEPTED {"username":"<receiver>", "filename":"<filename>","uuid":"<uuid>"}
S -> C2: TRANSFER_ACCEPTED {"username":"<sender>", "filename":"<filename>","uuid":"<uuid>"}
```

Both clients open a new socket connection to the server after receiving the message. 
After connecting, each client sends the UUID with "S" or "R" appended to it, to indicate sender or receiver, respectively.

```
C1 -> S: <uuid>S
C2 -> S: <uuid>R
```
- `<uuid>`: UUID of the file transfer;

Once sender is connected, it can start sending the file bytes regardless of the receiver's connection status.
After receiver is connected, server starts forwarding the file bytes to the receiver.

```
C1 -> S: <file bytes>
S -> C2: <file bytes>
```
- `<file bytes>`: bytes of the file;

### 10.2.2 Accept transfer unhappy flow
If user sends an accept message unexpectedly, the server responds with an error message
```
C2 -> S: TRANSFER_ACCEPT {"id": "<uuid>"}
S -> C2: TRANSFER_ACCEPT_RESP {"status":"ERROR", "code": <error code>}
```
- `<uuid>`: UUID of the file transfer;


Possible `<error code>`:

| Error code | Description               |
|------------|---------------------------|
| 12001      | Unexpected accept message |

### 10.2.3 Reject transfer happy flow
Client `C2` rejects the transfer request
```
C2 -> S: TRANSFER_REJECT {"id": "<uuid>"}
S -> C2: TRANSFER_REJECT_RESP {"status":"OK"}
```
- `<uuid>`: UUID of the file transfer;

```
S -> C1: TRANSFER_REJECTED {"username":"<receiver>"}
```
- `<username>`: username of the receiver, that rejected the transfer;

### 10.2.4 Reject transfer unhappy flow
If user sends a reject message unexpectedly, the server responds with an error message.
```
C2 -> S: TRANSFER_REJECT {"id": "<uuid>"}
S -> C2: TRANSFER_REJECT_RESP {"status":"ERROR", "code": <error code>}
```
Possible `<error code>`:

| Error code | Description               |
|------------|---------------------------|
| 12001      | Unexpected reject message |


## 10.3 File transfer completion
After the file is sent, receiver calculates the checksum of the received file and compares it with the checksum sent by the sender.
If the checksums are equal, the transfer is successful. Otherwise, the transfer is unsuccessful.


### 10.3.1 Successful transfer
If the checksums are equal, the server informs both clients about the successful transfer.
```
C2 -> S: TRANSFER_CHECKSUM {sessionUuid: "<uuid>", "checksum":"<checksum>"}
S -> C1: TRANSFER_SUCCESS {"id": "<uuid>", "filename":"<filename>"}
S -> C2: TRANSFER_SUCCESS {"id": "<uuid>", "filename":"<filename>"}
```

- `<checksum>`: checksum of the received file;
- `<uuid>`: UUID of the file transfer;
- `<filename>`: name of the file;


### 10.3.2 Unsuccessful transfer

If the checksums are not equal, the transfer is unsuccessful. Receiver deletes the file locally.
```
C2 -> S: TRANSFER_CHECKSUM {"checksum":"<checksum>", session: "<uuid>"}
S -> C1: TRANSFER_FAILED {"filename":"<filename>", "code": <error code>}
```

Possible `<error code>`:

| Error code | Description             |
|------------|-------------------------|
| 12002      | Checksums are not equal |
| 10009      | Lost connection to user |

