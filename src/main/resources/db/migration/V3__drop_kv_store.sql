-- The KV store was the pre-tables persistence shim (plan.md §2) — holdings/tasks now live in
-- real tables (V2, and tasks already had one from the original Supabase boilerplate).
DROP TABLE IF EXISTS public.kv_store;
