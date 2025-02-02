package shared.messages.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransferRejectResp(String status, Integer code) {
}
