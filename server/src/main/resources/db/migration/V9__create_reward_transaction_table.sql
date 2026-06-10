CREATE TABLE reward_transaction
(
    id         VARCHAR(255)             NOT NULL,
    member_id  BIGINT                   NOT NULL,
    amount     BIGINT                   NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_reward_transaction PRIMARY KEY (id)
);