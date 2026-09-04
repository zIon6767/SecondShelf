-- Explicit Data API privileges for the Marketplace & Discovery module.
-- Run after 20260902_marketplace.sql.

grant usage on schema public to anon, authenticated;

grant select on table public.listings to anon;
grant select on table public.listing_images to anon;

grant select, insert, update, delete
on table public.listings
to authenticated;

grant select, insert, update, delete
on table public.listing_images
to authenticated;

grant select, insert, delete
on table public.favorites
to authenticated;

grant all on table public.listings to service_role;
grant all on table public.listing_images to service_role;
grant all on table public.favorites to service_role;
