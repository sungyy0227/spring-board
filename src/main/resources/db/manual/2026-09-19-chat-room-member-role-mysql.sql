ALTER TABLE chat_room_member
    ADD COLUMN role VARCHAR(20) NULL;

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
JOIN chat_room
  ON chat_room.id = chat_room_member.room_id
 AND chat_room.owner_id = chat_room_member.member_id
SET chat_room_member.role = 'OWNER';

ALTER TABLE chat_room_member
    MODIFY COLUMN role VARCHAR(20) NOT NULL;

ALTER TABLE chat_room
    DROP FOREIGN KEY fk_chat_room_owner;

ALTER TABLE chat_room
    DROP COLUMN owner_id;
