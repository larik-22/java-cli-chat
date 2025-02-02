package shared.messages.model.client;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferReq(String username, String filename, double filesize, String checksum, String sessionId) {
}
