ALTER TABLE chat_room_member
    ADD COLUMN role VARCHAR(20);

UPDATE chat_room_member
SET role = 'MEMBER';

INSERT INTO chat_room_member (room_id, member_id, role, joined_at)
SELECT chat_room.id, chat_room.owner_id, 'OWNER', chat_room.created_at
FROM chat_room
WHERE NOT EXISTS (
    SELECT 1
    FROM chat_room_member
    WHERE chat_room_member.room_id = chat_room.id
      AND chat_room_member.member_id = chat_room.owner_id
);

UPDATE chat_room_member
SET role = 'OWNER'
WHERE EXISTS (
    SELECT 1
    FROM chat_room
    WHERE chat_room.id = chat_room_member.room_id
      AND chat_room.owner_id = chat_room_member.member_id
);

ALTER TABLE chat_room_member
    ALTER COLUMN role SET NOT NULL;

ALTER TABLE chat_room
    DROP CONSTRAINT fk_chat_room_owner;

ALTER TABLE chat_room
    DROP COLUMN owner_id;
