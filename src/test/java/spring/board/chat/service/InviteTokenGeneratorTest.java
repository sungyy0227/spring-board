package spring.board.chat.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InviteTokenGeneratorTest {

    @Test
    void generateReturnsTwelveCharacterUrlSafeToken() {
        InviteTokenGenerator generator = new InviteTokenGenerator();

        String token = generator.generate();

        assertEquals(12, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]{12}"));
    }
}
