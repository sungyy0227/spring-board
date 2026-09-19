DELETE older_invite
FROM chat_room_invite older_invite
JOIN chat_room_invite newer_invite
  ON newer_invite.room_id = older_invite.room_id
 AND newer_invite.id > older_invite.id;

ALTER TABLE chat_room_invite
    ADD CONSTRAINT uk_chat_room_invite_room UNIQUE (room_id);
