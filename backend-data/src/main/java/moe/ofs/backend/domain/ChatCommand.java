package moe.ofs.backend.domain;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode
public class ChatCommand {
    @SerializedName("playerID")
    private int netId;  // net of the player who sent this message

    private double time;  // timestamp on message sent

    @SerializedName("msg")
    private String command;  // content of the command content

    @SerializedName("kw")
    private String keyword;

    private transient PlayerInfo player;
}
