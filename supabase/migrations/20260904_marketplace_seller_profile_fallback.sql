-- Backfill safe marketplace seller identity from Auth metadata when a profile row
-- has not yet been created. Only approved public display fields are returned.

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
    select distinct
        l.seller_id as id,
        coalesce(
            nullif(trim(p.name), ''),
            nullif(trim(u.raw_user_meta_data ->> 'full_name'), ''),
            nullif(trim(u.raw_user_meta_data ->> 'name'), ''),
            'Student'
        ) as name,
        coalesce(
            nullif(trim(p.campus), ''),
            nullif(trim(u.raw_user_meta_data ->> 'campus'), ''),
            ''
        ) as campus,
        coalesce(
            nullif(trim(p.avatar_url), ''),
            nullif(trim(u.raw_user_meta_data ->> 'avatar_url'), ''),
            nullif(trim(u.raw_user_meta_data ->> 'picture'), ''),
            ''
        ) as avatar_url
    from public.listings l
    left join public.profiles p on p.id = l.seller_id
    left join auth.users u on u.id = l.seller_id
    where l.status = 'ACTIVE';
$$;

revoke all on function public.get_marketplace_seller_profiles() from public;
grant execute on function public.get_marketplace_seller_profiles() to authenticated;
