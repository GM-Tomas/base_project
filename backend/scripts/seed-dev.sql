-- Sample holdings/platforms for exercising the dashboard locally against a real Postgres.
-- NOT a Flyway migration (backend/src/main/resources/db/migration) and never will be — it must
-- stay out of that folder so a real deploy never seeds a new user's account (CA-04.4: "Un usuario
-- nuevo arranca vacío"). Run by hand, once, against your own dev database:
--
--   psql "$SUPABASE_DB_URL" -v dev_user_id="'<your-auth.users.id>'" -f backend/scripts/seed-dev.sql
--
-- <your-auth.users.id> is the UUID of a real row in auth.users (e.g. the account you log in with
-- locally) — holdings.user_id/platforms.user_id both FK there, so an arbitrary UUID will fail.
-- Safe to re-run: platforms are keyed by (user_id, name), holdings are re-inserted fresh each time.

begin;

insert into platforms (user_id, name, type) values
  (:dev_user_id, 'Balanz', 'Broker'),
  (:dev_user_id, 'Mercado Pago', 'Wallet'),
  (:dev_user_id, 'Banco Galicia', 'Bank'),
  (:dev_user_id, 'Nexo', 'Exchange'),
  (:dev_user_id, 'Binance', 'Exchange')
on conflict (user_id, name) do nothing;

delete from holdings where user_id = :dev_user_id;

insert into holdings (id, user_id, name, asset_class, platform_name, value_usd) values
  (gen_random_uuid(), :dev_user_id, 'Cuenta remunerada ARS', 'Cash',         'Mercado Pago',   6150.00),
  (gen_random_uuid(), :dev_user_id, 'Plazo fijo UVA 90d',    'Fixed Income', 'Banco Galicia', 11300.00),
  (gen_random_uuid(), :dev_user_id, 'AL30D Bonar 2030',      'Fixed Income', 'Balanz',         9100.00),
  (gen_random_uuid(), :dev_user_id, 'S&P 500 Index Fund',    'Index Fund',   'Balanz',         5100.00),
  (gen_random_uuid(), :dev_user_id, 'AAPL',                  'Equity',       'Balanz',         7643.00),
  (gen_random_uuid(), :dev_user_id, 'NVDA',                  'Equity',       'Balanz',        10557.00),
  (gen_random_uuid(), :dev_user_id, 'Bitcoin',                'Crypto',       'Nexo',           9200.00),
  (gen_random_uuid(), :dev_user_id, 'Ethereum',               'Crypto',       'Nexo',           5580.00),
  (gen_random_uuid(), :dev_user_id, 'Bitcoin',                'Crypto',       'Binance',       13616.00),
  (gen_random_uuid(), :dev_user_id, 'USDT (stable)',          'Cash',         'Binance',        6004.00);

commit;
