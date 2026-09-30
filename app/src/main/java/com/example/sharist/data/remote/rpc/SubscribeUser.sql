CREATE OR REPLACE FUNCTION public.subscribe_to_ride(
    p_subscription_id uuid,
    p_user_id      uuid,
    p_ride_id      uuid,
    p_only_in_rain boolean,
    p_reoccurrence boolean default false
)
RETURNS text
LANGUAGE plpgsql
AS $function$
begin

    if not exists (select 1 from ride where id = p_ride_id) then
        return 'RIDE_NOT_FOUND';
    end if;

    if not exists (
        select 1 from ride
        where id = p_ride_id and "availableSeats" > 0
    ) then
        return 'NO_SEATS';
    end if;

    if exists (
        select 1 from subscription
        where "userRiderId" = p_user_id and "rideId" = p_ride_id
    ) then
        return 'ALREADY_SUBSCRIBED';
    end if;

    update ride
    set "availableSeats" = "availableSeats" - 1
    where id = p_ride_id;

    insert into subscription("id","userRiderId", "rideId", "onlyInRain", "reoccurrence", "syncState")
    values (p_subscription_id, p_user_id, p_ride_id, p_only_in_rain, p_reoccurrence, 'SYNCED');

    return 'OK';

end;
$function$;