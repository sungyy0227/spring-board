DELETE FROM chat_room_invite
WHERE id NOT IN (
    SELECT latest_invite.id
    FROM (
        SELECT MAX(id) AS id
        FROM chat_room_invite
        GROUP BY room_id
    ) latest_invite
);

ALTER TABLE chat_room_invite
    ADD CONSTRAINT uk_chat_room_invite_room UNIQUE (room_id);
