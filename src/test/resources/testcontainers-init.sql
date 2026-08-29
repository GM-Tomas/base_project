-- Runs once, right after the Testcontainers Postgres starts and before Flyway (V2's FKs target
-- auth.users, V4's index targets public.tasks — both real in Supabase, created there by
-- supabase/schema.sql outside Flyway, so a plain postgres:16-alpine image has neither).
-- Minimal stand-ins, not a copy of Supabase's real auth/tasks schema: just enough shape for the
-- FKs and the index Flyway's own migrations need to apply cleanly.
create schema if not exists auth;

create table if not exists auth.users (
    id uuid primary key default gen_random_uuid()
);

-- V2's RLS policies (USING/WITH CHECK) reference this real Supabase function to define the DDL —
-- the app itself never calls it (it connects with a privileged role that RLS doesn't apply to,
-- per data-model.md D5), so a stub that never resolves to a real session is fine here.
create or replace function auth.uid() returns uuid
    language sql stable
    as $$ select null::uuid $$;

create table if not exists public.tasks (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    title text not null,
    completed boolean not null default false,
    created_at timestamptz not null default now()
);
