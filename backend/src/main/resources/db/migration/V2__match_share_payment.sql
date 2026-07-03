create table match_share_payment (
    id bigserial primary key,
    match_room_id bigint not null,
    user_id bigint not null,
    payment_order_id bigint,
    amount_cent integer not null,
    status varchar(32) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0,
    constraint uk_match_share_payment_user unique (match_room_id, user_id)
);

create table match_share_refund (
    id bigserial primary key,
    match_room_id bigint not null,
    user_id bigint not null,
    refund_order_id bigint,
    amount_cent integer not null,
    status varchar(32) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

