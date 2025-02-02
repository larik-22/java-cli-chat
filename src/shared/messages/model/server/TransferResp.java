package shared.messages.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferResp(String sessionId, String status, Integer code) {
    public TransferResp(String status, Integer code) {
        this(null, status, code);
    }
}
