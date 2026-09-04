DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'moneyapp') THEN
        CREATE ROLE moneyapp LOGIN PASSWORD 'moneyapp';
    ELSE
        ALTER ROLE moneyapp WITH LOGIN PASSWORD 'moneyapp';
    END IF;
END
$$;

SELECT 'CREATE DATABASE moneyapp OWNER moneyapp'
WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'moneyapp')
\gexec

ALTER DATABASE moneyapp OWNER TO moneyapp;
