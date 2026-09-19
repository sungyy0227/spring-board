package spring.board.chat.migration;

import org.h2.tools.RunScript;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatRoomInviteUniqueMigrationTest {

    @Test
    void keepsLatestInviteAndAddsRoomUniqueConstraint() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:h2:mem:chat-invite-migration;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        )) {
            createLegacySchema(connection);

            var migration = getClass().getResourceAsStream(
                    "/db/manual/2026-09-19-chat-room-invite-room-unique-h2.sql"
            );
            assertNotNull(migration);
            RunScript.execute(
                    connection,
                    new InputStreamReader(migration, StandardCharsets.UTF_8)
            );

            try (Statement statement = connection.createStatement()) {
                try (ResultSet invites = statement.executeQuery("""
                        SELECT id, token
                        FROM chat_room_invite
                        WHERE room_id = 10
                        """)) {
                    invites.next();
                    assertEquals(2L, invites.getLong("id"));
                    assertEquals("newToken002", invites.getString("token"));
                    assertEquals(false, invites.next());
                }

                assertThrows(SQLException.class, () -> statement.execute("""
                        INSERT INTO chat_room_invite (
                            id, room_id, created_by_id, token, created_at, expires_at
                        ) VALUES (
                            3, 10, 1, 'thirdToken03',
                            TIMESTAMP '2026-09-19 12:00:00',
                            TIMESTAMP '2026-09-26 12:00:00'
                        )
                        """));
            }
        }
    }

    private void createLegacySchema(Connection connection) throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE chat_room_invite (
                        id BIGINT PRIMARY KEY,
                        room_id BIGINT NOT NULL,
                        created_by_id BIGINT NOT NULL,
                        token VARCHAR(12) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        expires_at TIMESTAMP NOT NULL,
                        CONSTRAINT uk_chat_room_invite_token UNIQUE (token)
                    )
                    """);
            statement.execute("""
                    INSERT INTO chat_room_invite (
                        id, room_id, created_by_id, token, created_at, expires_at
                    ) VALUES
                        (
                            1, 10, 1, 'oldToken001_',
                            TIMESTAMP '2026-09-19 10:00:00',
                            TIMESTAMP '2026-09-26 10:00:00'
                        ),
                        (
                            2, 10, 1, 'newToken002',
                            TIMESTAMP '2026-09-19 11:00:00',
                            TIMESTAMP '2026-09-26 11:00:00'
                        )
                    """);
        }
    }
}
