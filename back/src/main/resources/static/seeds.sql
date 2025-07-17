INSERT INTO clients (
    id,
    email,
    username,
    password,
    created_at,
    updated_at
) VALUES (
    gen_random_uuid(),
    'client@gmail.com',
    'bob',
    '$2a$10$P9c/1dIk7JNdZIrLqJAZNu7k37zLpijg.I8EYQQgSl1Ci1QiXZH6y',
    NOW(),
    NOW()
);

INSERT INTO agents (
    id,
    secret,
    agent_data
) VALUES (
    '00000000-0000-0000-0000-000000000000',
    '$2a$10$qh1XaBIzj0R2YInD2PuytuvmTE4vFtJ1/xUgV.glKP.ajlOCkujLK',
    '{"name": "Marie"}'::jsonb
);
