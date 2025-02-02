package shared.messages.model.server;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RPSStartResp(String status, Integer code, List<String> users) {
    public RPSStartResp(String status, Integer code) {
        this(status, code, null);
    }
    public RPSStartResp(String status) {
        this(status, null, null);
    }
}
