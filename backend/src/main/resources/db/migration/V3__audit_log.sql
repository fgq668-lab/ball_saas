create table audit_log (
    id bigserial primary key,
    action varchar(80) not null,
    object_type varchar(80) not null,
    object_id bigint,
    operator_user_id bigint,
    operator_role varchar(40),
    venue_id bigint,
    summary varchar(500),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted boolean not null default false,
    version bigint not null default 0
);

create index idx_audit_log_venue_created on audit_log(venue_id, created_at desc);
create index idx_audit_log_object on audit_log(object_type, object_id);
