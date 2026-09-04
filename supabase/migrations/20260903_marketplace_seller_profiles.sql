-- Safe public marketplace seller identity lookup.
-- Returns only display information needed by Marketplace; no email or auth data.

create or replace function public.get_marketplace_seller_profiles()
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
        p.id,
        coalesce(nullif(trim(p.name), ''), 'Student') as name,
        coalesce(p.campus, '') as campus,
        coalesce(p.avatar_url, '') as avatar_url
    from public.profiles p
    where exists (
        select 1
        from public.listings l
        where l.seller_id = p.id
          and l.status = 'ACTIVE'
    );
$$;

revoke all on function public.get_marketplace_seller_profiles() from public;
grant execute on function public.get_marketplace_seller_profiles() to authenticated;
