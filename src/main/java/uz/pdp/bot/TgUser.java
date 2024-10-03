package uz.pdp.bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class TgUser {
    private Long chatId;
    private String firstName;
    private String lastName;
    private TgState state = TgState.START;
    private Integer lastSentMessageId;
    private boolean stopPrinting;
}
