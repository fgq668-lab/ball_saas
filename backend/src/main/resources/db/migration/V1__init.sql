create table venue (
    id bigserial primary key,
    tenant_id bigint,
    name varchar(120) not null,
    sport_types varchar(200) not null,
    address varchar(300) not null,
    longitude numeric(12, 8),
    latitude numeric(12, 8),
    contact_name varchar(60) not null,
    contact_phone varchar(30) not null,
    status varchar(32) not null,
    wx_sub_mch_id varchar(64),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table court (
    id bigserial primary key,
    venue_id bigint not null,
    name varchar(80) not null,
    sport_type varchar(32) not null,
    indoor boolean not null,
    status varchar(32) not null,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create index idx_court_venue on court(venue_id);

create table court_price_rule (
    id bigserial primary key,
    venue_id bigint not null,
    court_id bigint not null,
    day_type varchar(32) not null,
    start_time time not null,
    end_time time not null,
    price_cent integer not null,
    effective_start_date date,
    effective_end_date date,
    priority integer not null default 0,
    enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create index idx_price_rule_court on court_price_rule(venue_id, court_id, enabled, priority desc);

create table booking_order (
    id bigserial primary key,
    order_no varchar(40) not null unique,
    venue_id bigint not null,
    court_id bigint not null,
    user_id bigint not null,
    start_at timestamptz not null,
    end_at timestamptz not null,
    amount_cent integer not null,
    payable_cent integer not null,
    status varchar(32) not null,
    lock_token varchar(80) not null,
    paid_at timestamptz,
    cancelled_at timestamptz,
    checked_in_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create index idx_booking_venue_court_time on booking_order(venue_id, court_id, start_at, end_at);

create table court_time_slot (
    id bigserial primary key,
    venue_id bigint not null,
    court_id bigint not null,
    slot_start_at timestamptz not null,
    slot_end_at timestamptz not null,
    booking_order_id bigint not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0,
    constraint uk_court_time_slot unique (venue_id, court_id, slot_start_at)
);

create table payment_order (
    id bigserial primary key,
    payment_no varchar(40) not null unique,
    business_type varchar(32) not null,
    business_id bigint not null,
    venue_id bigint not null,
    user_id bigint not null,
    amount_cent integer not null,
    channel varchar(32) not null,
    wx_prepay_id varchar(120),
    wx_transaction_id varchar(120),
    status varchar(32) not null,
    paid_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table payment_notify_log (
    id bigserial primary key,
    channel varchar(32) not null,
    notify_type varchar(40) not null,
    out_trade_no varchar(80),
    transaction_id varchar(120),
    raw_body text,
    headers text,
    process_status varchar(32) not null,
    error_message varchar(500),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table refund_order (
    id bigserial primary key,
    refund_no varchar(40) not null unique,
    payment_order_id bigint not null,
    venue_id bigint not null,
    amount_cent integer not null,
    reason varchar(200) not null,
    wx_refund_id varchar(120),
    status varchar(32) not null,
    requested_at timestamptz,
    refunded_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table match_room (
    id bigserial primary key,
    match_no varchar(40) not null unique,
    booking_order_id bigint not null,
    venue_id bigint not null,
    court_id bigint not null,
    creator_user_id bigint not null,
    start_at timestamptz not null,
    end_at timestamptz not null,
    min_players integer not null,
    max_players integer not null,
    current_players integer not null,
    pay_mode varchar(32) not null,
    amount_cent integer not null,
    per_user_amount_cent integer not null,
    status varchar(32) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table match_player (
    id bigserial primary key,
    match_room_id bigint not null,
    user_id bigint not null,
    status varchar(32) not null,
    payment_order_id bigint,
    joined_at timestamptz,
    left_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0,
    constraint uk_match_player_user unique (match_room_id, user_id)
);

create table app_user (
    id bigserial primary key,
    openid varchar(120),
    phone varchar(30),
    nickname varchar(80),
    status varchar(32) not null default 'ENABLED',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create table admin_user (
    id bigserial primary key,
    username varchar(80) not null unique,
    password_hash varchar(200) not null,
    display_name varchar(80) not null,
    venue_id bigint,
    status varchar(32) not null default 'ENABLED',
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

