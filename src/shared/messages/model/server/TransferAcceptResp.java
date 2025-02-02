package shared.messages.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferAcceptResp(String status, Integer code) {
}
