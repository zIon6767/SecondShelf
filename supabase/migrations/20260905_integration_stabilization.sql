-- Stabilize meetup handoff transactions and participant identity lookup.
-- Run once in Supabase SQL Editor before testing the QR flow.

create table if not exists public.transactions (
    id uuid primary key default gen_random_uuid(),
    listing_id uuid not null references public.listings(id) on delete cascade,
    buyer_id uuid not null references auth.users(id) on delete cascade,
    seller_id uuid not null references auth.users(id) on delete cascade,
    qr_token text not null,
    status text not null default 'pending'
        check (status in ('pending', 'completed', 'cancelled')),
    created_at timestamptz not null default now(),
    completed_at timestamptz
);

alter table public.transactions
    add column if not exists completed_at timestamptz;

create unique index if not exists transactions_qr_token_idx
    on public.transactions (qr_token);

create index if not exists transactions_participants_idx
    on public.transactions (seller_id, buyer_id, status);

alter table public.transactions enable row level security;

drop policy if exists "Handoff participants can view transactions" on public.transactions;
create policy "Handoff participants can view transactions"
on public.transactions for select
to authenticated
using (seller_id = auth.uid() or buyer_id = auth.uid());

drop policy if exists "Sellers can create handoffs" on public.transactions;
create policy "Sellers can create handoffs"
on public.transactions for insert
to authenticated
with check (
    seller_id = auth.uid()
    and buyer_id <> auth.uid()
    and status = 'pending'
    and exists (
        select 1
        from public.listings l
        where l.id = listing_id
          and l.seller_id = auth.uid()
          and l.status = 'ACTIVE'
    )
);

drop policy if exists "Assigned buyers can complete handoffs" on public.transactions;
create policy "Assigned buyers can complete handoffs"
on public.transactions for update
to authenticated
using (
    buyer_id = auth.uid()
    and seller_id <> auth.uid()
    and status = 'pending'
)
with check (
    buyer_id = auth.uid()
    and seller_id <> auth.uid()
    and status = 'completed'
);

drop policy if exists "Sellers can delete handoffs" on public.transactions;
create policy "Sellers can delete handoffs"
on public.transactions for delete
to authenticated
using (seller_id = auth.uid());

create or replace function public.set_transaction_completed_at()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
    if new.status = 'completed' and old.status is distinct from 'completed' then
        new.completed_at = now();
    end if;
    return new;
end;
$$;

drop trigger if exists transactions_set_completed_at on public.transactions;
create trigger transactions_set_completed_at
before update on public.transactions
for each row execute function public.set_transaction_completed_at();

-- Returns only safe display fields, and only for the signed-in user or somebody
-- with whom they share a message thread.
create or replace function public.get_chat_participant_profile(p_user_id uuid)
returns table (
    id uuid,
    name text,
    campus text,
    avatar_url text
)
language sql
stable
security definer
set search_path = ''
as $$
    select
        p_user_id,
        coalesce(
            nullif(trim(p.name), ''),
            nullif(trim(u.raw_user_meta_data ->> 'full_name'), ''),
            nullif(trim(u.raw_user_meta_data ->> 'name'), ''),
            'Student'
        ),
        coalesce(
            nullif(trim(p.campus), ''),
            nullif(trim(u.raw_user_meta_data ->> 'campus'), ''),
            ''
        ),
        coalesce(
            nullif(trim(p.avatar_url), ''),
            nullif(trim(u.raw_user_meta_data ->> 'avatar_url'), ''),
            nullif(trim(u.raw_user_meta_data ->> 'picture'), ''),
            ''
        )
    from auth.users u
    left join public.profiles p on p.id = u.id
    where u.id = p_user_id
      and auth.uid() is not null
      and (
          p_user_id = auth.uid()
          or exists (
              select 1
              from public.messages m
              where (m.sender_id = auth.uid() and m.receiver_id = p_user_id)
                 or (m.receiver_id = auth.uid() and m.sender_id = p_user_id)
          )
      );
$$;

revoke all on function public.get_chat_participant_profile(uuid) from public;
grant execute on function public.get_chat_participant_profile(uuid) to authenticated;
