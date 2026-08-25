-- Supports JdbcTaskRepository's findAll(userId) ORDER BY created_at DESC (tasks table already
-- exists with RLS — see supabase/schema.sql; this only adds the index the new query needs).
CREATE INDEX IF NOT EXISTS tasks_user_created_idx ON public.tasks (user_id, created_at DESC);
