-- SecondShelf Marketplace & Discovery schema
-- Run this file once in Supabase Dashboard > SQL Editor.
-- Safe to re-run: tables, indexes, trigger, bucket and policies are idempotent.

create extension if not exists pgcrypto;

create table if not exists public.listings (
    id uuid primary key default gen_random_uuid(),
    seller_id uuid not null references auth.users(id) on delete cascade,
    title text not null check (char_length(trim(title)) between 3 and 100),
    description text not null check (char_length(trim(description)) between 10 and 2000),
    price_cents integer not null check (price_cents > 0 and price_cents <= 100000000),
    category text not null check (
        category in ('BOOKS', 'ELECTRONICS', 'STATIONERY', 'CLOTHING', 'LAB_EQUIPMENT', 'FURNITURE', 'OTHER')
    ),
    condition text not null check (condition in ('LIKE_NEW', 'GOOD', 'FAIR')),
    location text not null check (char_length(trim(location)) between 2 and 120),
    status text not null default 'ACTIVE' check (status in ('ACTIVE', 'RESERVED', 'SOLD', 'ARCHIVED')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table if not exists public.listing_images (
    id uuid primary key default gen_random_uuid(),
    listing_id uuid not null references public.listings(id) on delete cascade,
    storage_path text not null unique,
    display_order integer not null default 0 check (display_order between 0 and 9),
    created_at timestamptz not null default now(),
    unique (listing_id, display_order)
);

create table if not exists public.favorites (
    user_id uuid not null references auth.users(id) on delete cascade,
    listing_id uuid not null references public.listings(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (user_id, listing_id)
);

create index if not exists listings_status_created_at_idx
    on public.listings (status, created_at desc);

create index if not exists listings_category_condition_idx
    on public.listings (category, condition);

create index if not exists listings_price_idx
    on public.listings (price_cents);

create index if not exists listings_seller_idx
    on public.listings (seller_id);

create index if not exists listing_images_listing_idx
    on public.listing_images (listing_id, display_order);

create index if not exists favorites_user_created_idx
    on public.favorites (user_id, created_at desc);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
security invoker
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

drop trigger if exists listings_set_updated_at on public.listings;
create trigger listings_set_updated_at
before update on public.listings
for each row execute function public.set_updated_at();

alter table public.listings enable row level security;
alter table public.listing_images enable row level security;
alter table public.favorites enable row level security;

drop policy if exists "Public can view active listings" on public.listings;
create policy "Public can view active listings"
on public.listings for select
using (status = 'ACTIVE' or seller_id = auth.uid());

drop policy if exists "Authenticated users can create own listings" on public.listings;
create policy "Authenticated users can create own listings"
on public.listings for insert
to authenticated
with check (seller_id = auth.uid());

drop policy if exists "Sellers can update own listings" on public.listings;
create policy "Sellers can update own listings"
on public.listings for update
to authenticated
using (seller_id = auth.uid())
with check (seller_id = auth.uid());

drop policy if exists "Sellers can delete own listings" on public.listings;
create policy "Sellers can delete own listings"
on public.listings for delete
to authenticated
using (seller_id = auth.uid());

drop policy if exists "Users can view images for visible listings" on public.listing_images;
create policy "Users can view images for visible listings"
on public.listing_images for select
using (
    exists (
        select 1
        from public.listings
        where listings.id = listing_images.listing_id
          and (listings.status = 'ACTIVE' or listings.seller_id = auth.uid())
    )
);

drop policy if exists "Sellers can add images to own listings" on public.listing_images;
create policy "Sellers can add images to own listings"
on public.listing_images for insert
to authenticated
with check (
    exists (
        select 1
        from public.listings
        where listings.id = listing_images.listing_id
          and listings.seller_id = auth.uid()
    )
);

drop policy if exists "Sellers can update own listing images" on public.listing_images;
create policy "Sellers can update own listing images"
on public.listing_images for update
to authenticated
using (
    exists (
        select 1
        from public.listings
        where listings.id = listing_images.listing_id
          and listings.seller_id = auth.uid()
    )
)
with check (
    exists (
        select 1
        from public.listings
        where listings.id = listing_images.listing_id
          and listings.seller_id = auth.uid()
    )
);

drop policy if exists "Sellers can delete own listing images" on public.listing_images;
create policy "Sellers can delete own listing images"
on public.listing_images for delete
to authenticated
using (
    exists (
        select 1
        from public.listings
        where listings.id = listing_images.listing_id
          and listings.seller_id = auth.uid()
    )
);

drop policy if exists "Users can view own favorites" on public.favorites;
create policy "Users can view own favorites"
on public.favorites for select
to authenticated
using (user_id = auth.uid());

drop policy if exists "Users can create own favorites" on public.favorites;
create policy "Users can create own favorites"
on public.favorites for insert
to authenticated
with check (user_id = auth.uid());

drop policy if exists "Users can delete own favorites" on public.favorites;
create policy "Users can delete own favorites"
on public.favorites for delete
to authenticated
using (user_id = auth.uid());

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'listing-images',
    'listing-images',
    true,
    5242880,
    array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists "Public can view listing image objects" on storage.objects;
create policy "Public can view listing image objects"
on storage.objects for select
using (bucket_id = 'listing-images');

drop policy if exists "Users can upload to own listing folder" on storage.objects;
create policy "Users can upload to own listing folder"
on storage.objects for insert
to authenticated
with check (
    bucket_id = 'listing-images'
    and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "Users can update own listing images" on storage.objects;
create policy "Users can update own listing images"
on storage.objects for update
to authenticated
using (
    bucket_id = 'listing-images'
    and owner_id = auth.uid()::text
)
with check (
    bucket_id = 'listing-images'
    and (storage.foldername(name))[1] = auth.uid()::text
);

drop policy if exists "Users can delete own listing images" on storage.objects;
create policy "Users can delete own listing images"
on storage.objects for delete
to authenticated
using (
    bucket_id = 'listing-images'
    and owner_id = auth.uid()::text
);
